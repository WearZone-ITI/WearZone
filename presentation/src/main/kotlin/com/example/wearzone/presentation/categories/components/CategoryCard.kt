package com.example.wearzone.presentation.categories.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.presentation.R
import com.example.wearzone.presentation.categories.CategoryUiModel
import com.example.wearzone.presentation.common.theme.AppTheme

@Composable
fun CategoryCard(
    category: CategoryUiModel,
    isFullWidth: Boolean,
    onCategoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardHeight = if (isFullWidth) 170.dp else 200.dp
    val themeColors = AppTheme.colors

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .padding(vertical = 10.dp, horizontal = 6.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                clip = false,
                ambientColor = themeColors.textPrimary.copy(alpha = 0.2f),
                spotColor = themeColors.textPrimary.copy(alpha = 0.4f)
            )
            .graphicsLayer {
                translationY = -4f
            },
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, themeColors.border.copy(alpha = 0.15f)),
        colors = CardDefaults.cardColors(
            containerColor = themeColors.surfaceVariant
        ),
        onClick = onCategoryClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = category.imageUrl,
                contentDescription = category.name,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.wearzone_img),
                error = painterResource(R.drawable.wearzone_img),
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.1f),
                                Color.Black.copy(alpha = 0.4f),
                                Color.Black.copy(alpha = 0.85f)
                            ),
                            startY = 60f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = category.name.uppercase(),
                    color = Color.White,
                    fontSize = if (isFullWidth) 24.sp else 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                )

                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(3.dp)
                        .background(themeColors.accent, RoundedCornerShape(2.dp))
                )
            }
        }
    }
}