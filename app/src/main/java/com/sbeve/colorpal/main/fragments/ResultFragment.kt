package com.sbeve.colorpal.main.fragments

import android.annotation.SuppressLint
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
    private lateinit var mainActivity: MainActivity

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivity = requireActivity() as MainActivity
        binding = FragmentResultBinding.bind(view)

        binding.materialToolbar.setNavigationOnClickListener {
            mainActivity.navController.navigateUp()
        }

        // get the bitmap from Uri from the args (passed by the gallery fragment) and pass in the
        // height and width of the image view that's going to show it
        viewModel.setBitmapFromUri(
            this,
            args.selectedImageUri,
            binding.selectedImage.layoutParams.width,
            binding.selectedImage.layoutParams.height
        )

        viewModel.selectedImageBitmap.observe(viewLifecycleOwner) {

            // handle the bitmap once its decoded
            handleBitmap(it)
        }
    }

    // set the bitmap, that has been decoded by Glide, to the image view and pass in the same to
    // the palette builder to generate the palette. Pass in the generated palette to be worked with
    private fun handleBitmap(bitmap: Bitmap) {
        binding.selectedImage.setImageBitmap(bitmap)
        val builder = Palette.Builder(bitmap)
        builder.maximumColorCount(64)
        builder.generate { palette ->
            palette?.let { populateTheLayout(it) }
        }
    }

    // extract colors from the palette and create instances of ColorDotView to show those colors
    @SuppressLint("SetTextI18n")
    private fun populateTheLayout(palette: Palette) {

        // extract each one of the six colors from the palette
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

            // if no color of that type could be decoded, i.e. the color code is the same as NO_COLOR_FOUND_CODE, skip it
            if (each == NO_COLOR_FOUND_CODE) continue

            // inflate the colorNameLayout to set each color to the ColorDotView inside the layout
            val colorNameBinding = ColorNameLayoutBinding.inflate(layoutInflater)
            colorNameBinding.colorCircle.fillColor = each
            colorNameBinding.colorCode.text = "#${toHexString(each).uppercase()}"
            binding.colorsLinearLayout.addView(colorNameBinding.root)
        }
    }
}
