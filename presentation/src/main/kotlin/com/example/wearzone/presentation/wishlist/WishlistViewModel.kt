package com.example.wearzone.presentation.wishlist

import androidx.lifecycle.ViewModel
import com.example.presentation.R
import androidx.lifecycle.viewModelScope
import com.example.wearzone.domain.auth.model.AuthAccessState
import com.example.wearzone.domain.auth.usecase.GetAuthAccessStateUseCase
import com.example.wearzone.domain.auth.usecase.GetCurrentUserUseCase
import com.example.wearzone.domain.cart.usecase.ObserveCartItemCountUseCase
import com.example.wearzone.domain.wishlist.usecase.ObserveWishlistUseCase
import com.example.wearzone.domain.wishlist.usecase.SyncWishlistUseCase
import com.example.wearzone.domain.wishlist.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val getAuthAccessStateUseCase: GetAuthAccessStateUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val observeWishlistUseCase: ObserveWishlistUseCase,
    private val syncWishlistUseCase: SyncWishlistUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val observeCartItemCountUseCase: ObserveCartItemCountUseCase,
) : ViewModel() {

    private val cartItemCount: StateFlow<Int> =
        observeCartItemCountUseCase()
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                0
            )
    private val wishlistState = MutableStateFlow<WishlistUiState>(WishlistUiState.Loading)
    val uiState: StateFlow<WishlistUiState> =
        combine(
            wishlistState,
            cartItemCount
        ) { state, count ->

            when (state) {
                is WishlistUiState.Success ->
                    state.copy(cartItemCount = count)

                else -> state
            }

        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            WishlistUiState.Loading
        )
    private val _uiEffect = Channel<WishlistUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private var currentUserId: String? = null

    init {
        checkUserAndLoadWishlist()
    }

    private fun checkUserAndLoadWishlist() {
        viewModelScope.launch {
            if (getAuthAccessStateUseCase() !is AuthAccessState.AuthenticatedCustomer) {
                currentUserId = null
                wishlistState.value = WishlistUiState.GuestState
                return@launch
            }

            val user = getCurrentUserUseCase()
            if (user == null) {
                currentUserId = null
                wishlistState.value = WishlistUiState.GuestState
            } else {
                currentUserId = user.uid
                // Start sync in background
                syncWishlistUseCase(user.uid)

                // Observe local database
                observeWishlistUseCase(user.uid)
                    .catch {
                        wishlistState.value = WishlistUiState.Error(R.string.wishlist_error_loading)
                    }
                    .collectLatest { items ->
                        val currentState = wishlistState.value
                        val itemToRemove = if (currentState is WishlistUiState.Success) currentState.itemToRemove else null
                        wishlistState.value = WishlistUiState.Success(items.toImmutableList(), itemToRemove)
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
                val currentState = wishlistState.value
                if (currentState is WishlistUiState.Success) {
                    wishlistState.value = currentState.copy(itemToRemove = intent.item)
                }
            }
            is WishlistUiIntent.OnCancelRemove -> {
                val currentState = wishlistState.value
                if (currentState is WishlistUiState.Success) {
                    wishlistState.value = currentState.copy(itemToRemove = null)
                }
            }
            is WishlistUiIntent.OnConfirmRemove -> {
                val currentState = wishlistState.value
                if (currentState is WishlistUiState.Success) {
                    val item = currentState.itemToRemove
                    val userId = currentUserId
                    if (item != null && userId != null) {
                        viewModelScope.launch {
                            wishlistState.value = currentState.copy(itemToRemove = null)
                            toggleFavoriteUseCase(item, userId)
                        }
                    }
                }
            }
            is WishlistUiIntent.OnCartClicked -> viewModelScope.launch {
                _uiEffect.send(WishlistUiEffect.NavigateToCart)
            }
        }
    }
}
