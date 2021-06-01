package com.sbeve.colorpal.main.fragments

import android.graphics.Bitmap
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ResultViewModel : ViewModel() {

    companion object {
        const val NO_COLOR_FOUND_CODE = -1
    }

    private val _selectedImageBitmap = MutableLiveData<Bitmap>()
    val selectedImageBitmap: LiveData<Bitmap>
        get() = _selectedImageBitmap

    // get the Uri, decode it with Glide on the IO dispatcher and post the generated value to the
    // LiveData to be observed in the fragment
    fun setBitmapFromUri(fragment: Fragment, uri: Uri, width: Int, height: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val bitmap = Glide.with(fragment)
                .asBitmap()
                .load(uri)
                .submit(width, height)
                .get()
            _selectedImageBitmap.postValue(bitmap)
        }
    }
}
