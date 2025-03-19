package com.tohoku.cafeteria.ui.menu

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tohoku.cafeteria.CafeteriaApplication
import com.tohoku.cafeteria.R
import com.tohoku.cafeteria.data.repository.FoodRepository
import com.tohoku.cafeteria.domain.model.FoodCategory
import com.tohoku.cafeteria.util.ToastManager
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

data class MenuUiState(
    val menuData: List<FoodCategory>? = null,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val isUploading: Boolean = false
)

class MenuViewModel(
    private val foodRepository: FoodRepository,
    private val application: CafeteriaApplication
) : ViewModel() {
    private val _uiState = mutableStateOf(MenuUiState())
    val uiState: State<MenuUiState> = _uiState

    init {
        refreshMenu()
    }

    fun refreshMenu()  {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    menuData = foodRepository.getMenu(),
                    errorMessage = null,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: application.getString(R.string.unknown_error_occurred),
                )
                ToastManager.showMessage(
                    e.message ?: application.getString(R.string.unknown_error_occurred)
                )
            } finally {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }

    fun resetMenu() {
        // Show progress indicator
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        viewModelScope.launch {
            try {
                // Call the reset menu API from the repository
                foodRepository.resetMenu()
                // Optionally, refresh the menu to reflect the changes
                refreshMenu()
            } catch (e: Exception) {
                // Update error state and show an error message if needed
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: application.getString(R.string.unknown_error_occurred)
                )
                ToastManager.showMessage(
                    e.message ?: application.getString(R.string.unknown_error_occurred)
                )
            } finally {
                // Hide the progress indicator
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }

    // Add this function to MenuViewModel
    fun uploadMenuImage(uri: Uri, method: String = "GoogleOCR") {
        _uiState.value = _uiState.value.copy(isUploading = true)
        viewModelScope.launch {
            try {
                // Get content resolver from application
                val contentResolver = application.contentResolver

                // Create file from Uri
                val inputStream = contentResolver.openInputStream(uri)
                val fileName = getFileName(contentResolver, uri) ?: "upload.jpg"
                val file = File(application.cacheDir, fileName)
                file.outputStream().use { outputStream ->
                    inputStream?.copyTo(outputStream)
                }
                inputStream?.close()

                // Create RequestBody and MultipartBody.Part
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image_upload", file.name, requestFile)
                val methodRequestBody = method.toRequestBody("text/plain".toMediaTypeOrNull())

                // Upload the image
                foodRepository.uploadMenuImage(imagePart, methodRequestBody)

                // Show success message
                ToastManager.run { showMessage(application.getString(R.string.upload_successful)) }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: application.getString(R.string.unknown_error_occurred)
                )
                ToastManager.showMessage(
                    e.message ?: application.getString(R.string.unknown_error_occurred)
                )
            } finally {
                _uiState.value = _uiState.value.copy(isUploading = false)
            }

            refreshMenu()
        }
    }

    // Helper function to get file name from URI
    private fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (columnIndex != -1) {
                        result = cursor.getString(columnIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                result = result?.substring(cut!! + 1)
            }
        }
        return result
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as CafeteriaApplication)
                val foodRepository = application.appContainer.foodRepository
                MenuViewModel(foodRepository = foodRepository, application = application)
            }
        }
    }
}