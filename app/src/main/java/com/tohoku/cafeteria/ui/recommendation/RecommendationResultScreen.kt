package com.tohoku.cafeteria.ui.recommendation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tohoku.cafeteria.R
import com.tohoku.cafeteria.ui.menu.EmptyScreen
import com.tohoku.cafeteria.ui.menu.ErrorScreen
import com.tohoku.cafeteria.ui.menu.MenuFoodDisplay
import com.tohoku.cafeteria.util.ToastManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationResultScreen(
    modifier: Modifier = Modifier,
    viewModel: RecommendationViewModel = viewModel(factory = RecommendationViewModel.Factory),
    onBackClick: () -> Unit
) {
    val uiState = viewModel.uiState.value

    LaunchedEffect(uiState.isErrorNew) {
        if (uiState.isErrorNew) {
            uiState.errorMessage?.let { message ->
                ToastManager.showMessage(message)
                viewModel.clearNewErrorFlag() // Only show toast once
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.tab_recommendation)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
            when {
                uiState.isRefreshing -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.recommendation != null -> {
                    val rec = uiState.recommendation
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Additional Notes: ${rec.additionalNotes}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Recommended Meal Detail:",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(text = rec.recommendedMealDetail)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nutrition Details:",
                            style = MaterialTheme.typography.titleSmall
                        )
                        rec.detailNutritions.forEach { detail ->
                            Text(text = "• $detail")
                        }
                    }
                }
                uiState.errorMessage != null -> {
                    ErrorScreen(
                        message = uiState.errorMessage,
                        onRetry = { viewModel.fetchRecommendation() }
                    )
                }
                else -> {
                    ErrorScreen(
                        message = stringResource(R.string.unknown_error_occurred),
                        onRetry = { viewModel.fetchRecommendation() }
                    )
                }
            }
        }
    }
}