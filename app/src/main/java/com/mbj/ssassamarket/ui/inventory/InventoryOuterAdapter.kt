package com.mbj.ssassamarket.ui.inventory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.mbj.ssassamarket.data.model.InventoryData
import com.mbj.ssassamarket.databinding.RecyclerviewItemInventoryProductListBinding
import com.mbj.ssassamarket.databinding.RecyclerviewItemInventoryTypeBinding

private const val VIEW_TYPE_INVENTORY = 0
private const val VIEW_TYPE_PRODUCT_LIST = 1

class InventoryOuterAdapter : RecyclerView.Adapter<ViewHolder>() {

    private val inventoryDataList = mutableListOf<InventoryData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            VIEW_TYPE_INVENTORY -> InventoryTypeViewHolder.from(parent)
            VIEW_TYPE_PRODUCT_LIST -> InventoryProductListViewHolder.from(parent)
            else -> throw  java.lang.ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when(holder) {
            is InventoryTypeViewHolder -> {
                val inventoryData = inventoryDataList[position] as InventoryData.ProductType
                holder.bind(inventoryData)
            }
            is InventoryProductListViewHolder -> {
                val inventoryData = inventoryDataList[position] as InventoryData.ProductItem
                holder.bind(inventoryData)
            }
        }
    }

    override fun getItemCount(): Int {
        return inventoryDataList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (inventoryDataList[position]) {
            is InventoryData.ProductType -> VIEW_TYPE_INVENTORY
            is InventoryData.ProductItem -> VIEW_TYPE_PRODUCT_LIST
        }
    }

    fun submitList(list: List<InventoryData>) {
        inventoryDataList.clear()
        inventoryDataList.addAll(list)
        notifyDataSetChanged()
    }

    class InventoryTypeViewHolder(val binding: RecyclerviewItemInventoryTypeBinding) : ViewHolder(binding.root) {

        fun bind(categoryItem: InventoryData.ProductType) {
            binding.savedOuterType.text = categoryItem.inventoryType.label
        }

        companion object {
            fun from(parent: ViewGroup): InventoryTypeViewHolder {
                return InventoryTypeViewHolder(
                    RecyclerviewItemInventoryTypeBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }

    class InventoryProductListViewHolder(val binding: RecyclerviewItemInventoryProductListBinding) : ViewHolder(binding.root) {

        private val inventoryInnerAdapter = InventoryInnerAdapter()

        init {
            binding.inventoryInnerProductListRv.adapter = inventoryInnerAdapter
        }

        fun bind(productItem: InventoryData.ProductItem) {
            inventoryInnerAdapter.submitList(productItem.productList)
        }

        companion object {
            fun from(parent: ViewGroup): InventoryProductListViewHolder {
                return InventoryProductListViewHolder(
                    RecyclerviewItemInventoryProductListBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }
}
