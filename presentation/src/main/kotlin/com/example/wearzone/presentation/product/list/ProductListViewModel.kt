package com.example.wearzone.presentation.product.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wearzone.domain.cart.usecase.ObserveCartItemCountUseCase
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.product.usecase.GetProductsUseCase
import com.example.wearzone.domain.settings.usecase.ObserveSettingsPreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val observeCartItemCountUseCase: ObserveCartItemCountUseCase,
    private val observeSettingsPreferencesUseCase: ObserveSettingsPreferencesUseCase,
) : ViewModel() {
    private var currentCollectionId: Long? = null
    private var loadProductsJob: Job? = null
    private val cartItemCount: StateFlow<Int> =
        observeCartItemCountUseCase()
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                0
            )
    private val productListState = MutableStateFlow(ProductListUiState())
    val uiState: StateFlow<ProductListUiState> =
        combine(
            productListState,
            cartItemCount
        ) { state, count ->

            state.copy(
                cartItemCount = count
            )

        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            ProductListUiState()
        )

    private val _uiEffect = MutableSharedFlow<ProductListUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        observeLanguageChanges()
    }

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
            is ProductListUiIntent.OnCartClicked ->  viewModelScope.launch {
                _uiEffect.emit(ProductListUiEffect.NavigateToCart)
            }

            ProductListUiIntent.Retry -> {
                loadProducts(currentCollectionId)            }
        }
    }

    private fun loadProducts(collectionId: Long?, showLoading: Boolean = true) {
        loadProductsJob?.cancel()
        loadProductsJob = viewModelScope.launch {
            if (showLoading || productListState.value.products.isEmpty()) {
                productListState.value = productListState.value.copy(
                    isLoading = true,
                    error = null
                )
            } else {
                productListState.value = productListState.value.copy(error = null)
            }

            when (val result = getProductsUseCase.getProducts(collectionId)) {

                is DataResult.Success -> {

                    productListState.value = productListState.value.copy(
                        isLoading = false,
                        products = result.data
                    )
                }

                is DataResult.Error -> {

                    productListState.value = productListState.value.copy(
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

    private fun observeLanguageChanges() {
        observeSettingsPreferencesUseCase()
            .map { it.languageCode }
            .distinctUntilChanged()
            .drop(1)
            .onEach { loadProducts(currentCollectionId, showLoading = false) }
            .launchIn(viewModelScope)
    }
}
