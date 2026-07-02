package com.example.wearzone.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wearzone.domain.auth.usecase.GetCurrentUserUseCase
import com.example.wearzone.domain.cart.model.CartItem
import com.example.wearzone.domain.cart.usecase.AddToCartUseCase
import com.example.wearzone.domain.cart.usecase.ObserveCartUseCase
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.product.usecase.GetProductsUseCase
import com.example.wearzone.domain.wishlist.model.WishlistItem
import com.example.wearzone.domain.wishlist.usecase.ObserveWishlistUseCase
import com.example.wearzone.domain.wishlist.usecase.ToggleFavoriteUseCase
import com.example.presentation.R
import com.example.wearzone.domain.product.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val observeCartUseCase: ObserveCartUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val observeWishlistUseCase: ObserveWishlistUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val homeDataState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    private val cartItemCount: StateFlow<Int> =
        observeCartUseCase()
            .map { items -> items.sumOf { it.quantity } }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                0
            )

    val uiState: StateFlow<HomeUiState> =
        combine(homeDataState, cartItemCount) { state, count ->
            if (state is HomeUiState.Success) {
                state.copy(cartItemCount = count)
            } else {
                state
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            HomeUiState.Loading
        )

    private val _uiEffect = Channel<HomeUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        handleIntent(HomeUiIntent.LoadHomeData)
    }

    fun handleIntent(intent: HomeUiIntent) {
        when (intent) {
            is HomeUiIntent.LoadHomeData -> loadHomeData()
            is HomeUiIntent.OnBrandClicked -> sendEffect(HomeUiEffect.NavigateToBrand(intent.brandId))
            is HomeUiIntent.OnCategoryClicked -> sendEffect(HomeUiEffect.NavigateToCategory(intent.categoryId))
            is HomeUiIntent.OnProductClicked -> sendEffect(HomeUiEffect.NavigateToProductDetail(intent.productId))
            is HomeUiIntent.OnCartClicked -> sendEffect(HomeUiEffect.NavigateToCart)
            is HomeUiIntent.OnAddToCartClicked -> addToCart(intent.product)
            is HomeUiIntent.OnFavoriteClicked -> {
                if (intent.product.isFavorite) {
                    val currentState = homeDataState.value
                    if (currentState is HomeUiState.Success) {
                        homeDataState.value = currentState.copy(productToRemove = intent.product)
                    }
                } else {
                    toggleFavorite(intent.product)
                }
            }
            is HomeUiIntent.OnCancelRemove -> {
                val currentState = homeDataState.value
                if (currentState is HomeUiState.Success) {
                    homeDataState.value = currentState.copy(productToRemove = null)
                }
            }
            is HomeUiIntent.OnConfirmRemove -> {
                val currentState = homeDataState.value
                if (currentState is HomeUiState.Success) {
                    val product = currentState.productToRemove
                    if (product != null) {
                        homeDataState.value = currentState.copy(productToRemove = null)
                        toggleFavorite(product, isAdding = false)
                    }
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
                size = "M"
            )
            when (addToCartUseCase(item)) {
                is DataResult.Success -> {
                    sendEffect(HomeUiEffect.ShowSnackbar(R.string.add_to_cart))
                }
                is DataResult.Error -> {
                    sendEffect(HomeUiEffect.ShowSnackbar(R.string.failed_to_add_to_cart))
                }
            }
        }
    }

    private fun toggleFavorite(product: com.example.wearzone.domain.product.model.Product, isAdding: Boolean = true) {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            if (user == null || user.uid.isEmpty()) {
                sendEffect(HomeUiEffect.ShowSnackbar(R.string.wishlist_sign_in_required))
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
            toggleFavoriteUseCase(item, user.uid)
            if (isAdding) {
                sendEffect(HomeUiEffect.ShowSnackbar(R.string.wishlist_item_added))
            } else {
                sendEffect(HomeUiEffect.ShowSnackbar(R.string.wishlist_item_removed))
            }
        }
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            homeDataState.update { HomeUiState.Loading }

            val categoriesResult = getProductsUseCase.getCategories()
            val brandsResult = getProductsUseCase.getBrands()
            val productsResult = getProductsUseCase.getProducts()
            val user = getCurrentUserUseCase()

            if (categoriesResult is DataResult.Success &&
                brandsResult is DataResult.Success &&
                productsResult is DataResult.Success
            ) {
                if (user != null && user.uid.isNotEmpty()) {
                    launch {
                        observeWishlistUseCase(user.uid).collect { wishlistItems ->
                            val wishlistIds = wishlistItems.map { it.id }.toSet()
                            updateStateWithWishlist(
                                categoriesResult.data,
                                brandsResult.data,
                                productsResult.data,
                                wishlistIds,
                                user.displayName ?: "Guest"
                            )
                        }
                    }
                } else {
                    updateStateWithWishlist(
                        categoriesResult.data,
                        brandsResult.data,
                        productsResult.data,
                        emptySet(),
                        "Guest"
                    )
                }
            } else {
                homeDataState.update {
                    HomeUiState.Error("Failed to load home data")
                }
            }
        }
    }

    private fun updateStateWithWishlist(
        categories: List<com.example.wearzone.domain.product.model.Category>,
        brands: List<com.example.wearzone.domain.product.model.Brand>,
        products: List<com.example.wearzone.domain.product.model.Product>,
        wishlistIds: Set<String>,
        userName: String
    ) {
        val updatedProducts = products.map { it.copy(isFavorite = wishlistIds.contains(it.id)) }
        val hero = updatedProducts.firstOrNull()
        val trending = updatedProducts.take(5)
        val newArrivals = updatedProducts.takeLast(4)
        homeDataState.update { currentState ->
            val productToRemove = if (currentState is HomeUiState.Success) currentState.productToRemove else null
            HomeUiState.Success(
                userName = userName,
                categories = categories.toImmutableList(),
                brands = brands.toImmutableList(),
                trendingProducts = trending.toImmutableList(),
                newArrivalProducts = newArrivals.toImmutableList(),
                heroProduct = hero,
                productToRemove = productToRemove
            )
        }
    }

    private fun sendEffect(effect: HomeUiEffect) {
        viewModelScope.launch {
            _uiEffect.send(effect)
        }
    }
}