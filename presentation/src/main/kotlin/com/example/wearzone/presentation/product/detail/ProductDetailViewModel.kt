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
import com.example.wearzone.domain.product.model.Product
import com.example.wearzone.domain.product.model.ProductDetail
import com.example.wearzone.domain.product.usecase.GetProductDetailUseCase
import com.example.wearzone.domain.wishlist.model.WishlistItem
import com.example.wearzone.domain.wishlist.usecase.ObserveWishlistUseCase
import com.example.wearzone.domain.product.repository.IReviewRepository
import com.example.wearzone.domain.wishlist.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

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
    private val observeCartUseCase: ObserveCartUseCase
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
            delay(1000)
            when (val result = getProductDetailUseCase(productId)) {
                is DataResult.Success -> {
                    val productDetail = result.data
                    currentProduct = productDetail
                    val accessState = getAuthAccessStateUseCase()
                    val user = if (accessState is AuthAccessState.AuthenticatedCustomer) {
                        getCurrentUserUseCase()
                    } else {
                        null
                    }
                    
                    _uiState.value = ProductDetailUiState.Success(
                        id = productDetail.id,
                        title = productDetail.title,
                        vendor = productDetail.vendor,
                        price = "${productDetail.price} ${productDetail.currencyCode}",
                        descriptionHtml = productDetail.descriptionHtml,
                        images = productDetail.images.toImmutableList(),
                        availableSizes = productDetail.availableSizes.toImmutableList(),
                        rating = productDetail.rating,
                        reviewsCount = productDetail.reviewsCount,
                        isFavorite = productDetail.isFavorite
                    )

                    // Observe real-time review updates
                    launch {
                        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
                        reviewRepository.getReviewsForProduct(productDetail.id).collect { reviewsList ->
                            val totalCount = reviewsList.size
                            val avgRating = if (totalCount > 0) {
                                reviewsList.map { it.rating }.sum() / totalCount
                            } else {
                                0.0
                            }
                            
                            val reviewsUiList = reviewsList.map { review ->
                                ClientReviewUiModel(
                                    id = review.id,
                                    shopperName = review.shopperName,
                                    rating = review.rating,
                                    comment = review.comment,
                                    formattedDate = dateFormat.format(java.util.Date(review.timestamp))
                                )
                            }.toImmutableList()
                            
                            _uiState.update { state ->
                                if (state is ProductDetailUiState.Success) {
                                    state.copy(
                                        rating = avgRating,
                                        reviewsCount = totalCount,
                                        reviews = reviewsUiList
                                    )
                                } else state
                            }
                        }
                    }
                    
                    if (user != null && user.uid.isNotEmpty()) {
                        launch {
                            observeWishlistUseCase(user.uid).collect { wishlistItems ->
                                val isFav = wishlistItems.any { it.id == productDetail.id }
                                _uiState.update { state ->
                                    if (state is ProductDetailUiState.Success) {
                                        state.copy(isFavorite = isFav)
                                    } else state
                                }
                            }
                        }
                    }

                    // Observe cart updates
                    launch {
                        observeCartUseCase().collect { cartItems ->
                            val totalQty = cartItems
                                .filter { it.productId == productDetail.id }
                                .sumOf { it.quantity }
                            _uiState.update { state ->
                                if (state is ProductDetailUiState.Success) {
                                    state.copy(quantityInCart = totalQty)
                                } else state
                            }
                        }
                    }
                }
                is DataResult.Error -> {
                    _uiState.value = ProductDetailUiState.Error(R.string.product_detail_error_loading)
                }
            }
        }
    }

    private fun selectSize(size: String) {
        _uiState.update { state ->
            if (state is ProductDetailUiState.Success) {
                state.copy(selectedSize = size)
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

            if (state.selectedSize == null) {
                _uiEffect.send(
                    ProductDetailUiEffect.ShowToast(
                        R.string.product_detail_select_size_first
                    )
                )
                return@launch
            }

            val product = currentProduct ?: return@launch
            // we need to merge between product model and productDetails to can save in cart in database

            val item = CartItem(
                variantId = product.variantId,
                productId = product.id,
                title = product.title,
                vendor = product.vendor,
                price = product.price,
                currencyCode = product.currencyCode,
                quantity = 1,
                maxQuantity = 10,
                imageUrl = product.images.firstOrNull(),
                size = state.selectedSize
            )

            when (addToCartUseCase(item)) {
                is DataResult.Success ->
                    _uiEffect.send(ProductDetailUiEffect.ShowToast(R.string.add_to_cart))

                is DataResult.Error ->
                    _uiEffect.send(ProductDetailUiEffect.ShowToast(R.string.failed_to_add_to_cart))
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
                price = state.price,
                currencyCode = "EGP", // Provide a default currency if not in ProductDetail
                imageUrl = state.images.firstOrNull() ?: "",
                isOutOfStock = false
            )
            
            toggleFavoriteUseCase(item, user.uid)
            
            if (isAdding) {
                _uiEffect.send(ProductDetailUiEffect.ShowToast(R.string.wishlist_item_added))
            }
        }
    }

    private fun submitReview(rating: Double, comment: String) {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            val shopperName = user?.displayName?.takeIf { it.isNotEmpty() } ?: "WearZone Shopper"
            
            val state = _uiState.value as? ProductDetailUiState.Success ?: return@launch
            
            when (val result = reviewRepository.addReview(
                productId = state.id,
                shopperName = shopperName,
                rating = rating,
                comment = comment
            )) {
                is DataResult.Success -> {
                    _uiEffect.send(ProductDetailUiEffect.ShowToast(R.string.review_submitted_successfully))
                }
                is DataResult.Error -> {
                    _uiEffect.send(ProductDetailUiEffect.ShowToast(R.string.review_submit_failed))
                }
            }
        }
    }
}
