package com.mbj.ssassamarket.ui.home

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.load
import coil.transform.RoundedCornersTransformation
import com.google.firebase.storage.FirebaseStorage
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.data.model.ProductPostItem
import com.mbj.ssassamarket.databinding.RecyclerviewItemHomeProductBinding
import com.mbj.ssassamarket.util.DateFormat.getFormattedElapsedTime

class HomeAdapter : ListAdapter<ProductPostItem, HomeAdapter.HomeViewHolder>(homeDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        return HomeViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    class HomeViewHolder(val binding: RecyclerviewItemHomeProductBinding) : ViewHolder(binding.root) {

        fun bind(productPostItem: ProductPostItem) {
            loadFirstImage(productPostItem)
            binding.homeProductTitleTv.text = productPostItem.title
            binding.homeProductTimeTv.text = getFormattedElapsedTime(productPostItem.createdDate)
            binding.homeProductPriceTv.text = productPostItem.price.toString()
            binding.homeProductLocation.text = productPostItem.location
        }

        private fun loadFirstImage(postItem: ProductPostItem) {
            val firstImageLocation = postItem.imageLocations?.firstOrNull()
            val storageRef = FirebaseStorage.getInstance().reference
            if (firstImageLocation != null) {
                val imageRef = storageRef.child(firstImageLocation)
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val roundedCorners = RoundedCornersTransformation(15f)
                    binding.homeProductIv.load(uri.toString()) {
                        transformations(roundedCorners)
                        error(R.drawable.null_icon)
                    }
                }.addOnFailureListener { exception ->
                    Log.e("HomeAdapter", "Failed to load image: ${exception.message}")
                }
            } else {
                binding.homeProductIv.load(R.drawable.null_icon)
            }
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
        private val homeDiffCallback = object : DiffUtil.ItemCallback<ProductPostItem>() {
            override fun areItemsTheSame(
                oldItem: ProductPostItem,
                newItem: ProductPostItem
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: ProductPostItem,
                newItem: ProductPostItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
