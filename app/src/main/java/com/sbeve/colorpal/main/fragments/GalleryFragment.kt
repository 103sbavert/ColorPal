package com.sbeve.colorpal.main.fragments

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.sbeve.colorpal.R
import com.sbeve.colorpal.databinding.FragmentGalleryBinding
import com.sbeve.colorpal.main.MainActivity
import com.sbeve.colorpal.recyclerview_utils.RVAdapter
import com.sbeve.colorpal.utils.ImageProvider

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentGalleryBinding.bind(view)
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        binding.recyclerView.adapter =
            RVAdapter(ImageProvider.getAllImages(mainActivity, ImageProvider.ImageSource.External), imageViewClickListener)
    }
}
