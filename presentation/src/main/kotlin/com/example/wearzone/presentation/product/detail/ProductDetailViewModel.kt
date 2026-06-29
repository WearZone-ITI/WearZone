package com.example.wearzone.presentation.product.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wearzone.domain.auth.repository.IAuthRepository
import com.example.wearzone.domain.product.usecase.GetProductDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getProductDetailUseCase: GetProductDetailUseCase,
    private val authRepository: IAuthRepository,
) : ViewModel() {

    private val productId: Long = savedStateHandle["productId"] ?: 9091143729380L

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
            is ProductDetailUiIntent.AddToCart -> addToCart()
            is ProductDetailUiIntent.OnToggleFavorite -> toggleFavorite()
        }
    }

    private fun loadProduct() {
        viewModelScope.launch {
            _uiState.value = ProductDetailUiState.Loading
            getProductDetailUseCase(productId).fold(
                onSuccess = { productDetail ->
                    _uiState.value = ProductDetailUiState.Success(
                        id = productDetail.id,
                        title = productDetail.title,
                        vendor = productDetail.vendor,
                        price = productDetail.price,
                        descriptionHtml = productDetail.descriptionHtml,
                        images = productDetail.images.toImmutableList(),
                        availableSizes = productDetail.availableSizes.toImmutableList(),
                        rating = productDetail.rating,
                        reviewsCount = productDetail.reviewsCount,
                        isFavorite = productDetail.isFavorite
                    )
                },
                onFailure = { error ->
                    _uiState.value = ProductDetailUiState.Error(error.message ?: "Failed to load product")
                }
            )
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
            if (!authRepository.isLoggedIn()) {
                _uiEffect.send(ProductDetailUiEffect.ShowAuthRequiredError)
                return@launch
            }
            
            val state = _uiState.value as? ProductDetailUiState.Success ?: return@launch
            if (state.selectedSize == null) {
                _uiEffect.send(ProductDetailUiEffect.ShowToast("Please select a size first."))
                return@launch
            }
            
            _uiEffect.send(ProductDetailUiEffect.NavigateToCart)
        }
    }

    private fun toggleFavorite() {
        viewModelScope.launch {
            if (!authRepository.isLoggedIn()) {
                _uiEffect.send(ProductDetailUiEffect.ShowAuthRequiredError)
                return@launch
            }
            
            _uiState.update { state ->
                if (state is ProductDetailUiState.Success) {
                    state.copy(isFavorite = !state.isFavorite)
                } else {
                    state
                }
            }
        }
    }
}
