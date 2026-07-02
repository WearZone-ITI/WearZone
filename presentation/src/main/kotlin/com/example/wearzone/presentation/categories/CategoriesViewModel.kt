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

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CategoriesUiState>(CategoriesUiState.Loading)
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<CategoriesUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<CategoriesUiEffect> = _uiEffect.receiveAsFlow()

    private var allCategories: List<CategoryUiModel> = emptyList()

    init { loadCategories() }

    fun handleIntent(intent: CategoriesUiIntent) {
        when (intent) {
            is CategoriesUiIntent.OnSearchQueryChanged -> filterCategories(intent.query)
            is CategoriesUiIntent.OnCategoryClicked -> navigateToProductList(intent.categoryId, intent.categoryName)
            is CategoriesUiIntent.OnRetry -> loadCategories()
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

    private fun navigateToProductList(categoryId: String, categoryName: String) {
        viewModelScope.launch {
            _uiEffect.send(CategoriesUiEffect.NavigateToProductList(categoryId, categoryName))
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