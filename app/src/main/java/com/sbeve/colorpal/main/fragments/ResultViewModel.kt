package com.sbeve.colorpal.main.fragments

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class ResultViewModel : ViewModel() {

    companion object {
        const val NO_COLOR_FOUND_CODE = -1
    }

    var selectedImageBitmap = MutableLiveData<Bitmap>()
}
