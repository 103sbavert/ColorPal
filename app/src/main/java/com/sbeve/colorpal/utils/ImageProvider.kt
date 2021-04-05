package com.sbeve.colorpal.utils

import android.content.Context
import android.net.Uri
import android.provider.MediaStore

object ImageProvider {
    private val EXTERNAL_CONTENT_BASE_URI: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    private val INTERNAL_CONTENT_BASE_URI: Uri = MediaStore.Images.Media.INTERNAL_CONTENT_URI

    enum class ImageSource {
        Internal,
        External
    }

    fun getAllImages(context: Context, imageSource: ImageSource): ArrayList<Uri> {
        val baseUri = if (imageSource == ImageSource.External) EXTERNAL_CONTENT_BASE_URI else INTERNAL_CONTENT_BASE_URI
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val imageUris = arrayListOf<Uri>()
        val cursor = context.contentResolver.query(baseUri, projection, null, null, null)
        while (cursor?.moveToNext() == true) {
            val columnIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val imageId = cursor.getInt(columnIndex)
            val currentImageUri = Uri.withAppendedPath(baseUri, imageId.toString())
            imageUris.add(currentImageUri)
        }
        cursor?.close()
        return imageUris
    }
}
