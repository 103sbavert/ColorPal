package com.sbeve.colorpal.main.fragments

import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class ResultViewModel : ViewModel() {

    companion object {
        const val NO_COLOR_FOUND_CODE = -1
    }

    private val _selectedImageBitmap = MutableLiveData<Bitmap>()
    val selectedImageBitmap: LiveData<Bitmap>
        get() = _selectedImageBitmap


    // listener to be used when the image is loaded with glide
    private val listener = object : RequestListener<Bitmap> {
        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean) = false

        override fun onResourceReady(
            resource: Bitmap?,
            model: Any?,
            target: Target<Bitmap>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {

            // update the live data with the new bitmap
            resource.let { _selectedImageBitmap.value = it }
            return true
        }
    }

    // get the Uri, decode it with Glide on the IO dispatcher and post the generated value to the
    // LiveData to be observed in the fragment
    fun setBitmapFromUri(fragment: Fragment, uri: Uri, imageView: ImageView) {
        Glide
            .with(fragment)
            .asBitmap()
            .load(uri)
            .addListener(listener)
            .into(imageView)
    }
}
