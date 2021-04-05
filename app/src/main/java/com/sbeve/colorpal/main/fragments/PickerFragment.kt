package com.sbeve.colorpal.main.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.sbeve.colorpal.R
import com.sbeve.colorpal.databinding.FragmentPickerBinding
import com.sbeve.colorpal.main.MainActivity

private const val OPEN_GALLERY_REQUEST_CODE = 2

class PickerFragment : Fragment(R.layout.fragment_picker) {
    private lateinit var binding: FragmentPickerBinding
    private val mainActivity: MainActivity by lazy {
        requireActivity() as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentPickerBinding.bind(view)
        binding.openGalleryButton.setOnClickListener {
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_PICK
        intent.type = "image/*"
        startActivityForResult(intent, OPEN_GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return
        when (requestCode) {
            OPEN_GALLERY_REQUEST_CODE -> {
                mainActivity.navController.navigate(PickerFragmentDirections.actionPickerFragmentToResultFragment(data?.data!!))
            }
        }
    }
}
