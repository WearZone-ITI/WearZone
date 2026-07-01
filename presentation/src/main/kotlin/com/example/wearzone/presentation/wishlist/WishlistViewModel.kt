package com.example.wearzone.presentation.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wearzone.domain.auth.usecase.GetCurrentUserUseCase
import com.example.wearzone.domain.wishlist.usecase.ObserveWishlistUseCase
import com.example.wearzone.domain.wishlist.usecase.SyncWishlistUseCase
import com.example.wearzone.domain.wishlist.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val observeWishlistUseCase: ObserveWishlistUseCase,
    private val syncWishlistUseCase: SyncWishlistUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<WishlistUiState>(WishlistUiState.Loading)
    val uiState: StateFlow<WishlistUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<WishlistUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private var currentUserId: String? = null

    init {
        checkUserAndLoadWishlist()
    }

    private fun checkUserAndLoadWishlist() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            if (user == null) {
                _uiState.value = WishlistUiState.GuestState
            } else {
                currentUserId = user.uid
                // Start sync in background
                syncWishlistUseCase(user.uid)

                // Observe local database
                observeWishlistUseCase(user.uid)
                    .catch { e ->
                        _uiState.value = WishlistUiState.Error(e.message ?: "Unknown error")
                    }
                    .collectLatest { items ->
                        val currentState = _uiState.value
                        val itemToRemove = if (currentState is WishlistUiState.Success) currentState.itemToRemove else null
                        _uiState.value = WishlistUiState.Success(items.toImmutableList(), itemToRemove)
                    }
            }
        }
    }

    fun handleIntent(intent: WishlistUiIntent) {
        when (intent) {
            is WishlistUiIntent.OnProductClicked -> {
                viewModelScope.launch {
                    _uiEffect.send(WishlistUiEffect.NavigateToProductDetail(intent.productId))
                }
            }
            is WishlistUiIntent.OnRemoveClicked -> {
                val currentState = _uiState.value
                if (currentState is WishlistUiState.Success) {
                    _uiState.value = currentState.copy(itemToRemove = intent.item)
                }
            }
            is WishlistUiIntent.OnCancelRemove -> {
                val currentState = _uiState.value
                if (currentState is WishlistUiState.Success) {
                    _uiState.value = currentState.copy(itemToRemove = null)
                }
            }
            is WishlistUiIntent.OnConfirmRemove -> {
                val currentState = _uiState.value
                if (currentState is WishlistUiState.Success) {
                    val item = currentState.itemToRemove
                    val userId = currentUserId
                    if (item != null && userId != null) {
                        viewModelScope.launch {
                            _uiState.value = currentState.copy(itemToRemove = null)
                            toggleFavoriteUseCase(item, userId)
                        }
                    }
                }
            }
        }
    }
}
