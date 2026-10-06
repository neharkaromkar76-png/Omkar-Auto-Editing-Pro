package com.example.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class CameraCaptureState(
  val hasPermissions: Boolean = false,
  val isCameraInitialized: Boolean = false,
  val isRecording: Boolean = false,
  val lensFacing: Int = CameraSelector.LENS_FACING_BACK,
  val errorMessage: String? = null
)

class VideoCaptureViewModel : ViewModel() {

  private val _captureState = MutableStateFlow(CameraCaptureState())
  val captureState: StateFlow<CameraCaptureState> = _captureState.asStateFlow()

  fun checkAndSetPermissions(context: Context) {
    val cameraGranted = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
    val audioGranted = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    val granted = cameraGranted && audioGranted
    _captureState.value = _captureState.value.copy(hasPermissions = granted)
  }

  fun updatePermissionsGranted(granted: Boolean) {
    _captureState.value = _captureState.value.copy(hasPermissions = granted)
  }

  suspend fun initializeCamera(context: Context): ProcessCameraProvider? =
    suspendCancellableCoroutine { continuation ->
      val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
      cameraProviderFuture.addListener({
        try {
          val provider = cameraProviderFuture.get()
          _captureState.value = _captureState.value.copy(
            isCameraInitialized = true,
            errorMessage = null
          )
          continuation.resume(provider)
        } catch (e: Exception) {
          _captureState.value = _captureState.value.copy(
            isCameraInitialized = false,
            errorMessage = "Camera initialization failed: ${e.message}"
          )
          continuation.resume(null)
        }
      }, ContextCompat.getMainExecutor(context))
    }

  fun toggleLensFacing() {
    val current = _captureState.value.lensFacing
    val next = if (current == CameraSelector.LENS_FACING_BACK) {
      CameraSelector.LENS_FACING_FRONT
    } else {
      CameraSelector.LENS_FACING_BACK
    }
    _captureState.value = _captureState.value.copy(lensFacing = next)
  }
}
