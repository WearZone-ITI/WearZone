package com.example.wearzone.presentation.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────────────────────
// Core Modifier extension
// ─────────────────────────────────────────────────────────────────────────────

fun Modifier.shimmerEffect(
    baseColor: Color = Color.Unspecified,
    highlightColor: Color = Color.Unspecified,
): Modifier = composed {
    val shimmerBase = if (baseColor == Color.Unspecified)
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else baseColor
    val shimmerHighlight = if (highlightColor == Color.Unspecified)
        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f) else highlightColor

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(shimmerBase, shimmerHighlight, shimmerBase),
            start = Offset(translateAnim - 300f, 0f),
            end = Offset(translateAnim + 300f, 0f)
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Reusable shimmer primitives
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .shimmerEffect()
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Product Card Skeleton (used in Home, Product List, Search)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ProductCardSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Product image placeholder
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f),
            cornerRadius = 12.dp
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Brand / title
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(12.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        // Product name
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(14.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        // Price
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(13.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Product Grid Skeleton (2-column, used for Home / Product List loading states)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ProductGridSkeleton(
    modifier: Modifier = Modifier,
    itemCount: Int = 6,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 16.dp,
            vertical = 16.dp
        ),
        userScrollEnabled = false
    ) {
        items(itemCount) {
            ProductCardSkeleton()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Cart Item Skeleton (used for Cart loading state)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CartItemSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ShimmerBox(
            modifier = Modifier.size(90.dp),
            cornerRadius = 12.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.7f).height(14.dp))
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.5f).height(12.dp))
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.3f).height(16.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Full-screen centered Lottie loader (replaces bare CircularProgressIndicator)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FullScreenLoader(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        WearZoneAnimatedLoader(size = 280.dp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Home Screen Skeleton
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HomeScreenSkeleton(modifier: Modifier = Modifier) {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = false
    ) {
        // Greeting
        item {
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.55f).height(28.dp))
            Spacer(modifier = Modifier.height(6.dp))
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.75f).height(16.dp))
        }
        // Search bar
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item { ShimmerBox(modifier = Modifier.fillMaxWidth().height(52.dp), cornerRadius = 28.dp) }
        // Section header
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { ShimmerBox(modifier = Modifier.fillMaxWidth(0.4f).height(20.dp)) }
        // 2-column product cards row x2
        item {
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ProductCardSkeleton(modifier = Modifier.weight(1f))
                ProductCardSkeleton(modifier = Modifier.weight(1f))
            }
        }
        item {
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ProductCardSkeleton(modifier = Modifier.weight(1f))
                ProductCardSkeleton(modifier = Modifier.weight(1f))
            }
        }
        // Brand chips row
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item { ShimmerBox(modifier = Modifier.fillMaxWidth(0.35f).height(20.dp)) }
        item {
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(4) {
                    ShimmerBox(modifier = Modifier.size(72.dp), cornerRadius = 36.dp)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Profile Screen Skeleton
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ProfileSkeleton(modifier: Modifier = Modifier) {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = false
    ) {
        // Avatar + name
        item {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ShimmerBox(modifier = Modifier.size(72.dp), cornerRadius = 36.dp)
                androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShimmerBox(modifier = Modifier.width(140.dp).height(18.dp))
                    ShimmerBox(modifier = Modifier.width(200.dp).height(14.dp))
                }
            }
        }
        // Divider + section header
        item { ShimmerBox(modifier = Modifier.fillMaxWidth().height(1.dp), cornerRadius = 0.dp) }
        item { ShimmerBox(modifier = Modifier.fillMaxWidth(0.4f).height(16.dp)) }
        // 2 recent order cards
        item {
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ShimmerBox(modifier = Modifier.weight(1f).height(90.dp), cornerRadius = 12.dp)
                ShimmerBox(modifier = Modifier.weight(1f).height(90.dp), cornerRadius = 12.dp)
            }
        }
        // Menu rows
        item { ShimmerBox(modifier = Modifier.fillMaxWidth().height(1.dp), cornerRadius = 0.dp) }
        repeat(5) {
            item {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ShimmerBox(modifier = Modifier.size(24.dp), cornerRadius = 4.dp)
                    ShimmerBox(modifier = Modifier.weight(1f).height(16.dp))
                    ShimmerBox(modifier = Modifier.width(60.dp).height(14.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Order History Card Skeleton
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun OrderCardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.5f).height(14.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.3f).height(12.dp))
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) { ShimmerBox(modifier = Modifier.size(60.dp), cornerRadius = 8.dp) }
        }
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(40.dp), cornerRadius = 8.dp)
    }
}

@Composable
fun OrderListSkeleton(modifier: Modifier = Modifier, itemCount: Int = 4) {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 10.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        userScrollEnabled = false
    ) {
        items(itemCount) { OrderCardSkeleton() }
    }
}
