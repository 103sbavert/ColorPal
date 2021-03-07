package com.sbeve.colorpal.fragment

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

const val NO_COLOR_FOUND_CODE = -1

class ResultViewModel : ViewModel() {
    var selectedImageBitmap = MutableLiveData<Bitmap>()
}
