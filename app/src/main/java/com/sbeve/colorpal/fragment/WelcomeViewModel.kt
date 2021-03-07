package com.sbeve.colorpal.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WelcomeViewModel : ViewModel() {
    var permissionButtonState = MutableLiveData(true)
    var nextButtonState = MutableLiveData(false)
}
