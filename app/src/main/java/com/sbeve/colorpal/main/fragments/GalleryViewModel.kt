package com.sbeve.colorpal.main.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sbeve.colorpal.utils.ImageProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class GalleryViewModel
@Inject
constructor(@ApplicationContext val applicationContext: Context) : ViewModel() {

    companion object {
        const val OPEN_GALLERY_REQUEST_CODE = 2
        const val IS_FIRST_TIME_KEY = "is_first_time"
        const val STORAGE_PERMISSION_REQUEST_CODE = 3
        const val USER_PERMISSION_ACTION_KEY = "user_permission_action"
    }

    private val _listOfImageUris = MutableLiveData<ArrayList<Uri>>()
    val listOfImagesUris: LiveData<ArrayList<Uri>>
        get() = _listOfImageUris


    // load all the images from the ImageLoader utility class asynchronously
    fun loadUrisToLiveData() {
        viewModelScope.launch(IO) {

            // post the list of the Image Uris to the mutableLiveData to be observed from within the fragment
            val listOfImageUris = ImageProvider.getAllImages(applicationContext, ImageProvider.ImageSource.External)
            _listOfImageUris.postValue(listOfImageUris)

        }
    }
}
