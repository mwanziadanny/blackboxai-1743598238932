package com.example.danzygram.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtil {
    private const val JPEG_EXTENSION = ".jpg"
    private const val PNG_EXTENSION = ".png"
    private const val TEMP_PREFIX = "TEMP_"
    private const val DATE_FORMAT = "yyyyMMdd_HHmmss"

    fun createImageFile(context: Context, prefix: String = "IMG"): File {
        val timeStamp = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "${prefix}_${timeStamp}_",
            JPEG_EXTENSION,
            storageDir
        )
    }

    fun createTempFile(context: Context, extension: String): File {
        val timeStamp = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())
        val storageDir = context.cacheDir
        return File.createTempFile(
            "${TEMP_PREFIX}${timeStamp}_",
            extension,
            storageDir
        )
    }

    fun getMimeType(context: Context, uri: Uri): String? {
        return if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            context.contentResolver.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase(Locale.ROOT))
        }
    }

    fun getFileExtension(context: Context, uri: Uri): String {
        val mimeType = getMimeType(context, uri) ?: return ""
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: ""
    }

    fun copyUriToFile(context: Context, uri: Uri, destinationFile: File) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        }
    }

    fun deleteFile(file: File) {
        if (file.exists()) {
            file.delete()
        }
    }

    fun clearDirectory(directory: File) {
        if (directory.exists() && directory.isDirectory) {
            directory.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    clearDirectory(file)
                }
                file.delete()
            }
        }
    }

    fun clearCache(context: Context) {
        try {
            clearDirectory(context.cacheDir)
            context.externalCacheDir?.let { clearDirectory(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getFileSize(file: File): Long {
        return if (file.exists() && file.isFile) {
            file.length()
        } else {
            0
        }
    }

    fun getDirectorySize(directory: File): Long {
        var size: Long = 0
        if (directory.exists() && directory.isDirectory) {
            directory.listFiles()?.forEach { file ->
                size += if (file.isDirectory) {
                    getDirectorySize(file)
                } else {
                    getFileSize(file)
                }
            }
        }
        return size
    }

    fun getCacheSize(context: Context): Long {
        var size = getDirectorySize(context.cacheDir)
        context.externalCacheDir?.let {
            size += getDirectorySize(it)
        }
        return size
    }

    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }

    fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
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

    fun createNoMediaFile(directory: File) {
        File(directory, ".nomedia").createNewFile()
    }

    fun isImageFile(mimeType: String?): Boolean {
        return mimeType?.startsWith("image/") == true
    }

    fun isVideoFile(mimeType: String?): Boolean {
        return mimeType?.startsWith("video/") == true
    }

    fun getUniqueFileName(directory: File, fileName: String): String {
        var uniqueName = fileName
        var counter = 1
        while (File(directory, uniqueName).exists()) {
            val dotIndex = fileName.lastIndexOf(".")
            uniqueName = if (dotIndex != -1) {
                val name = fileName.substring(0, dotIndex)
                val extension = fileName.substring(dotIndex)
                "${name}_${counter++}$extension"
            } else {
                "${fileName}_${counter++}"
            }
        }
        return uniqueName
    }
}