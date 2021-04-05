package com.sbeve.colorpal.main.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.sbeve.colorpal.R
import com.sbeve.colorpal.databinding.FragmentGalleryBinding
import com.sbeve.colorpal.main.MainActivity
import com.sbeve.colorpal.recyclerview_utils.RVAdapter
import com.sbeve.colorpal.utils.ImageProvider

private const val STORAGE_PERMISSION_REQUEST_CODE = 2

class GalleryFragment : Fragment(R.layout.fragment_gallery) {

    private lateinit var binding: FragmentGalleryBinding
    private val mainActivity: MainActivity by lazy {
        requireActivity() as MainActivity
    }
    private val imageViewClickListener = object : RVAdapter.ImageViewClickListener {
        override fun onImageClick(uri: Uri, imageView: ImageView) {
            mainActivity.navController.navigate(GalleryFragmentDirections.actionGalleryFragmentToResultFragment(uri))
        }
    }
    private val isStoragePermissionGranted: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) mainActivity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED else true

    private val shouldShowRequestPermissionRationale: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) mainActivity.shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) else false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentGalleryBinding.bind(view)
        if (!isStoragePermissionGranted) requestStoragePermission() else setUpRecyclerView()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setUpRecyclerView()
            }
        }
    }

    private fun requestStoragePermission() {
        if (shouldShowRequestPermissionRationale) Toast.makeText(mainActivity, "Please grant storage permission", Toast.LENGTH_LONG).show()
        requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST_CODE)
    }

    private fun setUpRecyclerView() {
        binding.recyclerView.adapter =
            RVAdapter(ImageProvider.getAllImages(mainActivity, ImageProvider.ImageSource.External), imageViewClickListener)
    }
}
