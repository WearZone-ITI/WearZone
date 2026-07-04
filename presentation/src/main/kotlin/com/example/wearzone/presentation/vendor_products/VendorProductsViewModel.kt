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
import com.example.wearzone.domain.wishlist.model.WishlistItem
import com.example.wearzone.domain.wishlist.usecase.ObserveWishlistUseCase
import com.example.wearzone.domain.wishlist.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val addToCartUseCase: AddToCartUseCase
) : ViewModel() {

    private val vendorName: String = savedStateHandle.get<String>("vendorName").orEmpty()

    private val _uiState = MutableStateFlow<VendorProductsUiState>(VendorProductsUiState.Loading)
    val uiState: StateFlow<VendorProductsUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<VendorProductsUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private var currentUserId: String? = null

    init {
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

    private fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = VendorProductsUiState.Loading
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
                        launch {
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
                    }
                }
                is DataResult.Error -> {
                    val message = when (val error = result.error) {
                        is DomainError.Server -> error.message ?: "Server error"
                        is DomainError.Network -> error.exception.message ?: "Network error"
                        is DomainError.Unknown -> error.exception.message ?: "Unknown error"
                    }
                    _uiState.value = VendorProductsUiState.Error(message)
                }
            }
        }
    }

    private fun addToCart(product: Product) {
        viewModelScope.launch {
            val item = CartItem(
                variantId = product.variantId,
                productId = product.id,
                title = product.title,
                vendor = product.vendor,
                price = product.price,
                currencyCode = product.currencyCode,
                quantity = 1,
                maxQuantity = 10,
                imageUrl = product.imageUrl,
                size = "M" // Default size fallback
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
                _uiEffect.send(VendorProductsUiEffect.ShowSignInRequired)
                return@launch
            }

            val item = WishlistItem(
                id = product.id,
                title = product.title,
                vendor = product.vendor,
                price = product.price.toString(),
                currencyCode = product.currencyCode,
                imageUrl = product.imageUrl ?: "",
                isOutOfStock = false
            )

            toggleFavoriteUseCase(item, userId)
            
            // Show toast/snackbar feedback
            if (!product.isFavorite) {
                _uiEffect.send(VendorProductsUiEffect.ShowSnackbar(R.string.wishlist_item_added))
            }
        }
    }
}
