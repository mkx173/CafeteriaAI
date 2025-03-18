package com.tohoku.cafeteria.ui.recommendation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
    var additionalNotes by remember { mutableStateOf("") }
    var totalPrice by remember { mutableIntStateOf(0) }

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
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(
                    topStart = dimensionResource(R.dimen.round_corner_radius_medium),
                    topEnd = dimensionResource(R.dimen.round_corner_radius_medium),
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                ),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensionResource(R.dimen.padding_medium))
                ) {
                    OutlinedTextField(
                        value = additionalNotes,
                        onValueChange = { newText -> additionalNotes = newText },
                        label = { Text(stringResource(R.string.additional_instructions)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimensionResource(R.dimen.padding_medium))
                            .padding(bottom = dimensionResource(R.dimen.padding_xsmall)),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                    )

                    ListItem(
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent
                        ),
                        headlineContent = {
                            Text(
                                text = stringResource(R.string.total),
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        trailingContent = {
                            Text(
                                text = stringResource(R.string.price, totalPrice),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = dimensionResource(R.dimen.padding_medium))
                        ) {
                            Text(stringResource(R.string.get_new_recommendation))
                        }

                        Button(
                            onClick = { },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = dimensionResource(R.dimen.padding_medium))
                        ) {
                            Text(stringResource(R.string.save_to_history))
                        }
                    }

                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                uiState.isRefreshing -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.recommendation != null -> {
                    val rec = uiState.recommendation
                    RecommendationResultDisplay(
                        viewModel = viewModel,
                        recommendationResponse = rec,
                        getFoodByVariantId =  viewModel::getFoodByVariantId,
                        onTotalPriceCalculated = { price ->
                            totalPrice = price
                        }
                    )
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