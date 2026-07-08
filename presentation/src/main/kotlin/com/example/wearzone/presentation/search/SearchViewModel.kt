package com.example.wearzone.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wearzone.domain.cart.usecase.ObserveCartItemCountUseCase
import com.example.wearzone.domain.common.Category
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.common.DomainError
import com.example.wearzone.domain.product.model.Brand
import com.example.wearzone.domain.product.model.Product
import com.example.wearzone.domain.product.usecase.GetProductsUseCase
import com.example.wearzone.domain.product.usecase.SearchProductsUseCase
import com.example.wearzone.domain.search.model.SearchFilters
import com.example.wearzone.domain.search.usecase.ClearRecentSearchesUseCase
import com.example.wearzone.domain.search.usecase.GetRecentSearchesUseCase
import com.example.wearzone.domain.search.usecase.SaveRecentSearchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchProductsUseCase: SearchProductsUseCase,
    private val getProductsUseCase: GetProductsUseCase,
    private val getRecentSearchesUseCase: GetRecentSearchesUseCase,
    private val saveRecentSearchUseCase: SaveRecentSearchUseCase,
    private val clearRecentSearchesUseCase: ClearRecentSearchesUseCase,
    private val observeCartItemCountUseCase: ObserveCartItemCountUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<SearchUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private val queryChanges = MutableStateFlow("")
    private var searchJob: Job? = null

    init {
        observeRecentSearches()
        observeQueryChanges()
        loadInitialSearchContent()
        observeCart()
    }

    fun handleIntent(intent: SearchUiIntent) {
        when (intent) {
            is SearchUiIntent.OnQueryChanged -> updateQuery(intent.query)
            is SearchUiIntent.OnSearchSubmitted -> submitSearch()
            is SearchUiIntent.OnRecentSearchClicked -> selectRecentSearch(intent.query)
            is SearchUiIntent.OnClearRecentSearches -> clearRecentSearches()
            is SearchUiIntent.OnFilterClicked -> setFilterSheetVisible(true)
            is SearchUiIntent.OnDismissFilters -> setFilterSheetVisible(false)
            is SearchUiIntent.OnBrandSelected -> updateBrand(intent.brandTitle)
            is SearchUiIntent.OnCategorySelected -> updateCategory(intent.categoryTitle)
            is SearchUiIntent.OnMinPriceChanged -> updateMinPrice(intent.minPrice)
            is SearchUiIntent.OnMaxPriceChanged -> updateMaxPrice(intent.maxPrice)
            is SearchUiIntent.OnApplyFilters -> applyFilters()
            is SearchUiIntent.OnResetFilters -> resetFilters()
            is SearchUiIntent.OnRetry -> searchProducts()
            is SearchUiIntent.OnProductClicked -> navigateToProduct(intent.productId)
        }
    }

    private fun observeRecentSearches() {
        getRecentSearchesUseCase()
            .onEach { searches ->
                _uiState.update { state ->
                    state.copy(recentSearches = searches.map { it.query }.toImmutableList())
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeQueryChanges() {
        queryChanges
            .drop(1)
            .debounce(300)
            .onEach { searchProducts() }
            .launchIn(viewModelScope)
    }

    private fun loadInitialSearchContent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, hasError = false, errorMessage = null) }

            val brandsDeferred = async { getProductsUseCase.getBrands() }
            val categoriesDeferred = async { getProductsUseCase.getCategories() }
            val suggestedProductsDeferred = async {
                getProductsUseCase.getProductsPreview(INITIAL_SEARCH_PRODUCT_LIMIT)
            }

            val brandsResult = brandsDeferred.await()
            val categoriesResult = categoriesDeferred.await()
            val suggestedProducts = suggestedProductsDeferred.await().toProductSearchModels()

            _uiState.update { state ->
                val shouldShowSuggestedProducts = state.query.isBlank() && !state.hasActiveFilters()
                state.copy(
                    brands = brandsResult.toBrandOptions(),
                    categories = categoriesResult.toCategoryOptions(),
                    suggestedProducts = suggestedProducts,
                    products = if (shouldShowSuggestedProducts) suggestedProducts else state.products,
                    isLoading = false,
                )
            }
        }
    }

    private fun updateQuery(query: String) {
        _uiState.update { state ->
            state.copy(
                query = query,
                hasSearched = query.isNotBlank() || state.hasActiveFilters(),
            )
        }
        queryChanges.value = query
    }

    private fun submitSearch() {
        viewModelScope.launch {
            _uiState.value.query.trim().takeIf { it.isNotBlank() }?.let { saveRecentSearchUseCase(it) }
            searchProducts()
        }
    }

    private fun selectRecentSearch(query: String) {
        _uiState.update { it.copy(query = query, hasSearched = true) }
        queryChanges.value = query
        viewModelScope.launch { saveRecentSearchUseCase(query) }
    }

    private fun clearRecentSearches() {
        viewModelScope.launch { clearRecentSearchesUseCase() }
    }

    private fun setFilterSheetVisible(isVisible: Boolean) {
        _uiState.update { it.copy(isFilterSheetVisible = isVisible) }
    }

    private fun updateBrand(brandTitle: String?) {
        _uiState.update { it.copy(selectedBrandTitle = brandTitle) }
    }

    private fun updateCategory(categoryTitle: String?) {
        _uiState.update { it.copy(selectedCategoryTitle = categoryTitle) }
    }

    private fun updateMinPrice(minPrice: String) {
        _uiState.update { it.copy(minPrice = minPrice.filterPriceInput()) }
    }

    private fun updateMaxPrice(maxPrice: String) {
        _uiState.update { it.copy(maxPrice = maxPrice.filterPriceInput()) }
    }

    private fun applyFilters() {
        _uiState.update { it.copy(isFilterSheetVisible = false, hasSearched = true) }
        searchProducts()
    }

    private fun resetFilters() {
        _uiState.update {
            it.copy(
                selectedBrandTitle = null,
                selectedCategoryTitle = null,
                minPrice = "",
                maxPrice = "",
                isFilterSheetVisible = false,
            )
        }
        searchProducts()
    }

    private fun searchProducts() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val filters = _uiState.value.toFilters()
            if (!filters.hasActiveCriteria()) {
                _uiState.update {
                    it.copy(
                        products = it.suggestedProducts,
                        isLoading = false,
                        hasSearched = false,
                        hasError = false,
                        errorMessage = null,
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, hasSearched = true, hasError = false, errorMessage = null) }

            when (val result = searchProductsUseCase(filters)) {
                is DataResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            hasError = true,
                            errorMessage = result.errorMessageOrDefault(),
                        )
                    }
                }

                is DataResult.Success -> {
                    _uiState.update {
                        it.copy(
                            products = result.data.map { product -> product.toUiModel() }.toImmutableList(),
                            isLoading = false,
                            hasError = false,
                            errorMessage = null,
                        )
                    }
                }
            }
        }
    }

    private fun navigateToProduct(productId: String) {
        viewModelScope.launch { _uiEffect.send(SearchUiEffect.NavigateToProductDetail(productId)) }
    }

    private fun SearchUiState.toFilters(): SearchFilters = SearchFilters(
        query = query,
        minPrice = minPrice.toDoubleOrNull(),
        maxPrice = maxPrice.toDoubleOrNull(),
        brandTitle = selectedBrandTitle,
        categoryTitle = selectedCategoryTitle,
    )

    private fun SearchUiState.hasActiveFilters(): Boolean =
        selectedBrandTitle != null ||
                selectedCategoryTitle != null ||
                minPrice.toDoubleOrNull() != null ||
                maxPrice.toDoubleOrNull() != null

    private fun SearchFilters.hasActiveCriteria(): Boolean =
        query.isNotBlank() ||
                brandTitle != null ||
                categoryTitle != null ||
                minPrice != null ||
                maxPrice != null

    private fun DataResult<List<Product>>.toProductSearchModels() = when (this) {
        is DataResult.Error -> persistentListOf<ProductSearchUiModel>()
        is DataResult.Success -> data.map { product -> product.toUiModel() }.toImmutableList()
    }

    private fun Product.toUiModel(): ProductSearchUiModel = ProductSearchUiModel(
        id = id,
        title = title,
        vendor = vendor,
        basePriceEgp = price,
        imageUrl = imageUrl,
    )

    private fun DataResult<List<Brand>>.toBrandOptions() = when (this) {
        is DataResult.Error -> emptyList<SearchFilterOptionUiModel>().toImmutableList()
        is DataResult.Success -> data
            .filter { it.title.isNotBlank() }
            .distinctBy { it.title.trim().lowercase() }
            .map { brand ->
                SearchFilterOptionUiModel(
                    id = brand.title,
                    title = brand.title,
                )
            }
            .toImmutableList()
    }

    private fun DataResult<List<Category>>.toCategoryOptions() = when (this) {
        is DataResult.Error -> emptyList<SearchFilterOptionUiModel>().toImmutableList()
        is DataResult.Success -> data.map { SearchFilterOptionUiModel(it.id.toString(), it.title) }.toImmutableList()
    }

    private fun DataResult.Error.errorMessageOrDefault(): String = when (val domainError = error) {
        is DomainError.Network -> "search_error_network"
        is DomainError.Server -> domainError.message ?: "search_error_server"
        is DomainError.Unknown -> "search_error_unknown"
    }

    private fun String.filterPriceInput(): String {
        return filterIndexed { index, char -> char.isDigit() || (char == '.' && indexOf('.') == index) }
    }

    private companion object {
        const val INITIAL_SEARCH_PRODUCT_LIMIT = 12
    }

    private fun observeCart() {
        observeCartItemCountUseCase()
            .onEach { count ->
                _uiState.update { state ->
                    state.copy(cartItemCount = count)
                }
            }
            .launchIn(viewModelScope)
    }
}
