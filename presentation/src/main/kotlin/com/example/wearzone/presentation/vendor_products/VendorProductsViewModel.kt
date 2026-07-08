package com.example.wearzone.presentation.vendor_products

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presentation.R
import com.example.wearzone.domain.auth.model.AuthAccessState
import com.example.wearzone.domain.auth.usecase.GetAuthAccessStateUseCase
import com.example.wearzone.domain.auth.usecase.GetCurrentUserUseCase
import com.example.wearzone.domain.cart.model.CartItem
import com.example.wearzone.domain.cart.usecase.AddToCartUseCase
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.common.DomainError
import com.example.wearzone.domain.product.model.Product
import com.example.wearzone.domain.product.usecase.GetProductsByVendorUseCase
import com.example.wearzone.domain.settings.usecase.ObserveSettingsPreferencesUseCase
import com.example.wearzone.domain.wishlist.model.WishlistItem
import com.example.wearzone.domain.wishlist.usecase.ObserveWishlistUseCase
import com.example.wearzone.domain.wishlist.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VendorProductsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getProductsByVendorUseCase: GetProductsByVendorUseCase,
    private val getAuthAccessStateUseCase: GetAuthAccessStateUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val observeWishlistUseCase: ObserveWishlistUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val observeSettingsPreferencesUseCase: ObserveSettingsPreferencesUseCase,
) : ViewModel() {

    private val vendorName: String = savedStateHandle.get<String>("vendorName").orEmpty()

    private val _uiState = MutableStateFlow<VendorProductsUiState>(VendorProductsUiState.Loading)
    val uiState: StateFlow<VendorProductsUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<VendorProductsUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private var currentUserId: String? = null
    private var loadProductsJob: Job? = null
    private var wishlistJob: Job? = null

    init {
        observeLanguageChanges()
        loadProducts()
    }

    fun handleIntent(intent: VendorProductsUiIntent) {
        when (intent) {
            is VendorProductsUiIntent.LoadProducts -> loadProducts()
            is VendorProductsUiIntent.OnRetry -> loadProducts()
            is VendorProductsUiIntent.OnProductClicked -> {
                viewModelScope.launch {
                    _uiEffect.send(VendorProductsUiEffect.NavigateToProductDetail(intent.productId))
                }
            }
            is VendorProductsUiIntent.OnAddToCartClicked -> addToCart(intent.product)
            is VendorProductsUiIntent.OnFavoriteClicked -> toggleFavorite(intent.product)
        }
    }

    private fun loadProducts(showLoading: Boolean = true) {
        loadProductsJob?.cancel()
        loadProductsJob = viewModelScope.launch {
            if (showLoading || _uiState.value !is VendorProductsUiState.Success) {
                _uiState.value = VendorProductsUiState.Loading
            }
            val accessState = getAuthAccessStateUseCase()
            val user = if (accessState is AuthAccessState.AuthenticatedCustomer) {
                getCurrentUserUseCase()
            } else {
                null
            }
            currentUserId = user?.uid

            when (val result = getProductsByVendorUseCase(vendorName)) {
                is DataResult.Success -> {
                    val products = result.data
                    _uiState.value = VendorProductsUiState.Success(
                        products = products.toImmutableList(),
                        vendorName = vendorName
                    )
                    // If user is logged in, observe wishlist to update isFavorite flags dynamically
                    if (user != null && user.uid.isNotEmpty()) {
                        wishlistJob?.cancel()
                        wishlistJob = launch {
                            observeWishlistUseCase(user.uid).collect { wishlistItems ->
                                val wishlistIds = wishlistItems.map { it.id }.toSet()
                                _uiState.update { state ->
                                    if (state is VendorProductsUiState.Success) {
                                        val updatedProducts = state.products.map { product ->
                                            product.copy(isFavorite = wishlistIds.contains(product.id))
                                        }.toImmutableList()
                                        state.copy(products = updatedProducts)
                                    } else state
                                }
                            }
                        }
                    } else {
                        wishlistJob?.cancel()
                    }
                }
                is DataResult.Error -> {
                    val message = when (val error = result.error) {
                        is DomainError.Server -> error.message ?: "Server error"
                        is DomainError.Network -> error.exception.message ?: "Network error"
                        is DomainError.Unknown -> error.exception.message ?: "Unknown error"
                    }
                    if (showLoading || _uiState.value !is VendorProductsUiState.Success) {
                        _uiState.value = VendorProductsUiState.Error(message)
                    }
                }
            }
        }
    }

    private fun observeLanguageChanges() {
        observeSettingsPreferencesUseCase()
            .map { it.languageCode }
            .distinctUntilChanged()
            .drop(1)
            .onEach { loadProducts(showLoading = false) }
            .launchIn(viewModelScope)
    }

    private fun addToCart(product: Product) {
        viewModelScope.launch {
            if (getAuthAccessStateUseCase() !is AuthAccessState.AuthenticatedCustomer) {
                _uiEffect.send(VendorProductsUiEffect.ShowSignInRequired(R.string.sign_in_required_cart_message))
                return@launch
            }

            if (product.isOutOfStock || product.maxQuantity <= 0) {
                _uiEffect.send(VendorProductsUiEffect.ShowSnackbar(R.string.cart_out_of_stock))
                return@launch
            }

            val item = CartItem(
                variantId = product.variantId,
                productId = product.id,
                title = product.title,
                vendor = product.vendor,
                price = product.price,
                currencyCode = product.currencyCode,
                quantity = 1,
                maxQuantity = product.maxQuantity,
                imageUrl = product.imageUrl,
                size = selectedProductLabel(product.size, product.color)
            )

            when (addToCartUseCase(item)) {
                is DataResult.Success ->
                    _uiEffect.send(VendorProductsUiEffect.ShowSnackbar(R.string.add_to_cart))
                is DataResult.Error ->
                    _uiEffect.send(VendorProductsUiEffect.ShowSnackbar(R.string.failed_to_add_to_cart))
            }
        }
    }

    private fun toggleFavorite(product: Product) {
        viewModelScope.launch {
            val userId = currentUserId
            if (userId == null || getAuthAccessStateUseCase() !is AuthAccessState.AuthenticatedCustomer) {
                _uiEffect.send(VendorProductsUiEffect.ShowSignInRequired(R.string.sign_in_required_wishlist_message))
                return@launch
            }

            val item = WishlistItem(
                id = product.id,
                title = product.title,
                vendor = product.vendor,
                price = product.price.toString(),
                currencyCode = product.currencyCode,
                imageUrl = product.imageUrl ?: "",
                isOutOfStock = product.isOutOfStock
            )

            toggleFavoriteUseCase(item, userId)
            
            // Show toast/snackbar feedback
            if (!product.isFavorite) {
                _uiEffect.send(VendorProductsUiEffect.ShowSnackbar(R.string.wishlist_item_added))
            }
        }
    }

    private fun selectedProductLabel(size: String?, color: String?): String? =
        listOfNotNull(size, color).joinToString(" / ").takeIf { it.isNotBlank() }
}
