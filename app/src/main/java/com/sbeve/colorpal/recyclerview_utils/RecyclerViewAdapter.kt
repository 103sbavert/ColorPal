package com.sbeve.colorpal.recyclerview_utils

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sbeve.colorpal.databinding.GalleryImageBinding

class RecyclerViewAdapter(private val dataSet: List<Uri>, private val imageViewClickListener: ImageViewClickListener) :
    RecyclerView.Adapter<RecyclerViewAdapter.RVViewHolder>() {
    class RVViewHolder(private val binding: GalleryImageBinding, private val imageViewClickListener: ImageViewClickListener) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun inflateLayout(parent: ViewGroup, imageViewClickListener: ImageViewClickListener): RVViewHolder {
                val binding = GalleryImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)

                return RVViewHolder(binding, imageViewClickListener)
            }
        }

        fun setImageToImageView(uri: Uri) {
            Glide.with(binding.root)
                .load(uri)
                .centerCrop()
                .into(binding.galleryImage)
            binding.galleryImage.setOnClickListener {
                imageViewClickListener.onImageClick(uri)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RVViewHolder.inflateLayout(parent, imageViewClickListener)

    override fun onBindViewHolder(holder: RVViewHolder, position: Int) {
        holder.setImageToImageView(dataSet[position])
    }

    override fun getItemCount() = dataSet.size

    interface ImageViewClickListener {
        fun onImageClick(uri: Uri)
    }
}
