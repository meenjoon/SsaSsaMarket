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
    ListAdapter<ProductPostItem, InventoryInnerAdapter.InventoryProductViewHolder>(
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

        fun bind(item: ProductPostItem) {
            binding.inventoryInnerProductIv.loadFirstImage(item.imageLocations)
            binding.inventoryInnerProductTimeTv.setFormattedElapsedTime(item.createdDate)
            binding.inventoryInnerProductTitleTv.text = item.title
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

class InventoryProductDiffCallback : DiffUtil.ItemCallback<ProductPostItem>() {
    override fun areItemsTheSame(oldItem: ProductPostItem, newItem: ProductPostItem): Boolean {
        return oldItem.imageLocations == newItem.imageLocations
    }

    override fun areContentsTheSame(oldItem: ProductPostItem, newItem: ProductPostItem): Boolean {
        return oldItem == newItem
    }
}
