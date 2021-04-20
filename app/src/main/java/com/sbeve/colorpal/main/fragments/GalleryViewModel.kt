package com.sbeve.colorpal.main.fragments

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sbeve.colorpal.utils.ImageProvider
import kotlinx.coroutines.launch

class GalleryViewModel : ViewModel() {

    companion object {
        const val OPEN_GALLERY_REQUEST_CODE = 2
        const val IS_FIRST_TIME_KEY = "is_first_time"
        const val STORAGE_PERMISSION_REQUEST_CODE = 3
        const val USER_PERMISSION_ACTION_KEY = "user_permission_action"
    }

    private val _listOfImageUris = MutableLiveData<ArrayList<Uri>>()
    val listOfImages: LiveData<ArrayList<Uri>>
        get() = _listOfImageUris

    // load all the images from the ImageLoader utility class asynchronously
    fun loadUris(context: Context) {
        viewModelScope.launch {

            // post the list of the Image Uris to the mutableLiveData to be observed from within the fragment
            _listOfImageUris.postValue(ImageProvider.getAllImages(context, ImageProvider.ImageSource.External))
        }
    }
}
