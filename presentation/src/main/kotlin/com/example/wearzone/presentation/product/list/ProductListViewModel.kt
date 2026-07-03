package com.example.wearzone.presentation.product.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.product.usecase.GetProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductListUiState())
    val uiState = _uiState.asStateFlow()
    private var currentCollectionId: Long? = null

    private val _uiEffect = MutableSharedFlow<ProductListUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    fun onIntent(intent: ProductListUiIntent) {

        when (intent) {

            is ProductListUiIntent.LoadProducts -> {
                currentCollectionId = intent.collectionId
                loadProducts(intent.collectionId)
            }

            is ProductListUiIntent.ProductClicked -> {

                viewModelScope.launch {
                    _uiEffect.emit(
                        ProductListUiEffect.NavigateToProductDetails(
                            intent.productId
                        )
                    )
                }
            }

            ProductListUiIntent.Retry -> {
                loadProducts(currentCollectionId)            }
        }
    }

    private fun loadProducts(collectionId: Long?) {

        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            when (val result = getProductsUseCase.getProducts(collectionId)) {

                is DataResult.Success -> {

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        products = result.data
                    )
                }

                is DataResult.Error -> {

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.error.toString()
                    )

                    _uiEffect.emit(
                        ProductListUiEffect.ShowError(
                            result.error.toString()
                        )
                    )
                }
            }
        }
    }
}