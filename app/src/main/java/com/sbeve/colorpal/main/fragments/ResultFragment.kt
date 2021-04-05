package com.sbeve.colorpal.main.fragments

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.sbeve.colorpal.R
import com.sbeve.colorpal.databinding.ColorNameLayoutBinding
import com.sbeve.colorpal.databinding.FragmentResultBinding
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class ResultFragment : Fragment(R.layout.fragment_result) {

    private lateinit var binding: FragmentResultBinding
    private val args: ResultFragmentArgs by navArgs()
    private val viewModel: ResultViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentResultBinding.bind(view)
        setBitmapFromUri(
            args.selectedImageUri,
            binding.selectedImageView.layoutParams.width,
            binding.selectedImageView.layoutParams.height
        )
        viewModel.selectedImageBitmap.observe(viewLifecycleOwner) {
            handleBitmap(it)
        }
    }

    private fun setBitmapFromUri(uri: Uri, width: Int, height: Int) {
        lifecycleScope.launch(IO) {
            val bitmap = Glide.with(this@ResultFragment)
                .asBitmap()
                .load(uri)
                .submit(width, height)
                .get()
            viewModel.selectedImageBitmap.postValue(bitmap)
        }
    }

    private fun handleBitmap(bitmap: Bitmap) {
        binding.selectedImageView.setImageBitmap(bitmap)
        Palette.from(bitmap).generate { palette ->
            palette?.let { populateTheLayout(it) }
        }
    }

    private fun populateTheLayout(palette: Palette) {
        val colors = arrayOf(
            palette.getLightMutedColor(NO_COLOR_FOUND_CODE),
            palette.getMutedColor(NO_COLOR_FOUND_CODE),
            palette.getDarkMutedColor(NO_COLOR_FOUND_CODE),
            palette.getLightVibrantColor(NO_COLOR_FOUND_CODE),
            palette.getVibrantColor(NO_COLOR_FOUND_CODE),
            palette.getDarkVibrantColor(NO_COLOR_FOUND_CODE)
        )
        binding.colors.removeAllViews()
        for (each in colors) {
            if (each == NO_COLOR_FOUND_CODE) continue
            val colorNameBinding = ColorNameLayoutBinding.inflate(layoutInflater)
            colorNameBinding.colorCircle.fillColor = each
            colorNameBinding.colorCode.text = "#" + (Integer.toHexString(each).toString()).toUpperCase()
            binding.colors.addView(colorNameBinding.root)
        }
    }
}
