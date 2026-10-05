package com.example.engine

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportStorageManager {

  private const val TAG = "ExportStorageManager"
  const val EXPORT_DIRECTORY_NAME = "CutsZoom AI"
  const val RELATIVE_MOVIES_PATH = "Movies/CutsZoom AI"

  /**
   * Generates a collision-safe, sanitized filename such as CutsZoom_Edit_20261005_194500.mp4
   */
  fun generateSafeExportFileName(prefix: String = "CutsZoom_Edit"): String {
    val timeFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    val timestamp = timeFormat.format(Date())
    return "${prefix}_${timestamp}.mp4"
  }

  /**
   * Returns a persistent app-accessible directory for temporary renders.
   */
  fun getTempExportDir(context: Context): File {
    val dir = File(context.filesDir, "exports")
    if (!dir.exists()) {
      dir.mkdirs()
    }
    return dir
  }

  /**
   * Persists a rendered MP4 into Android's public Movies/CutsZoom AI collection
   * using MediaStore on Android 10+ (scoped storage) or public directory on older versions.
   */
  fun saveVideoToMediaStore(
    context: Context,
    sourceFile: File,
    displayName: String = generateSafeExportFileName()
  ): Result<Uri> {
    if (!sourceFile.exists() || sourceFile.length() <= 0L) {
      val err = "Source render file does not exist or is empty: ${sourceFile.absolutePath}"
      Log.e(TAG, err)
      return Result.failure(IllegalStateException(err))
    }

    Log.i(TAG, "Persisting video (${sourceFile.length()} bytes) to MediaStore as '$displayName'...")

    return try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
          put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
          put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
          put(MediaStore.Video.Media.RELATIVE_PATH, RELATIVE_MOVIES_PATH)
          put(MediaStore.Video.Media.IS_PENDING, 1)
          val nowSec = System.currentTimeMillis() / 1000
          put(MediaStore.Video.Media.DATE_ADDED, nowSec)
          put(MediaStore.Video.Media.DATE_MODIFIED, nowSec)
        }

        val volumeUri = try {
          MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } catch (_: Exception) {
          MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val itemUri = resolver.insert(volumeUri, contentValues)
          ?: return Result.failure(IllegalStateException("Failed to insert video metadata into MediaStore."))

        // Stream file contents into MediaStore destination
        try {
          resolver.openOutputStream(itemUri, "w")?.use { outputStream ->
            FileInputStream(sourceFile).use { inputStream ->
              val buffer = ByteArray(64 * 1024)
              var bytesRead: Int
              while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
              }
              outputStream.flush()
            }
          } ?: run {
            resolver.delete(itemUri, null, null)
            return Result.failure(IllegalStateException("Failed to open output stream for MediaStore URI: $itemUri"))
          }

          // Complete the transaction by clearing IS_PENDING flag
          contentValues.clear()
          contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
          resolver.update(itemUri, contentValues, null, null)

          // Verify the written media URI is readable and non-empty
          val verifiedLength = resolver.openAssetFileDescriptor(itemUri, "r")?.use { it.length } ?: 0L
          if (verifiedLength <= 0L) {
            Log.w(TAG, "MediaStore file length could not be verified via asset descriptor, checking alternative stream.")
          }

          Log.i(TAG, "Successfully persisted to MediaStore: $itemUri ($verifiedLength bytes)")
          Result.success(itemUri)
        } catch (writeEx: Exception) {
          try { resolver.delete(itemUri, null, null) } catch (_: Exception) {}
          throw writeEx
        }
      } else {
        // Android 9 and below legacy path
        val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        val cutsZoomDir = File(moviesDir, EXPORT_DIRECTORY_NAME).apply { mkdirs() }
        val destFile = File(cutsZoomDir, displayName)

        FileInputStream(sourceFile).use { input ->
          FileOutputStream(destFile).use { output ->
            input.copyTo(output)
            output.flush()
          }
        }

        // Trigger MediaScanner so the file is immediately visible in gallery/files
        MediaScannerConnection.scanFile(
          context,
          arrayOf(destFile.absolutePath),
          arrayOf("video/mp4"),
          null
        )

        val fileProviderUri = getFileProviderUri(context, destFile)
        Result.success(fileProviderUri)
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error persisting video to MediaStore: ${e.message}", e)
      Result.failure(e)
    }
  }

  /**
   * Generates a safe, granted content:// URI for a file using FileProvider.
   */
  fun getFileProviderUri(context: Context, file: File): Uri {
    val authority = "${context.packageName}.fileprovider"
    return FileProvider.getUriForFile(context, authority, file)
  }

  /**
   * Resolves the best available shareable content:// URI for an exported video.
   * Never returns a raw file:// URI.
   */
  fun getShareableContentUri(context: Context, localFile: File, mediaStoreUriString: String?): Uri {
    if (!mediaStoreUriString.isNullOrBlank()) {
      try {
        val parsedUri = Uri.parse(mediaStoreUriString)
        if (parsedUri.scheme == "content") {
          // Verify that this content URI is actually readable
          context.contentResolver.openAssetFileDescriptor(parsedUri, "r")?.use { afd ->
            if (afd.length > 0) {
              return parsedUri
            }
          } ?: return parsedUri
        }
      } catch (e: Exception) {
        Log.w(TAG, "MediaStore URI was not readable, falling back to FileProvider: ${e.message}")
      }
    }

    // Always fallback to FileProvider content:// URI
    return getFileProviderUri(context, localFile)
  }

  /**
   * Shares the exported video to external applications (WhatsApp, Drive, Files, Quick Share)
   * with explicit temporary read permissions granted to all matching targets.
   */
  fun shareVideo(context: Context, contentUri: Uri, chooserTitle: String = "Share CutsZoom Video"): Result<Unit> {
    return try {
      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "video/mp4"
        putExtra(Intent.EXTRA_STREAM, contentUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        putExtra(Intent.EXTRA_SUBJECT, "CutsZoom AI Video")
      }

      val chooser = Intent.createChooser(shareIntent, chooserTitle).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }

      // Explicitly grant URI permission to all candidate package handlers
      val resInfoList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.packageManager.queryIntentActivities(
          shareIntent,
          PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
        )
      } else {
        @Suppress("DEPRECATION")
        context.packageManager.queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY)
      }

      for (resolveInfo in resInfoList) {
        val packageName = resolveInfo.activityInfo.packageName
        try {
          context.grantUriPermission(packageName, contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: Exception) {}
      }

      context.startActivity(chooser)
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to share video URI $contentUri: ${e.message}", e)
      Result.failure(e)
    }
  }

  /**
   * Opens or previews the exported video in the user's default video player or files app.
   */
  fun openVideo(context: Context, contentUri: Uri): Result<Unit> {
    return try {
      val viewIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(contentUri, "video/mp4")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }

      val chooser = Intent.createChooser(viewIntent, "Open Video").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }

      val resInfoList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.packageManager.queryIntentActivities(
          viewIntent,
          PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
        )
      } else {
        @Suppress("DEPRECATION")
        context.packageManager.queryIntentActivities(viewIntent, PackageManager.MATCH_DEFAULT_ONLY)
      }

      for (resolveInfo in resInfoList) {
        val packageName = resolveInfo.activityInfo.packageName
        try {
          context.grantUriPermission(packageName, contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: Exception) {}
      }

      context.startActivity(chooser)
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to open video URI $contentUri: ${e.message}", e)
      Result.failure(e)
    }
  }
}
