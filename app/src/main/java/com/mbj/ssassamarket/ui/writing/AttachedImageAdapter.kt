package com.mbj.ssassamarket.ui.writing

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.mbj.ssassamarket.data.model.ImageContent
import com.mbj.ssassamarket.databinding.RecyclerviewItemAttachedImageBinding
import com.mbj.ssassamarket.ui.common.ImageRemoveListener

class AttachedImageAdapter(private val imageRemoveListener: ImageRemoveListener) : ListAdapter<ImageContent, AttachedImageAdapter.AttachedImageViewHolder>(ImageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachedImageViewHolder {
        return AttachedImageViewHolder.from(parent, imageRemoveListener)
    }

    override fun onBindViewHolder(holder: AttachedImageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AttachedImageViewHolder(
        private val binding: RecyclerviewItemAttachedImageBinding,
        private val imageRemoveListener: ImageRemoveListener,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(selectedImage: ImageContent) {
            binding.writingAttachedIv.load(selectedImage.uri)
            binding.writingImageRemoveIv.setOnClickListener {
                imageRemoveListener.onImageRemoveListener(selectedImage)
            }
        }

        companion object {
            fun from(
                parent: ViewGroup,
                imageRemoveListener: ImageRemoveListener
            ): AttachedImageViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return AttachedImageViewHolder(
                    RecyclerviewItemAttachedImageBinding.inflate(
                        inflater,
                        parent,
                        false,
                    ), imageRemoveListener
                )
            }
        }
    }

    private class ImageDiffCallback : DiffUtil.ItemCallback<ImageContent>() {
        override fun areItemsTheSame(oldItem: ImageContent, newItem: ImageContent): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ImageContent, newItem: ImageContent): Boolean {
            return oldItem == newItem
        }
    }
}
