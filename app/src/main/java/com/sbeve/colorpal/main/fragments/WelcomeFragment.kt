package com.sbeve.colorpal.main.fragments

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import com.sbeve.colorpal.R
import com.sbeve.colorpal.databinding.FragmentWelcomeBinding
import com.sbeve.colorpal.main.MainActivity


class WelcomeFragment : Fragment(R.layout.fragment_welcome), SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        private const val STORAGE_PERMISSION_REQUEST_CODE = 2
        private const val USER_PERMISSION_ACTION_KEY = "user_permission_action"
        private const val SELECTED_LAYOUT_KEY = "selected_layout"
    }

    private lateinit var binding: FragmentWelcomeBinding

    // parent activity as mainActivity to access the contents inside
    private val mainActivity: MainActivity by lazy {
        requireActivity() as MainActivity
    }

    /*
    private val viewModel: WelcomeViewModel by viewModels()
    private val isStoragePermissionGranted: Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mainActivity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            } else true
        }
    */

    private val shouldShowRequestPermissionRationale: Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mainActivity.shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
            } else false
        }
    private val currentlySelectedLayout: Int
        get() {
            return binding.pickLayoutChoiceGroup.checkedRadioButtonId
        }

    /**
     * action taken by user when the permission pop up was shown
     * 0 -> Deny
     * 1 -> Allow
     * 2 -> Deny and don't ask again
     */
    private val userPermissionAction: Int
        get() = mainActivity.sharedPreferences.getInt(USER_PERMISSION_ACTION_KEY, 0)
    private val selectedLayout: Int
        get() = mainActivity.sharedPreferences.getInt(SELECTED_LAYOUT_KEY, 0)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentWelcomeBinding.bind(view)
        when (selectedLayout) {
            1 -> mainActivity.navController.navigate(WelcomeFragmentDirections.actionWelcomeFragmentToGalleryFragment())
            2 -> mainActivity.navController.navigate(WelcomeFragmentDirections.actionWelcomeFragmentToPickerFragment())
        }

        /**
         * set shared preference listener to disable or enable nextButton
         * or permissionButton based on the value of [userPermissionAction]
         */

        mainActivity.sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        updateButtonStates()

        binding.pickLayoutChoiceGroup.setOnCheckedChangeListener { _: RadioGroup, checkedItemId: Int ->
            when (checkedItemId) {
                binding.manual.id -> {
                    binding.permissionButton.visibility = View.INVISIBLE
                    binding.nextButton.isEnabled = true
                }
                binding.gallery.id -> {
                    binding.permissionButton.visibility = View.VISIBLE
                    binding.nextButton.isEnabled = userPermissionAction == 1
                }
            }
        }

        binding.permissionButton.setOnClickListener {
            requestStoragePermission()
        }
        binding.nextButton.setOnClickListener {
            when (currentlySelectedLayout) {
                binding.gallery.id -> {
                    mainActivity.sharedPreferences.edit().putInt(SELECTED_LAYOUT_KEY, 1).apply()
                    mainActivity.navController.navigate(WelcomeFragmentDirections.actionWelcomeFragmentToGalleryFragment())
                }
                binding.manual.id -> {
                    mainActivity.sharedPreferences.edit().putInt(SELECTED_LAYOUT_KEY, 2).apply()
                    mainActivity.navController.navigate(WelcomeFragmentDirections.actionWelcomeFragmentToPickerFragment())
                }
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
                        if (!shouldShowRequestPermissionRationale)
                            mainActivity.sharedPreferences.edit().putInt(USER_PERMISSION_ACTION_KEY, 2).apply()
                        // If [shouldShowPermissionRationale] is false, we know the
                        // user has selected "Deny and don't ask again"
                        else
                            mainActivity.sharedPreferences.edit().putInt(USER_PERMISSION_ACTION_KEY, 0).apply()
                    }
                }
            }
        }
    }

    private fun requestStoragePermission() {
        requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST_CODE)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            USER_PERMISSION_ACTION_KEY -> {
                updateButtonStates()
            }
        }
    }

    /**
     * check the current state of [userPermissionAction] as the fragment
     * is initialized to enable or disable nextButton and permissionButton
     */
    private fun updateButtonStates() {
        when (userPermissionAction) {
            0 -> {
                binding.nextButton.isEnabled = false
                binding.permissionButton.isEnabled = true
            }
            1 -> {
                binding.nextButton.isEnabled = true
                binding.permissionButton.isEnabled = false
            }
            2 -> {
                binding.nextButton.isEnabled = false
                binding.permissionButton.isEnabled = false
            }
        }
    }
}
