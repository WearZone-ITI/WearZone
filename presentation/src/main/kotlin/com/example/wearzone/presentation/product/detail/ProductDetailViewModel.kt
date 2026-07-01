package com.example.wearzone.presentation.product.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presentation.R
import com.example.wearzone.domain.auth.repository.IAuthRepository
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.product.usecase.GetProductDetailUseCase
import com.example.wearzone.domain.wishlist.model.WishlistItem
import com.example.wearzone.domain.wishlist.usecase.ObserveWishlistUseCase
import com.example.wearzone.domain.wishlist.usecase.ToggleFavoriteUseCase
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
    private val observeWishlistUseCase: ObserveWishlistUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
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
            is ProductDetailUiIntent.OnToggleFavorite -> {
                val state = _uiState.value
                if (state is ProductDetailUiState.Success) {
                    if (state.isFavorite) {
                        _uiState.value = state.copy(showRemoveDialog = true)
                    } else {
                        toggleFavorite(isAdding = true)
                    }
                }
            }
            is ProductDetailUiIntent.OnCancelRemove -> {
                val state = _uiState.value
                if (state is ProductDetailUiState.Success) {
                    _uiState.value = state.copy(showRemoveDialog = false)
                }
            }
            is ProductDetailUiIntent.OnConfirmRemove -> {
                val state = _uiState.value
                if (state is ProductDetailUiState.Success) {
                    _uiState.value = state.copy(showRemoveDialog = false)
                    toggleFavorite(isAdding = false)
                }
            }
        }
    }

    private fun loadProduct() {
        viewModelScope.launch {
            _uiState.value = ProductDetailUiState.Loading
            when (val result = getProductDetailUseCase(productId)) {
                is DataResult.Success -> {
                    val productDetail = result.data
                    val user = authRepository.getCurrentUser()
                    
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
                    
                    if (user != null && user.uid.isNotEmpty()) {
                        launch {
                            observeWishlistUseCase(user.uid).collect { wishlistItems ->
                                val isFav = wishlistItems.any { it.id == productDetail.id.toString() }
                                _uiState.update { state ->
                                    if (state is ProductDetailUiState.Success) {
                                        state.copy(isFavorite = isFav)
                                    } else state
                                }
                            }
                        }
                    }
                }
                is DataResult.Error -> {
                    _uiState.value = ProductDetailUiState.Error(R.string.product_detail_error_loading)
                }
            }
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
                _uiEffect.send(ProductDetailUiEffect.ShowToast(R.string.product_detail_select_size_first))
                return@launch
            }
            
            _uiEffect.send(ProductDetailUiEffect.ShowToast(R.string.product_detail_added_to_cart))
        }
    }

    private fun toggleFavorite(isAdding: Boolean = true) {
        viewModelScope.launch {
            if (!authRepository.isLoggedIn()) {
                _uiEffect.send(ProductDetailUiEffect.ShowAuthRequiredError)
                return@launch
            }
            
            val state = _uiState.value as? ProductDetailUiState.Success ?: return@launch
            val user = authRepository.getCurrentUser() ?: return@launch
            
            val item = WishlistItem(
                id = state.id.toString(),
                title = state.title,
                vendor = state.vendor,
                price = state.price,
                currencyCode = "EGP", // Provide a default currency if not in ProductDetail
                imageUrl = state.images.firstOrNull() ?: "",
                isOutOfStock = false
            )
            
            toggleFavoriteUseCase(item, user.uid)
            
            if (isAdding) {
                _uiEffect.send(ProductDetailUiEffect.ShowToast(R.string.product_detail_added_to_cart))
            }
        }
    }
}
