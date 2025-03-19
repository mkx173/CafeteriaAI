package com.tohoku.cafeteria.ui.menu

import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tohoku.cafeteria.R
import com.tohoku.cafeteria.ui.cart.CartViewModel
import com.tohoku.cafeteria.ui.theme.CafeteriaAITheme
import com.tohoku.cafeteria.util.ToastManager
import java.io.File

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

    var showPhotoDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Temporary URI for camera
    val tempUri = remember {
        mutableStateOf<Uri?>(null)
    }

    // Register for image picker
    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            viewModel.uploadMenuImage(uri)
        }
    }

    // Register for camera
    val takePhoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempUri.value?.let { uri ->
                viewModel.uploadMenuImage(uri)
            }
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.tab_menu)) },
                actions = {
                    IconButton(onClick = { showConfirmDialog = true }) {
                        Icon(Icons.Default.ClearAll, stringResource(R.string.reset_menu))
                    }
                    IconButton(onClick = { showPhotoDialog = true }) {
                        Icon(Icons.Filled.PhotoCamera, stringResource(R.string.upload_image))
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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        ErrorScreen(
                            message = uiState.errorMessage,
                            onRetry = { viewModel.refreshMenu() }
                        )
                    }
                }
                uiState.menuData != null && uiState.menuData.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        EmptyScreen(
                            onRefresh = { viewModel.refreshMenu() },
                            onUpload = { showPhotoDialog = true }
                        )
                    }
                }
                else -> {
                    MenuFoodDisplay(
                        categoryData = uiState.menuData,
                        cartViewModel = cartViewModel
                    )
                }
            }
        }

        // Photo options dialog
        if (showPhotoDialog) {
            AlertDialog(
                onDismissRequest = { showPhotoDialog = false },
                title = { Text(stringResource(R.string.choose_image_source)) },
                text = { Text(stringResource(R.string.select_an_image_from_gallery_or_take_a_new_photo)) },
                confirmButton = {
                    TextButton(onClick = {
                        try {
                            // Create a temporary file and URI for the camera
                            val photoFile = File.createTempFile(
                                "IMG_",
                                ".jpg",
                                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                            )
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.applicationContext.packageName}.provider",
                                photoFile
                            )
                            tempUri.value = uri
                            takePhoto.launch(uri)
                            showPhotoDialog = false
                        } catch (e: Exception) {
                            ToastManager.showMessage(e.message ?: context.getString(R.string.unknown_error_occurred))
                        }
                    }) {
                        Text(stringResource(R.string.take_photo))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        showPhotoDialog = false
                    }) {
                        Text(stringResource(R.string.select_from_gallery))
                    }
                }
            )
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
fun EmptyScreen(onRefresh: () -> Unit, onUpload: () -> Unit) {
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
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_xsmall)))
        Button(onClick = onUpload) {
            Text(text = stringResource(R.string.upload_image))
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
            onRefresh = { },
            onUpload = { }
        )
    }
}