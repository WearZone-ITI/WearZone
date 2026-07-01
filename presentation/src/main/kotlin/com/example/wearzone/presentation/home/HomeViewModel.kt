package com.example.wearzone.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wearzone.domain.auth.usecase.GetCurrentUserUseCase
import com.example.wearzone.domain.cart.model.CartItem
import com.example.wearzone.domain.cart.usecase.AddToCartUseCase
import com.example.wearzone.domain.cart.usecase.ObserveCartUseCase
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.product.usecase.GetProductsUseCase
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
    private val addToCartUseCase: AddToCartUseCase
) : ViewModel() {

    private val homeDataState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    private val cartItemCount: StateFlow<Int> =
        observeCartUseCase()
            .map { items -> items.sumOf { it.quantity } }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
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
            SharingStarted.WhileSubscribed(5000),
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
        }
    }

    private fun addToCart(product: com.example.wearzone.domain.product.model.Product) {
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
                    sendEffect(HomeUiEffect.ShowSnackbar("Added to cart"))
                }
                is DataResult.Error -> {
                    sendEffect(HomeUiEffect.ShowSnackbar("Failed to add to cart"))
                }
            }
        }
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            homeDataState.update { HomeUiState.Loading }

            val categoriesResult = getProductsUseCase.getCategories()
            val brandsResult = getProductsUseCase.getBrands()
            val productsResult = getProductsUseCase.getProducts()

            if (categoriesResult is DataResult.Success &&
                brandsResult is DataResult.Success &&
                productsResult is DataResult.Success
            ) {
                val products = productsResult.data
                val hero = products.firstOrNull()
                val trending = products.take(5)
                val newArrivals = products.takeLast(4)
                homeDataState.update {
                    HomeUiState.Success(
                        userName = getCurrentUserUseCase.invoke()?.displayName ?: "Guest",
                        categories = categoriesResult.data.toImmutableList(),
                        brands = brandsResult.data.toImmutableList(),
                        trendingProducts = trending.toImmutableList(),
                        newArrivalProducts = newArrivals.toImmutableList(),
                        heroProduct = hero
                    )
                }
            } else {
                homeDataState.update { HomeUiState.Error("Failed to load home data") }
            }
        }
    }

    private fun sendEffect(effect: HomeUiEffect) {
        viewModelScope.launch {
            _uiEffect.send(effect)
        }
    }
}