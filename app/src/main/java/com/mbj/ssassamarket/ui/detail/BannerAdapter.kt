package com.mbj.ssassamarket.ui.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mbj.ssassamarket.databinding.RecyclerviewItemBannerBinding

class BannerAdapter : ListAdapter<String, BannerAdapter.BannerViewHolder>(BannerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        return BannerViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    class BannerViewHolder(private val binding: RecyclerviewItemBannerBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(image: String) {
            binding.imageUrl = image
        }

        companion object {
            fun from(parent: ViewGroup): BannerViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return BannerViewHolder(
                    RecyclerviewItemBannerBinding.inflate(
                        inflater,
                        parent,
                        false
                    )
                )
            }
        }
    }

    private class BannerDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}
