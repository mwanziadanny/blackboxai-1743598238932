package com.example.danzygram.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

object PermissionUtil {
    sealed class PermissionResult {
        object Granted : PermissionResult()
        data class Denied(val shouldShowRationale: Boolean) : PermissionResult()
    }

    private val storagePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
        )
    } else {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    private val cameraPermissions = arrayOf(
        Manifest.permission.CAMERA
    )

    fun hasStoragePermissions(context: Context): Boolean {
        return storagePermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun hasCameraPermissions(context: Context): Boolean {
        return cameraPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestStoragePermissions(
        fragment: Fragment,
        launcher: ActivityResultLauncher<Array<String>>
    ) {
        launcher.launch(storagePermissions)
    }

    fun requestCameraPermissions(
        fragment: Fragment,
        launcher: ActivityResultLauncher<Array<String>>
    ) {
        launcher.launch(cameraPermissions)
    }

    fun checkPermissionResult(
        permissions: Array<String>,
        grantResults: IntArray
    ): PermissionResult {
        return if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            PermissionResult.Granted
        } else {
            PermissionResult.Denied(false)
        }
    }

    fun shouldShowStorageRationale(fragment: Fragment): Boolean {
        return storagePermissions.any {
            fragment.shouldShowRequestPermissionRationale(it)
        }
    }

    fun shouldShowCameraRationale(fragment: Fragment): Boolean {
        return cameraPermissions.any {
            fragment.shouldShowRequestPermissionRationale(it)
        }
    }

    fun openAppSettings(context: Context) {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            context.startActivity(this)
        }
    }

    fun checkPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun checkPermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.all { checkPermission(context, it) }
    }

    fun shouldShowRationale(fragment: Fragment, permission: String): Boolean {
        return fragment.shouldShowRequestPermissionRationale(permission)
    }

    fun shouldShowRationale(fragment: Fragment, permissions: Array<String>): Boolean {
        return permissions.any { shouldShowRationale(fragment, it) }
    }

    fun getRequiredPermissions(): Array<String> {
        return arrayOf(
            *storagePermissions,
            *cameraPermissions
        )
    }

    fun getMissingPermissions(context: Context): Array<String> {
        return getRequiredPermissions().filter {
            !checkPermission(context, it)
        }.toTypedArray()
    }

    fun hasAllRequiredPermissions(context: Context): Boolean {
        return checkPermissions(context, getRequiredPermissions())
    }
}