package com.provans.flowers.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.provans.flowers.R
import com.provans.flowers.model.Flower

class FlowerAdapter(
    private val onFlowerClick: (Flower) -> Unit
) : ListAdapter<Flower, FlowerAdapter.FlowerViewHolder>(FlowerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlowerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_flower, parent, false)
        return FlowerViewHolder(view, onFlowerClick)
    }

    override fun onBindViewHolder(holder: FlowerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class FlowerViewHolder(
        itemView: View,
        private val onFlowerClick: (Flower) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val imageView: ImageView = itemView.findViewById(R.id.flowerImage)
        private val nameView: TextView = itemView.findViewById(R.id.flowerName)
        private val priceView: TextView = itemView.findViewById(R.id.flowerPrice)
        private val oldPriceView: TextView = itemView.findViewById(R.id.oldPrice)
        private val ratingView: TextView = itemView.findViewById(R.id.ratingText)
        private val badgeNew: View = itemView.findViewById(R.id.badgeNew)
        private val badgeSale: View = itemView.findViewById(R.id.badgeSale)

        fun bind(flower: Flower) {
            itemView.setOnClickListener { onFlowerClick(flower) }
            
            nameView.text = flower.name
            priceView.text = "${flower.price} ₽"
            ratingView.text = "★ ${flower.rating}"
            
            if (flower.oldPrice != null) {
                oldPriceView.visibility = View.VISIBLE
                oldPriceView.text = "${flower.oldPrice} ₽"
                oldPriceView.paintFlags = android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                badgeSale.visibility = View.VISIBLE
            } else {
                oldPriceView.visibility = View.GONE
                badgeSale.visibility = View.GONE
            }
            
            badgeNew.visibility = if (flower.isNew) View.VISIBLE else View.GONE
            
            Glide.with(itemView.context)
                .load(flower.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(imageView)
        }
    }

    class FlowerDiffCallback : DiffUtil.ItemCallback<Flower>() {
        override fun areItemsTheSame(oldItem: Flower, newItem: Flower): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Flower, newItem: Flower): Boolean {
            return oldItem == newItem
        }
    }
}
