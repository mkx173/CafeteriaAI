package com.tohoku.cafeteria.ui.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tohoku.cafeteria.R
import com.tohoku.cafeteria.ui.components.MenuFoodCategoryComponent
import com.tohoku.cafeteria.ui.navigation.SnackbarManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    modifier: Modifier = Modifier,
    viewModel: MenuViewModel = viewModel(factory = MenuViewModel.Factory),
    snackbarHostState: SnackbarHostState,
) {
    val uiState = viewModel.uiState.value
    val pullRefreshState = rememberPullToRefreshState()

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            SnackbarManager.showMessage(message)
            viewModel.clearErrorMessage() // Clear the error message after showing it
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.tab_menu)) },
                actions = {
                    IconButton(onClick = { viewModel.refreshMenu() }) {
                        Icon(Icons.Filled.Refresh, "Trigger Refresh")
                    }
                }
            )
        }
    ) { innerPadding ->
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
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                items(uiState.menuData) { menuItem ->
                    MenuFoodCategoryComponent(
                        title = menuItem.category,
                        items = menuItem.items
                    )
                }
            }
        }
    }
}
