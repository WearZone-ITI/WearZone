package com.example.wearzone.presentation.brands

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.common.DomainError
import com.example.wearzone.domain.product.usecase.GetBrandsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrandsViewModel @Inject constructor(
    private val getBrandsUseCase: GetBrandsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BrandsUiState>(BrandsUiState.Loading)
    val uiState: StateFlow<BrandsUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<BrandsUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        handleIntent(BrandsUiIntent.LoadBrands)
    }

    fun handleIntent(intent: BrandsUiIntent) {
        when (intent) {
            is BrandsUiIntent.LoadBrands -> loadBrands()
            is BrandsUiIntent.OnBrandClicked -> {
                viewModelScope.launch {
                    _uiEffect.send(BrandsUiEffect.NavigateToVendorProducts(intent.brandName))
                }
            }
        }
    }

    private fun loadBrands() {
        viewModelScope.launch {
            _uiState.value = BrandsUiState.Loading
            when (val result = getBrandsUseCase()) {
                is DataResult.Success -> {
                    _uiState.value = BrandsUiState.Success(result.data.toImmutableList())
                }
                is DataResult.Error -> {
                    val message = when (val error = result.error) {
                        is DomainError.Server -> error.message ?: "Server error"
                        is DomainError.Network -> error.exception.message ?: "Network error"
                        is DomainError.Unknown -> error.exception.message ?: "Unknown error"
                    }
                    _uiState.value = BrandsUiState.Error(message)
                }
            }
        }
    }
}
