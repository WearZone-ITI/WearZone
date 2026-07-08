package com.example.wearzone.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wearzone.domain.auth.model.AuthAccessState
import com.example.wearzone.domain.auth.usecase.GetAuthAccessStateUseCase
import com.example.wearzone.domain.auth.usecase.GetCurrentUserUseCase
import com.example.wearzone.domain.cart.model.CartItem
import com.example.wearzone.domain.cart.usecase.AddToCartUseCase
import com.example.wearzone.domain.cart.usecase.ObserveCartItemCountUseCase
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.product.usecase.GetProductsUseCase
import com.example.wearzone.domain.wishlist.model.WishlistItem
import com.example.wearzone.domain.wishlist.usecase.ObserveWishlistUseCase
import com.example.wearzone.domain.wishlist.usecase.ToggleFavoriteUseCase
import com.example.wearzone.domain.settings.usecase.ObserveSettingsPreferencesUseCase
import com.example.presentation.R
import com.example.wearzone.domain.product.model.Product
import com.example.wearzone.domain.common.Category
import com.example.wearzone.domain.product.model.Brand
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val getAuthAccessStateUseCase: GetAuthAccessStateUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val observeCartItemCountUseCase: ObserveCartItemCountUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val observeWishlistUseCase: ObserveWishlistUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val observeSettingsPreferencesUseCase: ObserveSettingsPreferencesUseCase,
) : ViewModel() {

    private val homeDataState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    private val cartItemCount: StateFlow<Int> =
        observeCartItemCountUseCase()
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

    private var wishlistJob: Job? = null
    private var loadHomeJob: Job? = null

    init {
        observeLanguageChanges()
        handleIntent(HomeUiIntent.LoadHomeData)
    }

    fun handleIntent(intent: HomeUiIntent) {
        when (intent) {
            is HomeUiIntent.LoadHomeData -> loadHomeData()
            is HomeUiIntent.OnBrandClicked -> {
                val currentState = homeDataState.value
                val brandName = if (currentState is HomeUiState.Success) {
                    currentState.brands.find { it.id == intent.brandId }?.title ?: intent.brandId
                } else {
                    intent.brandId
                }
                sendEffect(HomeUiEffect.NavigateToBrand(brandName))
            }
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
            if (getAuthAccessStateUseCase() !is AuthAccessState.AuthenticatedCustomer) {
                sendEffect(HomeUiEffect.ShowSignInRequired(R.string.sign_in_required_cart_message))
                return@launch
            }

            if (product.isOutOfStock || product.maxQuantity <= 0) {
                sendEffect(HomeUiEffect.ShowSnackbar(R.string.cart_out_of_stock))
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
                is DataResult.Success -> {
                    sendEffect(HomeUiEffect.ShowSnackbar(R.string.add_to_cart))
                }
                is DataResult.Error -> {
                    sendEffect(HomeUiEffect.ShowSnackbar(R.string.failed_to_add_to_cart))
                }
            }
        }
    }

    private fun toggleFavorite(product: Product, isAdding: Boolean = true) {
        viewModelScope.launch {
            if (getAuthAccessStateUseCase() !is AuthAccessState.AuthenticatedCustomer) {
                sendEffect(HomeUiEffect.ShowSignInRequired(R.string.sign_in_required_wishlist_message))
                return@launch
            }

            val user = getCurrentUserUseCase()
            if (user == null || user.uid.isEmpty()) {
                sendEffect(HomeUiEffect.ShowSignInRequired(R.string.sign_in_required_wishlist_message))
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
            toggleFavoriteUseCase(item, user.uid)
            if (isAdding) {
                sendEffect(HomeUiEffect.ShowSnackbar(R.string.wishlist_item_added))
            } else {
                sendEffect(HomeUiEffect.ShowSnackbar(R.string.wishlist_item_removed))
            }
        }
    }

    private fun loadHomeData(showLoading: Boolean = true) {
        loadHomeJob?.cancel()
        loadHomeJob = viewModelScope.launch {
            if (showLoading || homeDataState.value !is HomeUiState.Success) {
                homeDataState.update { HomeUiState.Loading }
            }

            val categoriesResult = getProductsUseCase.getCategories()
            val brandsResult = getProductsUseCase.getBrands()
            val productsResult = getProductsUseCase.getProducts()
            val accessState = getAuthAccessStateUseCase()
            val user = if (accessState is AuthAccessState.AuthenticatedCustomer) {
                getCurrentUserUseCase()
            } else {
                null
            }

            if (categoriesResult is DataResult.Success &&
                brandsResult is DataResult.Success &&
                productsResult is DataResult.Success
            ) {
                if (user != null && user.uid.isNotEmpty()) {
                    wishlistJob?.cancel()
                    wishlistJob = launch {
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
                    wishlistJob?.cancel()
                    updateStateWithWishlist(
                        categoriesResult.data,
                        brandsResult.data,
                        productsResult.data,
                        emptySet(),
                        "Guest"
                    )
                }
            } else {
                homeDataState.update { currentState ->
                    if (currentState is HomeUiState.Success && !showLoading) {
                        currentState
                    } else {
                        HomeUiState.Error("Failed to load home data")
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
            .onEach { loadHomeData(showLoading = false) }
            .launchIn(viewModelScope)
    }

    private fun updateStateWithWishlist(
        categories: List<Category>,
        brands: List<Brand>,
        products: List<Product>,
        wishlistIds: Set<String>,
        userName: String
    ) {
        val updatedProducts = products.map { it.copy(isFavorite = wishlistIds.contains(it.id)) }
        val hero = updatedProducts.firstOrNull()
        val trending = updatedProducts.take(5)
        val newArrivals = updatedProducts.takeLast(4)
        val promoAds = updatedProducts.shuffled().take(3)

        homeDataState.update { currentState ->
            val productToRemove = if (currentState is HomeUiState.Success) currentState.productToRemove else null
            HomeUiState.Success(
                userName = userName,
                categories = categories.toImmutableList(),
                brands = brands.toImmutableList(),
                trendingProducts = trending.toImmutableList(),
                newArrivalProducts = newArrivals.toImmutableList(),
                heroProduct = hero,
                promoAds = promoAds.toImmutableList(),
                productToRemove = productToRemove
            )
        }
    }

    private fun selectedProductLabel(size: String?, color: String?): String? =
        listOfNotNull(size, color).joinToString(" / ").takeIf { it.isNotBlank() }

    private fun sendEffect(effect: HomeUiEffect) {
        viewModelScope.launch {
            _uiEffect.send(effect)
        }
    }
}
