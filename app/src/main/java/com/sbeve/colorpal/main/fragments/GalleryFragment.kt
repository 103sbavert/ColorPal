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
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.sbeve.colorpal.R
import com.sbeve.colorpal.databinding.FragmentGalleryBinding
import com.sbeve.colorpal.main.MainActivity
import com.sbeve.colorpal.main.fragments.GalleryViewModel.Companion.IS_FIRST_TIME_KEY
import com.sbeve.colorpal.main.fragments.GalleryViewModel.Companion.OPEN_GALLERY_REQUEST_CODE
import com.sbeve.colorpal.main.fragments.GalleryViewModel.Companion.STORAGE_PERMISSION_REQUEST_CODE
import com.sbeve.colorpal.main.fragments.GalleryViewModel.Companion.USER_PERMISSION_ACTION_KEY
import com.sbeve.colorpal.recyclerview_utils.RVAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GalleryFragment : Fragment(R.layout.fragment_gallery), SharedPreferences.OnSharedPreferenceChangeListener, RVAdapter.ImageViewClickListener {

    private lateinit var binding: FragmentGalleryBinding
    private val viewModel: GalleryViewModel by viewModels()

    // instance of the parent activity casted to MainActivity to access public components inside the MainActivity
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
    private val shouldShowRequestPermissionRationale: Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
            } else false
        }

    //animation to be played when a fragment transaction happens from this activity a pop action transaction to this activity happens so the fragment
    //doesn't disappear from the background
    private val stayInPlaceAnimation: Animation? by lazy {
        val anim: Animation = AlphaAnimation(1.0F, 1.0F)
        anim.duration = resources.getInteger(R.integer.animation_duration).toLong()
        anim
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentGalleryBinding.bind(view)

        // ask for storage access if this is the first time the app has been opened
        // (don't bug the user the next time, just give them a button)
        if (mainActivity.sharedPreferences.getBoolean(IS_FIRST_TIME_KEY, true)) {
            requestStorageAccess()
            mainActivity.sharedPreferences.edit().putBoolean(IS_FIRST_TIME_KEY, false).apply()
        } else {

            // determine and update the layout
            determineAndUpdateLayout()
        }


        // when the viewModel is done loading the list of image uris, set up the recycler view
        // to load and show all the images returned
        viewModel.listOfImagesUris.observe(viewLifecycleOwner) {
            setUpRecyclerView(it)
        }

        mainActivity.sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        // let the user manually grant storage access if they deny it the first time the app was opened
        binding.grantPermissionsButton.setOnClickListener {
            requestStorageAccess()
        }

        // let the user manually select an image using the system picker
        binding.openSystemPickerButton.setOnClickListener {
            openSystemPicker()
        }
    }

    //play an empty animation to keep the fragment from disappearing from the background when the enter animation for other fragments is playing
    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int) = stayInPlaceAnimation

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return
        when (requestCode) {

            // if the user was successful in picking an image using the system picker, navigate to
            // the results fragment and pass in the uri of the selected image
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
                    PackageManager.PERMISSION_GRANTED -> mainActivity.sharedPreferences.edit().putInt(USER_PERMISSION_ACTION_KEY, 1).apply()

                    // if the permission is not granted, check if the user selected
                    // "Deny" or "Deny and don't ask again"
                    PackageManager.PERMISSION_DENIED -> {

                        // if [shouldShowPermissionRationale] is true, we know the
                        // user has selected "Deny"
                        if (shouldShowRequestPermissionRationale) mainActivity.sharedPreferences.edit().putInt(USER_PERMISSION_ACTION_KEY, 0).apply()

                        // If [shouldShowPermissionRationale] is false, we know the
                        // user has selected "Deny and don't ask again"
                        else mainActivity.sharedPreferences.edit().putInt(USER_PERMISSION_ACTION_KEY, -1).apply()
                    }
                }
            }
        }
    }

    // determine the layout again based on whether the user has given storage access and update the views
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            USER_PERMISSION_ACTION_KEY -> {
                determineAndUpdateLayout()
            }
        }
    }

    override fun onImageClick(uri: Uri, imageView: ImageView) {
        mainActivity.navController.navigate(GalleryFragmentDirections.actionGalleryFragmentToResultFragment(uri))
    }

    private fun determineAndUpdateLayout() {
        when (userPermissionAction) {

            // if the user has granted storage access, request the viewModel to load imageUris
            // using the image loader utility class
            1 -> {

                // don't update the values inside the live data if it already has contents inside
                if (viewModel.listOfImagesUris.value.isNullOrEmpty()) viewModel.loadUrisToLiveData()
                binding.grantPermissionsButton.isEnabled = false
            }

            // if the user has selected "deny" (as opposed to "deny and don't ask again")
            // show a message to notify user about the fact and provide them a button
            0 -> showStorageAccessDeniedMessage()


            // disable the button if the user has selected "deny and don't ask again"
            -1 -> {
                showStorageAccessDeniedMessage()
                binding.grantPermissionsButton.isEnabled = false
            }
        }
    }

    private fun showStorageAccessDeniedMessage() {
        binding.galleryImagesRecyclerView.visibility = View.GONE
        binding.storageAccessNotGrantedMessage.visibility = View.VISIBLE
    }

    // open the system picker and set the mime type to image/* so only images are shown
    private fun openSystemPicker() {
        val intent = Intent()
        intent.action = Intent.ACTION_OPEN_DOCUMENT
        intent.type = "image/*"
        startActivityForResult(intent, OPEN_GALLERY_REQUEST_CODE)
    }

    // hide the error message and set up the recycler view with the provided list
    private fun setUpRecyclerView(list: ArrayList<Uri>) {
        binding.storageAccessNotGrantedMessage.visibility = View.GONE
        binding.galleryImagesRecyclerView.visibility = View.VISIBLE
        binding.galleryImagesRecyclerView.adapter = RVAdapter(list, this)
    }

    // request storage access
    private fun requestStorageAccess() {
        requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST_CODE)
    }
}
