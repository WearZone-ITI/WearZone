package com.example.wearzone.presentation.product.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presentation.R
import com.example.wearzone.domain.auth.model.AuthAccessState
import com.example.wearzone.domain.auth.usecase.GetAuthAccessStateUseCase
import com.example.wearzone.domain.auth.usecase.GetCurrentUserUseCase
import com.example.wearzone.domain.cart.model.CartItem
import com.example.wearzone.domain.cart.usecase.AddToCartUseCase
import com.example.wearzone.domain.cart.usecase.ObserveCartUseCase
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.product.model.ProductDetail
import com.example.wearzone.domain.product.model.ProductVariant
import com.example.wearzone.domain.product.repository.IReviewRepository
import com.example.wearzone.domain.product.usecase.GetProductDetailUseCase
import com.example.wearzone.domain.wishlist.model.WishlistItem
import com.example.wearzone.domain.wishlist.usecase.ObserveWishlistUseCase
import com.example.wearzone.domain.wishlist.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getProductDetailUseCase: GetProductDetailUseCase,
    private val getAuthAccessStateUseCase: GetAuthAccessStateUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val observeWishlistUseCase: ObserveWishlistUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val reviewRepository: IReviewRepository,
    private val observeCartUseCase: ObserveCartUseCase,
) : ViewModel() {

    private val productId: Long = savedStateHandle.get<String>("productId")?.toLongOrNull() ?: 9091143729380L
    private var currentProduct: ProductDetail? = null
    private val _uiState = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState.Loading)
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<ProductDetailUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<ProductDetailUiEffect> = _uiEffect.receiveAsFlow()

    init {
        loadProduct()
    }

    fun handleIntent(intent: ProductDetailUiIntent) {
        when (intent) {
            is ProductDetailUiIntent.LoadProduct -> loadProduct()
            is ProductDetailUiIntent.Retry -> loadProduct()
            is ProductDetailUiIntent.SelectSize -> selectSize(intent.size)
            is ProductDetailUiIntent.SelectColor -> selectColor(intent.color)
            is ProductDetailUiIntent.OnToggleFavorite -> {
                val state = _uiState.value
                if (state is ProductDetailUiState.Success) {
                    if (state.isFavorite) {
                        _uiState.value = state.copy(showRemoveDialog = true)
                    } else {
                        toggleFavorite(isAdding = true)
                    }
                }
            }
            is ProductDetailUiIntent.OnCancelRemove -> {
                val state = _uiState.value
                if (state is ProductDetailUiState.Success) {
                    _uiState.value = state.copy(showRemoveDialog = false)
                }
            }
            is ProductDetailUiIntent.OnConfirmRemove -> {
                val state = _uiState.value
                if (state is ProductDetailUiState.Success) {
                    _uiState.value = state.copy(showRemoveDialog = false)
                    toggleFavorite(isAdding = false)
                }
            }
            is ProductDetailUiIntent.OnAddToCartClick -> addToCart()
            is ProductDetailUiIntent.SubmitReview -> submitReview(intent.rating, intent.comment)
            is ProductDetailUiIntent.OnWriteReviewClick -> {
                viewModelScope.launch {
                    if (getAuthAccessStateUseCase() !is AuthAccessState.AuthenticatedCustomer) {
                        _uiEffect.send(ProductDetailUiEffect.ShowSignInRequired(R.string.sign_in_required_message))
                    } else {
                        _uiEffect.send(ProductDetailUiEffect.OpenWriteReviewSheet)
                    }
                }
            }
        }
    }

    private fun loadProduct() {
        viewModelScope.launch {
            _uiState.value = ProductDetailUiState.Loading
            when (val result = getProductDetailUseCase(productId)) {
                is DataResult.Success -> {
                    val productDetail = result.data
                    currentProduct = productDetail
                    val defaultSize = productDetail.availableSizes.singleOrNull()
                    val defaultColor = productDetail.availableColors.singleOrNull()
                    val selectedVariant = productDetail.bestVariantFor(defaultSize, defaultColor)
                    val accessState = getAuthAccessStateUseCase()
                    val user = if (accessState is AuthAccessState.AuthenticatedCustomer) getCurrentUserUseCase() else null

                    _uiState.value = ProductDetailUiState.Success(
                        id = productDetail.id,
                        title = productDetail.title,
                        vendor = productDetail.vendor,
                        basePriceEgp = selectedVariant?.price ?: productDetail.price,
                        descriptionHtml = productDetail.descriptionHtml,
                        images = productDetail.imagesWithVariantFirst(selectedVariant).toImmutableList(),
                        availableSizes = productDetail.availableSizes.toImmutableList(),
                        availableColors = productDetail.availableColors.toImmutableList(),
                        selectedSize = defaultSize,
                        selectedColor = defaultColor,
                        rating = productDetail.rating,
                        reviewsCount = productDetail.reviewsCount,
                        isFavorite = productDetail.isFavorite,
                        isOutOfStock = productDetail.isOutOfStock,
                        selectedVariantQuantity = selectedVariant?.availableQuantity ?: 0,
                    )

                    launchReviewsObserver(productDetail.id)
                    if (user != null && user.uid.isNotEmpty()) launchWishlistObserver(user.uid, productDetail.id)
                    launchCartObserver(productDetail.id)
                }
                is DataResult.Error -> {
                    _uiState.value = ProductDetailUiState.Error(R.string.product_detail_error_loading)
                }
            }
        }
    }

    private fun selectSize(size: String) {
        val product = currentProduct ?: return
        _uiState.update { state ->
            if (state is ProductDetailUiState.Success) {
                val variant = product.bestVariantFor(size, state.selectedColor)
                state.copy(
                    selectedSize = size,
                    basePriceEgp = variant?.price ?: product.price,
                    images = product.imagesWithVariantFirst(variant).toImmutableList(),
                    isOutOfStock = variant?.isAvailable == false,
                    selectedVariantQuantity = variant?.availableQuantity ?: 0,
                )
            } else {
                state
            }
        }
    }

    private fun selectColor(color: String) {
        val product = currentProduct ?: return
        _uiState.update { state ->
            if (state is ProductDetailUiState.Success) {
                val variant = product.bestVariantFor(state.selectedSize, color)
                state.copy(
                    selectedColor = color,
                    basePriceEgp = variant?.price ?: product.price,
                    images = product.imagesWithVariantFirst(variant).toImmutableList(),
                    isOutOfStock = variant?.isAvailable == false,
                    selectedVariantQuantity = variant?.availableQuantity ?: 0,
                )
            } else {
                state
            }
        }
    }

    private fun addToCart() {
        viewModelScope.launch {
            val state = _uiState.value as? ProductDetailUiState.Success ?: return@launch

            if (getAuthAccessStateUseCase() !is AuthAccessState.AuthenticatedCustomer) {
                _uiEffect.send(ProductDetailUiEffect.ShowSignInRequired(R.string.sign_in_required_cart_message))
                return@launch
            }

            val product = currentProduct ?: return@launch
            if (product.availableSizes.isNotEmpty() && state.selectedSize == null) {
                _uiEffect.send(ProductDetailUiEffect.ShowToast(R.string.product_detail_select_size_first))
                return@launch
            }
            if (product.availableColors.isNotEmpty() && state.selectedColor == null) {
                _uiEffect.send(ProductDetailUiEffect.ShowToast(R.string.product_detail_select_color_first))
                return@launch
            }

            val variant = product.bestVariantFor(state.selectedSize, state.selectedColor)
            if (product.isOutOfStock || variant?.isAvailable == false) {
                _uiEffect.send(ProductDetailUiEffect.ShowToast(R.string.cart_out_of_stock))
                return@launch
            }

            val item = CartItem(
                variantId = variant?.id?.takeIf { it.isNotBlank() } ?: product.variantId,
                productId = product.id,
                title = product.title,
                vendor = product.vendor,
                price = variant?.price ?: product.price,
                currencyCode = product.currencyCode,
                quantity = 1,
                maxQuantity = (variant?.availableQuantity ?: 10).coerceAtLeast(1),
                imageUrl = variant?.imageUrl ?: product.images.firstOrNull(),
                size = selectedVariantLabel(variant, state.selectedSize, state.selectedColor),
            )

            when (addToCartUseCase(item)) {
                is DataResult.Success -> _uiEffect.send(ProductDetailUiEffect.ShowToast(R.string.add_to_cart))
                is DataResult.Error -> _uiEffect.send(ProductDetailUiEffect.ShowToast(R.string.failed_to_add_to_cart))
            }
        }
    }

    private fun toggleFavorite(isAdding: Boolean = true) {
        viewModelScope.launch {
            if (getAuthAccessStateUseCase() !is AuthAccessState.AuthenticatedCustomer) {
                _uiEffect.send(ProductDetailUiEffect.ShowSignInRequired(R.string.sign_in_required_wishlist_message))
                return@launch
            }

            val state = _uiState.value as? ProductDetailUiState.Success ?: return@launch
            val user = getCurrentUserUseCase()
            if (user == null || user.uid.isEmpty()) {
                _uiEffect.send(ProductDetailUiEffect.ShowSignInRequired(R.string.sign_in_required_wishlist_message))
                return@launch
            }

            val item = WishlistItem(
                id = state.id,
                title = state.title,
                vendor = state.vendor,
                price = state.basePriceEgp.toString(),
                currencyCode = "EGP",
                imageUrl = state.images.firstOrNull() ?: "",
                isOutOfStock = state.isOutOfStock,
            )

            toggleFavoriteUseCase(item, user.uid)
            if (isAdding) _uiEffect.send(ProductDetailUiEffect.ShowToast(R.string.wishlist_item_added))
        }
    }

    private fun submitReview(rating: Double, comment: String) {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            val shopperName = user?.displayName?.takeIf { it.isNotEmpty() } ?: "WearZone Shopper"
            val state = _uiState.value as? ProductDetailUiState.Success ?: return@launch

            when (reviewRepository.addReview(productId = state.id, shopperName = shopperName, rating = rating, comment = comment)) {
                is DataResult.Success -> _uiEffect.send(ProductDetailUiEffect.ShowToast(R.string.review_submitted_successfully))
                is DataResult.Error -> _uiEffect.send(ProductDetailUiEffect.ShowToast(R.string.review_submit_failed))
            }
        }
    }

    private fun launchReviewsObserver(productId: String) {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            reviewRepository.getReviewsForProduct(productId).collect { reviewsList ->
                val totalCount = reviewsList.size
                val avgRating = if (totalCount > 0) reviewsList.map { it.rating }.sum() / totalCount else 0.0
                val reviewsUiList = reviewsList.map { review ->
                    ClientReviewUiModel(
                        id = review.id,
                        shopperName = review.shopperName,
                        rating = review.rating,
                        comment = review.comment,
                        formattedDate = dateFormat.format(java.util.Date(review.timestamp)),
                    )
                }.toImmutableList()

                _uiState.update { state ->
                    if (state is ProductDetailUiState.Success) {
                        state.copy(rating = avgRating, reviewsCount = totalCount, reviews = reviewsUiList)
                    } else {
                        state
                    }
                }
            }
        }
    }

    private fun launchWishlistObserver(userId: String, productId: String) {
        viewModelScope.launch {
            observeWishlistUseCase(userId).collect { wishlistItems ->
                val isFav = wishlistItems.any { it.id == productId }
                _uiState.update { state ->
                    if (state is ProductDetailUiState.Success) state.copy(isFavorite = isFav) else state
                }
            }
        }
    }

    private fun launchCartObserver(productId: String) {
        viewModelScope.launch {
            observeCartUseCase().collect { cartItems ->
                val totalQty = cartItems.filter { it.productId == productId }.sumOf { it.quantity }
                _uiState.update { state ->
                    if (state is ProductDetailUiState.Success) state.copy(quantityInCart = totalQty) else state
                }
            }
        }
    }

    private fun ProductDetail.bestVariantFor(size: String?, color: String?): ProductVariant? {
        val matching = variants.filter { variant ->
            (size == null || variant.size == size) && (color == null || variant.color == color)
        }
        return matching.firstOrNull { it.isAvailable }
            ?: matching.firstOrNull()
            ?: variants.firstOrNull { it.isAvailable }
            ?: variants.firstOrNull()
    }

    private fun selectedVariantLabel(variant: ProductVariant?, selectedSize: String?, selectedColor: String?): String? =
        listOfNotNull(variant?.size ?: selectedSize, variant?.color ?: selectedColor)
            .joinToString(" / ")
            .takeIf { it.isNotBlank() }

    private fun ProductDetail.imagesWithVariantFirst(variant: ProductVariant?): List<String> = buildList {
        variant?.imageUrl?.takeIf { it.isNotBlank() }?.let(::add)
        addAll(images)
    }.distinct()

    private fun formatMoney(amount: Double, currencyCode: String): String =
        String.format(Locale.US, "%.2f %s", amount, currencyCode.ifBlank { "EGP" })
}
