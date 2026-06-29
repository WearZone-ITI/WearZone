package com.wearzone.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wearzone.domain.common.result.DataResult
import com.wearzone.domain.product.usecase.GetProductsUseCase
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
class HomeViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase
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
        }
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { HomeUiState.Loading }

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

                _uiState.update {
                    HomeUiState.Success(
                        categories = categoriesResult.data.toImmutableList(),
                        brands = brandsResult.data.toImmutableList(),
                        trendingProducts = trending.toImmutableList(),
                        newArrivalProducts = newArrivals.toImmutableList(),
                        heroProduct = hero
                    )
                }
            } else {
                _uiState.update {
                    HomeUiState.Error("Failed to load home data")
                }
            }
        }
    }

    private fun sendEffect(effect: HomeUiEffect) {
        viewModelScope.launch {
            _uiEffect.send(effect)
        }
    }
}
