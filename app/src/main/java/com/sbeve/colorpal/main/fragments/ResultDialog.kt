package com.sbeve.colorpal.main.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.palette.graphics.Palette
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sbeve.colorpal.databinding.ColorNameLayoutBinding
import com.sbeve.colorpal.databinding.DialogResultBinding
import com.sbeve.colorpal.main.MainActivity
import com.sbeve.colorpal.main.fragments.ResultViewModel.Companion.NO_COLOR_FOUND_CODE
import java.lang.Integer.toHexString

private const val CLIP_LABEL = "Copied Color"

class ResultDialog : BottomSheetDialogFragment() {

    private lateinit var binding: DialogResultBinding
    private lateinit var mainActivity: MainActivity
    private val viewModel: ResultViewModel by viewModels()
    private val navArgs: ResultDialogArgs by navArgs()
    private lateinit var selectedBitmapUri: Uri
    private lateinit var clipboardManager: ClipboardManager


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreate(savedInstanceState)
        binding = DialogResultBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivity = requireActivity() as MainActivity
        selectedBitmapUri = navArgs.uri
        clipboardManager = mainActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        viewModel.setBitmapFromUri(this, selectedBitmapUri, binding.selectedImage)
        viewModel.selectedImageBitmap.observe(viewLifecycleOwner) {
            binding.selectedImage.setImageBitmap(it)
            val builder = Palette.Builder(it)
            builder.maximumColorCount(1024)
            builder.generate { palette ->
                palette?.let { populateTheLayout(palette) }
            }
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

        binding.colorsList.removeAllViews()

        for (each in colors) {

            // if no color of that type could be decoded, i.e. the color code is the same as NO_COLOR_FOUND_CODE, skip it
            if (each == NO_COLOR_FOUND_CODE) continue

            // inflate the colorNameLayout to set each color to the ColorDotView inside the layout
            val colorNameBinding = ColorNameLayoutBinding.inflate(layoutInflater, binding.root, false)
            colorNameBinding.colorCircle.fillColor = each
            val colorCode = "#${toHexString(each).uppercase().drop(2)}"
            colorNameBinding.colorCode.text = colorCode
            val clipData = ClipData.newPlainText(CLIP_LABEL, colorCode)
            colorNameBinding.copyButton.setOnClickListener {
                clipboardManager.setPrimaryClip(clipData)
                Toast.makeText(mainActivity, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            }
            binding.colorsList.addView(colorNameBinding.root)
        }
    }
}

