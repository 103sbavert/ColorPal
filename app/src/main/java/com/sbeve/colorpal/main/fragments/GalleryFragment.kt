package com.sbeve.colorpal.main.fragments

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.sbeve.colorpal.R
import com.sbeve.colorpal.databinding.FragmentGalleryBinding
import com.sbeve.colorpal.main.MainActivity
import com.sbeve.colorpal.recyclerview_utils.RVAdapter
import com.sbeve.colorpal.utils.ImageProvider

class GalleryFragment : Fragment(R.layout.fragment_gallery), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        private const val OPEN_GALLERY_REQUEST_CODE = 2
        private const val IS_FIRST_TIME_KEY = "is_first_time"
        private const val STORAGE_PERMISSION_REQUEST_CODE = 3
        private const val USER_PERMISSION_ACTION_KEY = "user_permission_action"
    }

    private lateinit var binding: FragmentGalleryBinding
    private val mainActivity: MainActivity by lazy {
        requireActivity() as MainActivity
    }

    /*
        action taken by user when the permission pop up was shown
        0 -> Deny
        1 -> Allow
        2 -> Deny and don't ask again
     */
    private val userPermissionAction: Int
        get() = mainActivity.sharedPreferences.getInt(USER_PERMISSION_ACTION_KEY, 0)
    private val imageViewClickListener = object : RVAdapter.ImageViewClickListener {
        override fun onImageClick(uri: Uri, imageView: ImageView) {
            mainActivity.navController.navigate(GalleryFragmentDirections.actionGalleryFragmentToResultFragment(uri))
        }
    }

    private val shouldShowRequestPermissionRationaleForStorageAccess: Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
            } else false
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentGalleryBinding.bind(view)

        if (mainActivity.sharedPreferences.getBoolean(IS_FIRST_TIME_KEY, true)) {
            requestStorageAccess()
            mainActivity.sharedPreferences.edit().putBoolean(IS_FIRST_TIME_KEY, false).apply()
        }

        mainActivity.sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        determineAndUpdateLayout()
        binding.grantPermissionsButton.setOnClickListener {
            requestStorageAccess()
        }
        binding.openGalleryButton.setOnClickListener {
            openSystemPicker()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return
        when (requestCode) {
            OPEN_GALLERY_REQUEST_CODE -> {
                mainActivity.navController.navigate(GalleryFragmentDirections.actionGalleryFragmentToResultFragment(data?.data!!))
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            STORAGE_PERMISSION_REQUEST_CODE -> {
                when (grantResults[0]) {
                    // if the permission is granted, update the sharedPreference
                    // so the sharedPreferencesListener would update the UI
                    PackageManager.PERMISSION_GRANTED -> {
                        mainActivity.sharedPreferences.edit().putInt(USER_PERMISSION_ACTION_KEY, 1).apply()
                    }
                    // if the permission is not granted, check if the user selected
                    // "Deny" or "Deny and don't ask again"
                    PackageManager.PERMISSION_DENIED -> {
                        // if [shouldShowPermissionRationale] is true, we know the
                        // user has selected "Deny"
                        if (shouldShowRequestPermissionRationaleForStorageAccess)
                            mainActivity.sharedPreferences.edit().putInt(USER_PERMISSION_ACTION_KEY, 0).apply()
                        // If [shouldShowPermissionRationale] is false, we know the
                        // user has selected "Deny and don't ask again"
                        else
                            mainActivity.sharedPreferences.edit().putInt(USER_PERMISSION_ACTION_KEY, -1).apply()
                    }
                }
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            USER_PERMISSION_ACTION_KEY -> {
                determineAndUpdateLayout()
            }
        }
    }

    private fun determineAndUpdateLayout() {
        when (userPermissionAction) {
            1 -> {
                setUpRecyclerView()
                binding.grantPermissionsButton.isEnabled = false
            }
            0 -> {
                showStorageAccessDeniedMessage()
            }
            -1 -> {
                showStorageAccessDeniedMessage()
                binding.grantPermissionsButton.isEnabled = false
            }
        }
    }

    private fun showStorageAccessDeniedMessage() {
        binding.recyclerView.visibility = View.GONE
        binding.errorMessage.visibility = View.VISIBLE
    }

    private fun openSystemPicker() {
        val intent = Intent()
        intent.action = Intent.ACTION_OPEN_DOCUMENT
        intent.type = "image/*"
        startActivityForResult(intent, OPEN_GALLERY_REQUEST_CODE)
    }

    private fun setUpRecyclerView() {
        binding.errorMessage.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
        binding.recyclerView.adapter = RVAdapter(ImageProvider.getAllImages(mainActivity, ImageProvider.ImageSource.External), imageViewClickListener)
    }

    private fun requestStorageAccess() {
        requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST_CODE)
    }
}
