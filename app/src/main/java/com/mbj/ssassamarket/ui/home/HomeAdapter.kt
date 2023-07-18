package com.mbj.ssassamarket.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.mbj.ssassamarket.data.model.ProductPostItem
import com.mbj.ssassamarket.databinding.RecyclerviewItemHomeProductBinding
import com.mbj.ssassamarket.ui.common.ProductClickListener

class HomeAdapter(private val productClickListener: ProductClickListener) : ListAdapter<Pair<String,ProductPostItem>, HomeAdapter.HomeViewHolder>(homeDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        return HomeViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        holder.bind(currentList[position], productClickListener)
    }

    class HomeViewHolder(val binding: RecyclerviewItemHomeProductBinding) : ViewHolder(binding.root) {

        fun bind(productPostItem: Pair<String, ProductPostItem>, productClickListener: ProductClickListener) {
            binding.productPostItemAndPostId = productPostItem
            binding.productPostItem = productPostItem.second
            binding.productClickListener = productClickListener
        }

        companion object {
            fun from(parent: ViewGroup): HomeViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return HomeViewHolder(
                    RecyclerviewItemHomeProductBinding.inflate(
                        inflater,
                        parent,
                        false
                    )
                )
            }
        }
    }

    companion object {
        private val homeDiffCallback = object : DiffUtil.ItemCallback<Pair<String,ProductPostItem>>() {
            override fun areItemsTheSame(
                oldItem: Pair<String,ProductPostItem>,
                newItem: Pair<String,ProductPostItem>
            ): Boolean {
                return oldItem.first == newItem.first
            }

            override fun areContentsTheSame(
                oldItem: Pair<String,ProductPostItem>,
                newItem: Pair<String,ProductPostItem>
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
