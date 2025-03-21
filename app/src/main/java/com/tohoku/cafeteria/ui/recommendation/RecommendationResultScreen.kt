package com.tohoku.cafeteria.ui.recommendation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.tohoku.cafeteria.R
import com.tohoku.cafeteria.ui.cart.CartViewModel
import com.tohoku.cafeteria.ui.menu.ErrorScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationResultScreen(
    modifier: Modifier = Modifier,
    viewModel: RecommendationViewModel = viewModel(factory = RecommendationViewModel.Factory),
    cartViewModel: CartViewModel,
    navController: NavHostController? = null,
    onBackClick: () -> Unit
) {
    val uiState = viewModel.uiState.value
    var additionalNotes by remember { mutableStateOf("") }
    var totalPrice by remember { mutableIntStateOf(0) }

    var onSaveToHistoryClick by remember { mutableStateOf({}) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
                },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            if (!uiState.isRefreshing && (uiState.errorMessage == null || uiState.recommendation != null)) {
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
                                onClick = { viewModel.fetchNewRecommendation(cartViewModel.getCartItems()) },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = dimensionResource(R.dimen.padding_medium))
                            ) {
                                Text(stringResource(R.string.get_new_recommendation))
                            }

                            Button(
                                onClick = onSaveToHistoryClick,
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
                        cartViewModel = cartViewModel,
                        navController = navController,
                        recommendationResponse = rec,
                        getFoodByVariantId =  viewModel::getFoodByVariantId,
                        onTotalPriceCalculated = { price ->
                            totalPrice = price
                        },
                        setSaveToHistoryClick = { action ->
                            onSaveToHistoryClick = action
                        }
                    )
                }
                uiState.errorMessage != null -> {
                    ErrorScreen(
                        message = uiState.errorMessage,
                        onRetry = { viewModel.fetchRecommendation(cartViewModel.getCartItems()) }
                    )
                }
                else -> {
                    ErrorScreen(
                        message = stringResource(R.string.unknown_error_occurred),
                        onRetry = { viewModel.fetchRecommendation(cartViewModel.getCartItems()) }
                    )
                }
            }
        }
    }
}