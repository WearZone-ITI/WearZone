package com.example.wearzone.presentation.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wearzone.domain.category.usecase.GetCategoriesUseCase
import com.example.wearzone.domain.common.Category
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.common.DomainError
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
import com.example.presentation.R // Import الـ Resources
import com.example.wearzone.domain.cart.usecase.ObserveCartItemCountUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val observeCartItemCountUseCase: ObserveCartItemCountUseCase,
) : ViewModel() {


    private val cartItemCount: StateFlow<Int> =
        observeCartItemCountUseCase()
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                0
            )
    private val _uiState = MutableStateFlow<CategoriesUiState>(CategoriesUiState.Loading)
    val uiState: StateFlow<CategoriesUiState> =
        combine(_uiState, cartItemCount) { state, count ->
            when (state) {
                is CategoriesUiState.Success ->
                    state.copy(cartItemCount = count)

                else -> state
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            CategoriesUiState.Loading
        )
    private val _uiEffect = Channel<CategoriesUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<CategoriesUiEffect> = _uiEffect.receiveAsFlow()

    private var allCategories: List<CategoryUiModel> = emptyList()

    init { loadCategories() }

    fun handleIntent(intent: CategoriesUiIntent) {
        when (intent) {
            is CategoriesUiIntent.OnSearchQueryChanged -> filterCategories(intent.query)
            is CategoriesUiIntent.OnCategoryClicked -> navigateToProductList(intent.categoryId, intent.categoryName)
            is CategoriesUiIntent.OnRetry -> loadCategories()
            is CategoriesUiIntent.OnNavigateToCartClick -> navigateToCartScreen()
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { CategoriesUiState.Loading }

            when (val result = getCategoriesUseCase()) {
                is DataResult.Success -> {
                    allCategories = result.data.map { it.toUiModel() }
                    _uiState.update {
                        if (allCategories.isEmpty()) {
                            CategoriesUiState.Empty()
                        } else {
                            CategoriesUiState.Success(allCategories.toImmutableList())
                        }
                    }
                }
                is DataResult.Error -> {
                    val errorPair = result.error.toResDetails()
                    _uiState.update { CategoriesUiState.Error(resId = errorPair.first, args = errorPair.second) }
                }
            }
        }
    }

    private fun filterCategories(query: String) {
        val filtered = if (query.isBlank()) {
            allCategories
        } else {
            allCategories.filter { it.name.contains(query, ignoreCase = true) }
        }
        _uiState.update {
            if (filtered.isEmpty()) {
                CategoriesUiState.Empty(searchQuery = query)
            } else {
                CategoriesUiState.Success(filtered.toImmutableList(), searchQuery = query)
            }
        }
    }

    private fun navigateToProductList(categoryId: Long, categoryName: String) {
        viewModelScope.launch {
            _uiEffect.send(CategoriesUiEffect.NavigateToProductList(categoryId, categoryName))
        }
    }

    private fun navigateToCartScreen(){
        viewModelScope.launch {
            _uiEffect.send(CategoriesUiEffect.NavigateToCart)
        }
    }

    private fun Category.toUiModel() = CategoryUiModel(
        id = id,
        name = title,
        imageUrl = imageUrl ?: "",
    )

    private fun DomainError.toResDetails(): Pair<Int, String?> = when (this) {
        is DomainError.Network -> Pair(R.string.error_network, null)
        is DomainError.Server -> Pair(R.string.error_server, message ?: code.toString())
        is DomainError.Unknown -> Pair(R.string.error_unknown, null)
    }
}
