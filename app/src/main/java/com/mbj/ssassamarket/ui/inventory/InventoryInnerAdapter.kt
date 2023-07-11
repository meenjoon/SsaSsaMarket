package com.mbj.ssassamarket.ui.inventory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mbj.ssassamarket.data.model.ProductPostItem
import com.mbj.ssassamarket.databinding.RecyclerviewItemInventoryProductBinding
import com.mbj.ssassamarket.ui.bindings.loadFirstImage
import com.mbj.ssassamarket.ui.bindings.setFormattedElapsedTime

class InventoryInnerAdapter :
    ListAdapter<Pair<String, ProductPostItem>, InventoryInnerAdapter.InventoryProductViewHolder>(
        InventoryProductDiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryProductViewHolder {
        return InventoryProductViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: InventoryProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class InventoryProductViewHolder(private val binding: RecyclerviewItemInventoryProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Pair<String, ProductPostItem>) {
            binding.inventoryInnerProductIv.loadFirstImage(item.second.imageLocations)
            binding.inventoryInnerProductTimeTv.setFormattedElapsedTime(item.second.createdDate)
            binding.inventoryInnerProductTitleTv.text = item.second.title
        }

        companion object {
            fun from(parent: ViewGroup): InventoryProductViewHolder {
                return InventoryProductViewHolder(
                    RecyclerviewItemInventoryProductBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }
}

class InventoryProductDiffCallback : DiffUtil.ItemCallback<Pair<String, ProductPostItem>>() {
    override fun areItemsTheSame(oldItem: Pair<String, ProductPostItem>, newItem: Pair<String, ProductPostItem>): Boolean {
        return oldItem.second.imageLocations == newItem.second.imageLocations
    }

    override fun areContentsTheSame(oldItem: Pair<String, ProductPostItem>, newItem: Pair<String, ProductPostItem>): Boolean {
        return oldItem == newItem
    }
}
