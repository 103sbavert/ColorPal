package com.sbeve.colorpal.main.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.palette.graphics.Palette
import com.sbeve.colorpal.R
import com.sbeve.colorpal.databinding.ColorNameLayoutBinding
import com.sbeve.colorpal.databinding.FragmentResultBinding
import com.sbeve.colorpal.main.MainActivity
import com.sbeve.colorpal.main.fragments.ResultViewModel.Companion.NO_COLOR_FOUND_CODE
import java.lang.Integer.toHexString

class ResultFragment : Fragment(R.layout.fragment_result) {

    private lateinit var binding: FragmentResultBinding
    private val args: ResultFragmentArgs by navArgs()
    private val viewModel: ResultViewModel by viewModels()
    private val mainActivity: MainActivity by lazy {
        requireActivity() as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentResultBinding.bind(view)

        binding.materialToolbar.setNavigationOnClickListener {
            mainActivity.navController.navigateUp()
        }

        viewModel.setBitmapFromUri(
            this,
            args.selectedImageUri,
            binding.selectedImageView.layoutParams.width,
            binding.selectedImageView.layoutParams.height
        )
        viewModel.selectedImageBitmap.observe(viewLifecycleOwner) {
            handleBitmap(it)
        }
    }

    private fun handleBitmap(bitmap: Bitmap) {
        binding.selectedImageView.setImageBitmap(bitmap)
        val builder = Palette.Builder(bitmap)
        builder.maximumColorCount(64)
        builder.generate { palette ->
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

        binding.colorsLinearLayout.removeAllViews()
        for (each in colors) {
            if (each == NO_COLOR_FOUND_CODE) continue
            val colorNameBinding = ColorNameLayoutBinding.inflate(layoutInflater)
            colorNameBinding.colorCircle.fillColor = each
            colorNameBinding.colorCode.text = "#${toHexString(each).toUpperCase()}"
            binding.colorsLinearLayout.addView(colorNameBinding.root)
        }
    }
}
