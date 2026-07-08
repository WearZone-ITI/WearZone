package com.example.wearzone.presentation.common

import android.graphics.Rect
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView

@Composable
fun rememberKeyboardVisibility(): State<Boolean> {
    val view = LocalView.current
    val density = LocalDensity.current
    val imeVisible = WindowInsets.ime.getBottom(density) > 0
    val isKeyboardVisible = remember { mutableStateOf(imeVisible) }

    LaunchedEffect(imeVisible) {
        if (imeVisible != isKeyboardVisible.value) {
            isKeyboardVisible.value = imeVisible
        }
    }

    DisposableEffect(view, density, imeVisible) {
        val visibleFrame = Rect()
        val keyboardThresholdPx = (80 * density.density).toInt()
        val listener = android.view.ViewTreeObserver.OnGlobalLayoutListener {
            val rootView = view.rootView
            rootView.getWindowVisibleDisplayFrame(visibleFrame)
            val rootHeight = rootView.height
            val visibleHeight = visibleFrame.height()
            val heightDiff = rootHeight - visibleHeight
            val threshold = maxOf(keyboardThresholdPx, (rootHeight * 0.15f).toInt())
            val visibleByFrame = rootHeight > 0 && heightDiff > threshold
            val visible = imeVisible || visibleByFrame
            if (visible != isKeyboardVisible.value) {
                isKeyboardVisible.value = visible
            }
        }

        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        listener.onGlobalLayout()

        onDispose {
            if (view.viewTreeObserver.isAlive) {
                view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
            }
        }
    }

    return isKeyboardVisible
}

@Composable
fun isImeVisible(): Boolean = rememberKeyboardVisibility().value
