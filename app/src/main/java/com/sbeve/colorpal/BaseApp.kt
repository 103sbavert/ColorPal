package com.sbeve.colorpal

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class BaseApp : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    init {
        context = this
    }
}
