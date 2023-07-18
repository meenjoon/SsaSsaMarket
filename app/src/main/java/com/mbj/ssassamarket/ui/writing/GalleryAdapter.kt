package com.mbj.ssassamarket.ui.writing

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.mbj.ssassamarket.databinding.RecyclerviewItemGalleryBinding
import com.mbj.ssassamarket.ui.common.GalleryClickListener

class GalleryAdapter(private val galleryClickListener: GalleryClickListener) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    private var selectedImageContentSize = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        return GalleryViewHolder.from(parent, galleryClickListener)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        holder.bind(selectedImageContentSize)
    }

    override fun getItemCount(): Int {
        return 1
    }

    fun updateSelectedImageContentSize(size: Int) {
        selectedImageContentSize = size
        notifyItemChanged(0)
    }

    class GalleryViewHolder(
        private val binding: RecyclerviewItemGalleryBinding,
        private val galleryClickListener: GalleryClickListener,
    ) : ViewHolder(binding.root) {

        fun bind(selectedImageContentSize: Int) {
            binding.selectedImageContentSize = selectedImageContentSize
            binding.galleryClickListener = galleryClickListener
        }

        companion object {
            fun from(
                parent: ViewGroup,
                galleryClickListener: GalleryClickListener,
            ): GalleryViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return GalleryViewHolder(
                    RecyclerviewItemGalleryBinding.inflate(
                        inflater,
                        parent,
                        false
                    ), galleryClickListener
                )
            }
        }
    }
}
