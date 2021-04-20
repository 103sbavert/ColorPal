package com.sbeve.colorpal.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

object ImageProvider {

    enum class ImageSource(val uri: Uri) {
        Internal(MediaStore.Images.Media.INTERNAL_CONTENT_URI),
        External(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    }

    suspend fun getAllImages(context: Context, imageSource: ImageSource): ArrayList<Uri> {
        val returnedList = arrayListOf<Uri>()
        val projection = arrayOf(MediaStore.Images.Media._ID)
        var cursor: Cursor?
        withContext(IO) {
            cursor = context.contentResolver.query(imageSource.uri, projection, null, null, null)
        }
        val uriColumnIndex = cursor?.getColumnIndex(projection[0])
        withContext(Default) {
            while (cursor?.moveToNext() == true) {
                val currentImageId = uriColumnIndex?.let { cursor?.getInt(it) }
                val currentImageUri = Uri.withAppendedPath(imageSource.uri, currentImageId.toString())
                returnedList.add(currentImageUri)
            }
        }
        cursor?.close()
        return returnedList
    }
}
