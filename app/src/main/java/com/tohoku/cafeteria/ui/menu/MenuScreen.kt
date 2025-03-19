package com.tohoku.cafeteria.ui.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tohoku.cafeteria.R
import com.tohoku.cafeteria.ui.cart.CartViewModel
import com.tohoku.cafeteria.ui.theme.CafeteriaAITheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    modifier: Modifier = Modifier,
    viewModel: MenuViewModel = viewModel(factory = MenuViewModel.Factory),
    cartViewModel: CartViewModel
) {
    val uiState = viewModel.uiState.value
    val pullRefreshState = rememberPullToRefreshState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.tab_menu)) },
                actions = {
                    IconButton(onClick = { showConfirmDialog = true }) {
                        Icon(Icons.Default.ClearAll, stringResource(R.string.reset_menu))
                    }
                    IconButton(onClick = {  }) {
                        Icon(Icons.Filled.PhotoCamera, stringResource(R.string.upload_menu))
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        // Confirmation dialog
        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text(stringResource(R.string.confirmation)) },
                text = { Text(stringResource(R.string.confirm_reset_menu)) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.resetMenu()
                        showConfirmDialog = false
                    }) {
                        Text(stringResource(R.string.yes))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text(stringResource(R.string.cancel_button))
                    }
                }
            )
        }

        PullToRefreshBox(
            modifier = Modifier.padding(innerPadding),
            state = pullRefreshState,
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refreshMenu() },
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullRefreshState,
                    isRefreshing = uiState.isRefreshing,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }
        ) {
            when {
                uiState.errorMessage != null -> {
                    ErrorScreen(
                        message = uiState.errorMessage,
                        onRetry = { viewModel.refreshMenu() }
                    )
                }
                uiState.menuData != null && uiState.menuData.isEmpty() -> {
                    EmptyScreen(
                        onRefresh = { viewModel.refreshMenu() }
                    )
                }
                else -> {
                    MenuFoodDisplay(
                        categoryData = uiState.menuData,
                        cartViewModel = cartViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.padding_medium)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
        Button(onClick = onRetry) {
            Text(text = stringResource(R.string.try_again))
        }
    }
}

@Composable
fun EmptyScreen(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.padding_medium)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.menu_unavailable),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
        Button(onClick = onRefresh) {
            Text(text = stringResource(R.string.try_again))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorScreenPreview() {
    CafeteriaAITheme {
        ErrorScreen(
            message = stringResource(R.string.unknown_error_occurred),
            onRetry = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyScreenPreview() {
    CafeteriaAITheme {
        EmptyScreen(
            onRefresh = { }
        )
    }
}