package com.mbj.ssassamarket.ui.inventory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mbj.ssassamarket.data.model.ProductPostItem
import com.mbj.ssassamarket.databinding.RecyclerviewItemInventoryProductBinding
import com.mbj.ssassamarket.ui.common.ProductClickListener

class InventoryInnerAdapter(private val productClickListener: ProductClickListener) :
    ListAdapter<Pair<String, ProductPostItem>, InventoryInnerAdapter.InventoryProductViewHolder>(
        InventoryProductDiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryProductViewHolder {
        return InventoryProductViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: InventoryProductViewHolder, position: Int) {
        holder.bind(getItem(position), productClickListener)
    }

    class InventoryProductViewHolder(private val binding: RecyclerviewItemInventoryProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(productItem: Pair<String, ProductPostItem>, productClickListener: ProductClickListener) {
            binding.productPostItemAndPostId = productItem
            binding.productPostItem = productItem.second
            binding.productClickListener = productClickListener
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
