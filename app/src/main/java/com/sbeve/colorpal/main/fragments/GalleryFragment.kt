package com.sbeve.colorpal.main.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.sbeve.colorpal.R
import com.sbeve.colorpal.databinding.FragmentGalleryBinding
import com.sbeve.colorpal.main.MainActivity
import com.sbeve.colorpal.main.fragments.GalleryViewModel.Companion.IS_FIRST_TIME_KEY
import com.sbeve.colorpal.recyclerview_utils.RVAdapter

class GalleryFragment : Fragment(R.layout.fragment_gallery), RVAdapter.ImageViewClickListener {

    private lateinit var binding: FragmentGalleryBinding
    private val viewModel: GalleryViewModel by viewModels()

    // instance of the parent activity casted to MainActivity to access public components inside the MainActivity
    private lateinit var mainActivity: MainActivity

    private val getContentLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            mainActivity.navController.navigate(GalleryFragmentDirections.actionGalleryFragmentToResultDialog(uri))
        }
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
                Log.e("Gallery Fragment", "userPermissionAction: Allow")
                1
            } else {
                if (shouldShowRequestPermissionRationale) {
                    Log.e("Gallery Fragment", "userPermissionAction: Deny")
                    0
                } else {
                    Log.e("Gallery Fragment", "userPermissionAction: Deny and don't ask again")
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

        binding.galleryImagesRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    if (binding.openSystemPickerButton.isExtended) binding.openSystemPickerButton.shrink()
                } else if (dy < 0) {
                    if (!binding.openSystemPickerButton.isExtended) binding.openSystemPickerButton.extend()
                }
            }
        })

        // let the user manually grant storage access if they deny it the first time the app was opened
        binding.grantPermissionsButton.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        // let the user manually select an image using the system picker
        binding.openSystemPickerButton.setOnClickListener {
            getContentLauncher.launch("image/*")
        }
    }

    override fun onResume() {
        super.onResume()

        determineAndUpdateLayout()
    }

    override fun onImageClick(uri: Uri) {

        // navigate to the dialog fragment and pass in the uri for the image to be decoded with Glide again
        findNavController().navigate(GalleryFragmentDirections.actionGalleryFragmentToResultDialog(uri))
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
