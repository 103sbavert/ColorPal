package com.sbeve.colorpal.main.fragments

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.transition.TransitionInflater
import com.sbeve.colorpal.R
import com.sbeve.colorpal.databinding.FragmentGalleryBinding
import com.sbeve.colorpal.main.MainActivity
import com.sbeve.colorpal.main.fragments.GalleryViewModel.Companion.IS_FIRST_TIME_KEY
import com.sbeve.colorpal.main.fragments.GalleryViewModel.Companion.USER_PERMISSION_ACTION_KEY
import com.sbeve.colorpal.recyclerview_utils.RVAdapter

class GalleryFragment : Fragment(R.layout.fragment_gallery), SharedPreferences.OnSharedPreferenceChangeListener, RVAdapter.ImageViewClickListener {

    private lateinit var binding: FragmentGalleryBinding
    private val viewModel: GalleryViewModel by viewModels()

    // instance of the parent activity casted to MainActivity to access public components inside the MainActivity
    private lateinit var mainActivity: MainActivity

    private val getContentLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) mainActivity.navController.navigate(GalleryFragmentDirections.actionGalleryFragmentToResultFragment(uri))
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            determineAndUpdateLayout()
        }

    /*
    action taken by user when the permission pop up was shown
    0 -> Deny
    1 -> Allow
    -1 -> Deny and don't ask again
    */
    private val userPermissionAction: Int
        get() {
            val permission = ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
            return if (permission == PackageManager.PERMISSION_GRANTED) {
                Log.e("TAG", "userPermissionAction: 1")
                1
            } else {
                if (shouldShowRequestPermissionRationale) {
                    Log.e("TAG", "userPermissionAction: 0")
                    0
                } else {
                    Log.e("TAG", "userPermissionAction: -1")
                    -1
                }
            }
        }
    private val shouldShowRequestPermissionRationale: Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
            } else false
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentGalleryBinding.bind(view)
        mainActivity = requireActivity() as MainActivity

        exitTransition = TransitionInflater.from(requireContext()).inflateTransition(R.transition.fade)

        // ask for storage access if this is the first time the app has been opened
        // (don't bug the user the next time, just give them a button)
        if (mainActivity.sharedPreferences.getBoolean(IS_FIRST_TIME_KEY, true)) {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            mainActivity.sharedPreferences.edit().putBoolean(IS_FIRST_TIME_KEY, false).apply()
        } else {

            // determine and update the layout
            determineAndUpdateLayout()
        }

        binding.swipeToRefreshLayout.setOnRefreshListener {
            viewModel.shouldUpdateRecyclerView = true
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
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        // let the user manually select an image using the system picker
        binding.openSystemPickerButton.setOnClickListener {
            getContentLauncher.launch("image/*")
        }
    }

    //play an empty animation to keep the fragment from disappearing from the background when the enter animation for other fragments is playing
//    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int) = AlphaAnimation(1.0F, 1.0F).apply {
//        duration = resources.getInteger(R.integer.animation_duration).toLong()
//    }

    // determine the layout again based on whether the user has given storage access and update the views
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            USER_PERMISSION_ACTION_KEY -> {
                determineAndUpdateLayout()
            }
        }
    }

    override fun onImageClick(uri: Uri, imageView: ImageView) {

        // navigate to the result fragment and pass in the uri for the image to be decoded with Glide again
        val extras = FragmentNavigatorExtras(imageView to "large_image")
        mainActivity.navController.navigate(GalleryFragmentDirections.actionGalleryFragmentToResultFragment(uri), extras)
    }

    private fun determineAndUpdateLayout() {
        when (userPermissionAction) {

            // if the user has granted storage access, request the viewModel to load imageUris
            // using the image loader utility class
            1 -> {

                // don't update the values inside the live data if it already has contents inside
                if (viewModel.shouldUpdateRecyclerView) viewModel.loadUrisToLiveData()
                viewModel.shouldUpdateRecyclerView = false
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
        binding.swipeToRefreshLayout.isRefreshing = false
    }

    private fun showStorageAccessDeniedMessage() {
        binding.galleryImagesRecyclerView.visibility = View.GONE
        binding.storageAccessNotGrantedMessage.visibility = View.VISIBLE
    }

    // hide the error message and set up the recycler view with the provided list
    private fun setUpRecyclerView(list: ArrayList<Uri>) {
        binding.storageAccessNotGrantedMessage.visibility = View.GONE
        binding.galleryImagesRecyclerView.visibility = View.VISIBLE
        binding.galleryImagesRecyclerView.adapter = RVAdapter(list, this)
    }
}
