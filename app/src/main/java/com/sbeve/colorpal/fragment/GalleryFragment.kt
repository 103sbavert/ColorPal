package com.sbeve.colorpal.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.sbeve.colorpal.MainActivity
import com.sbeve.colorpal.R
import com.sbeve.colorpal.databinding.FragmentGalleryBinding
import com.sbeve.colorpal.recyclerview_utils.RecyclerViewAdapter

private const val STORAGE_PERMISSION_REQUEST_CODE = 2

class GalleryFragment : Fragment(R.layout.fragment_gallery) {

    private lateinit var binding: FragmentGalleryBinding
    private val mainActivity: MainActivity by lazy {
        requireActivity() as MainActivity
    }
    private val imageViewClickListener = object : RecyclerViewAdapter.ImageViewClickListener {
        override fun onImageClick(uri: Uri) {
            mainActivity.navController.navigate(GalleryFragmentDirections.actionGalleryFragmentToResultFragment(uri))
        }
    }
    private val isStoragePermissionGranted: Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mainActivity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            } else true
        }
    private val shouldShowRequestPermissionRationale: Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mainActivity.shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
            } else false
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentGalleryBinding.bind(view)
        requestStoragePermission()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            Log.e("srprs", shouldShowRequestPermissionRationale.toString())
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                binding.recyclerView.adapter = RecyclerViewAdapter(getImageUris(), imageViewClickListener)
            }
        }
    }

    private fun getImageUris(): List<Uri> {
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val cursor = mainActivity.contentResolver.query(
            uri,
            projection,
            null,
            null,
            null
        )
        val imageUris = mutableListOf<Uri>()
        while (cursor?.moveToNext() == true) {
            val columnIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val imageId = cursor.getInt(columnIndex)
            val currentImageUri = Uri.withAppendedPath(uri, imageId.toString())
            imageUris.add(currentImageUri)
        }
        cursor?.close()
        return imageUris
    }

    private fun requestStoragePermission() {
        when (isStoragePermissionGranted) {
            true -> {
                binding.recyclerView.adapter = RecyclerViewAdapter(getImageUris(), imageViewClickListener)
            }
            false -> {
                if (shouldShowRequestPermissionRationale) Toast.makeText(mainActivity, "Please grant storage permission", Toast.LENGTH_LONG).show()
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST_CODE)
            }
        }
    }
}
