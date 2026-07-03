package com.example.wearzone.presentation.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.presentation.R
import com.example.wearzone.domain.product.model.Product
import com.example.wearzone.presentation.common.theme.AppTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun PromoAdsCarousel(
    products: ImmutableList<Product>,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier,
    autoScrollDelayMillis: Long = 4000L,
    discountPercent: Int = 20
) {
    if (products.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { products.size })

    LaunchedEffect(pagerState, products) {
        while (true) {
            delay(autoScrollDelayMillis)
            if (products.size > 0) {
                val nextPage = (pagerState.currentPage + 1) % products.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    val colors = AppTheme.colors

    val premiumGradient = Brush.horizontalGradient(
        colors = listOf(colors.accent, colors.error)
    )

    val bottomScrim = Brush.verticalGradient(
        colors = listOf(Color.Transparent, colors.scrim.copy(alpha = 0.85f)),
        startY = 200f
    )

    val glassBackground = colors.textPrimary.copy(alpha = 0.12f)
    val glassBorderColor = colors.border.copy(alpha = 0.35f)
    val glassTextColor = Color.White

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 8.dp
        ) { page ->
            val product = products[page]
            val realPrice = product.price
            val strikedPrice = realPrice * (1 + discountPercent / 100.0)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = colors.scrim.copy(alpha = 0.3f),
                        spotColor = colors.accent.copy(alpha = 0.4f)
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(colors.card)
                    .clickable { onProductClick(product) }
            ) {
                val placeholderPainter = painterResource(R.drawable.wearzone_img)
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.title,
                    contentScale = ContentScale.Crop,
                    placeholder = placeholderPainter,
                    error = placeholderPainter,
                    fallback = placeholderPainter,
                    modifier = Modifier.fillMaxSize()
                )

                Box(modifier = Modifier.fillMaxSize().background(bottomScrim))

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = colors.error.copy(alpha = 0.4f)
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(premiumGradient)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "-$discountPercent%",
                            color = colors.onAccent,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.scrim.copy(alpha = 0.35f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.promo_discount_off).uppercase(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(glassBackground)
                        .border(1.dp, glassBorderColor, RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.promo_limited_time_sale).uppercase(),
                        color = glassTextColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = product.title,
                        color = glassTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1
                    )

                    Spacer(Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format(Locale.US, "%.0f", realPrice),
                                color = glassTextColor,
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = product.currencyCode,
                                color = colors.accent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = String.format(Locale.US, "%.0f %s", strikedPrice, product.currencyCode),
                                color = glassTextColor.copy(alpha = 0.5f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                textDecoration = TextDecoration.LineThrough,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(glassBackground)
                                .border(1.dp, glassBorderColor, RoundedCornerShape(14.dp))
                                .clickable { onProductClick(product) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.promo_shop_now),
                                color = glassTextColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(products.size) { index ->
                val isSelected = pagerState.currentPage == index
                val width by animateDpAsState(
                    targetValue = if (isSelected) 20.dp else 6.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "indicator_width"
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(6.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) premiumGradient
                            else Brush.linearGradient(
                                listOf(
                                    colors.divider.copy(alpha = 0.6f),
                                    colors.divider.copy(alpha = 0.6f)
                                )
                            )
                        )
                )
            }
        }
    }
}