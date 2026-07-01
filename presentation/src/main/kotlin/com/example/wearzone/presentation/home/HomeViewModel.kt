package com.example.wearzone.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wearzone.domain.auth.usecase.GetCurrentUserUseCase
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.product.usecase.GetProductsUseCase
import com.example.wearzone.domain.wishlist.model.WishlistItem
import com.example.wearzone.domain.wishlist.usecase.ObserveWishlistUseCase
import com.example.wearzone.domain.wishlist.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val observeWishlistUseCase: ObserveWishlistUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

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
            is HomeUiIntent.OnFavoriteClicked -> {
                if (intent.product.isFavorite) {
                    val currentState = _uiState.value
                    if (currentState is HomeUiState.Success) {
                        _uiState.value = currentState.copy(productToRemove = intent.product)
                    }
                } else {
                    toggleFavorite(intent.product)
                }
            }
            is HomeUiIntent.OnCancelRemove -> {
                val currentState = _uiState.value
                if (currentState is HomeUiState.Success) {
                    _uiState.value = currentState.copy(productToRemove = null)
                }
            }
            is HomeUiIntent.OnConfirmRemove -> {
                val currentState = _uiState.value
                if (currentState is HomeUiState.Success) {
                    val product = currentState.productToRemove
                    if (product != null) {
                        _uiState.value = currentState.copy(productToRemove = null)
                        toggleFavorite(product, isAdding = false)
                    }
                }
            }
        }
    }

    private fun toggleFavorite(product: com.example.wearzone.domain.product.model.Product, isAdding: Boolean = true) {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            if (user == null || user.uid.isEmpty()) {
                sendEffect(HomeUiEffect.ShowSnackbar("Sign in to add to wishlist"))
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
                sendEffect(HomeUiEffect.ShowSnackbar("Added to favorites"))
            } else {
                sendEffect(HomeUiEffect.ShowSnackbar("Item removed from wishlist"))
            }
        }
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { HomeUiState.Loading }

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
                _uiState.update {
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
        _uiState.update { currentState ->
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
