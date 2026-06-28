package com.example.presentation.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.presentation.R

@Composable
fun OnboardingScreen(
    onNavigateToLogin: () -> Unit,
    onShowMessage: (String) -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                OnboardingUiEffect.NavigateToLogin -> onNavigateToLogin()
                is OnboardingUiEffect.ShowError -> onShowMessage(effect.message)
                OnboardingUiEffect.ShowGuestModeUnavailable -> {
                    onShowMessage("Guest mode is not available yet.")
                }
            }
        }
    }

    OnboardingContent(
        uiState = uiState,
        onIntent = viewModel::handleIntent,
    )
}

@Composable
private fun OnboardingContent(
    uiState: OnboardingUiState,
    onIntent: (OnboardingUiIntent) -> Unit,
) {
    val pages = onboardingPages()
    val currentPage = uiState.currentPage.coerceIn(pages.indices)
    val page = pages[currentPage]
    val isFinalPage = currentPage == pages.lastIndex
    val isSaving = uiState is OnboardingUiState.Saving

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(page.imageRes),
            contentDescription = page.imageDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .aspectRatio(0.72f)
                .clip(RoundedCornerShape(28.dp)),
        )
        Spacer(modifier = Modifier.height(28.dp))
        PageIndicators(
            pageCount = pages.size,
            currentPage = currentPage,
            onPageSelected = { onIntent(OnboardingUiIntent.OnPageSelected(it)) },
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = page.title,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = FontFamily.Serif,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 40.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = page.description,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(28.dp))
        if (isFinalPage) {
            FinalPageActions(
                isSaving = isSaving,
                onIntent = onIntent,
            )
        } else {
            PageNavigationActions(
                canGoBack = currentPage > 0,
                onIntent = onIntent,
            )
        }
        if (uiState is OnboardingUiState.Error) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = uiState.message,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PageIndicators(
    pageCount: Int,
    currentPage: Int,
    onPageSelected: (Int) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(pageCount) { index ->
            TextButton(
                onClick = { onPageSelected(index) },
                modifier = Modifier.size(48.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(if (index == currentPage) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == currentPage) {
                                MaterialTheme.colorScheme.onBackground
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            },
                        ),
                )
            }
        }
    }
}

@Composable
private fun PageNavigationActions(
    canGoBack: Boolean,
    onIntent: (OnboardingUiIntent) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(
            onClick = { onIntent(OnboardingUiIntent.OnPreviousPageClicked) },
            enabled = canGoBack,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
        ) {
            Text(text = "Back")
        }
        Button(
            onClick = { onIntent(OnboardingUiIntent.OnNextPageClicked) },
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background,
            ),
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
        ) {
            Text(text = "Next", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun FinalPageActions(
    isSaving: Boolean,
    onIntent: (OnboardingUiIntent) -> Unit,
) {
    Button(
        onClick = { onIntent(OnboardingUiIntent.OnGetStartedClicked) },
        enabled = !isSaving,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.onBackground,
            contentColor = MaterialTheme.colorScheme.background,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.background,
                strokeWidth = 2.dp,
                modifier = Modifier.size(22.dp),
            )
        } else {
            Text(text = "Get Started", fontWeight = FontWeight.SemiBold)
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
    TextButton(
        onClick = { onIntent(OnboardingUiIntent.OnContinueAsGuestClicked) },
        enabled = !isSaving,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "Continue as Guest",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private data class OnboardingPage(
    @DrawableRes val imageRes: Int,
    val imageDescription: String,
    val title: String,
    val description: String,
)

private fun onboardingPages(): List<OnboardingPage> =
    listOf(
        OnboardingPage(
            imageRes = R.drawable.onboarding_style_discovery,
            imageDescription = "Woman wearing a cream outfit",
            title = "Discover Curated Fashion",
            description = "Explore refined looks selected for your everyday wardrobe.",
        ),
        OnboardingPage(
            imageRes = R.drawable.onboarding_shopping_flow,
            imageDescription = "Woman shopping from her phone",
            title = "Shop With Ease",
            description = "Browse collections, compare styles, and keep your favorites close.",
        ),
        OnboardingPage(
            imageRes = R.drawable.onboarding_delivery_moment,
            imageDescription = "Woman opening a fashion package",
            title = "Unbox Your Style",
            description = "Finish onboarding to continue to the Login placeholder and start your WearZone journey.",
        ),
    )
