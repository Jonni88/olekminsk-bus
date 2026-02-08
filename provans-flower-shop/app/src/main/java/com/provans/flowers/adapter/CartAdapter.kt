package com.provans.flowers.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.provans.flowers.R
import com.provans.flowers.model.CartItem

class CartAdapter(
    private val onQuantityChange: (Int, Int) -> Unit,
    private val onRemove: (Int) -> Unit,
    private val onToggleSelect: (Int) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(CartDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view, onQuantityChange, onRemove, onToggleSelect)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CartViewHolder(
        itemView: View,
        private val onQuantityChange: (Int, Int) -> Unit,
        private val onRemove: (Int) -> Unit,
        private val onToggleSelect: (Int) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val checkbox: CheckBox = itemView.findViewById(R.id.itemCheckbox)
        private val imageView: ImageView = itemView.findViewById(R.id.flowerImage)
        private val nameView: TextView = itemView.findViewById(R.id.flowerName)
        private val priceView: TextView = itemView.findViewById(R.id.itemPrice)
        private val quantityView: TextView = itemView.findViewById(R.id.quantityText)
        private val decreaseBtn: Button = itemView.findViewById(R.id.decreaseButton)
        private val increaseBtn: Button = itemView.findViewById(R.id.increaseButton)
        private val removeBtn: ImageButton = itemView.findViewById(R.id.removeButton)

        fun bind(item: CartItem) {
            checkbox.isChecked = item.selected
            checkbox.setOnCheckedChangeListener { _, _ ->
                onToggleSelect(item.flower.id)
            }
            
            nameView.text = item.flower.name
            priceView.text = "${item.flower.price * item.quantity} ₽"
            quantityView.text = item.quantity.toString()
            
            decreaseBtn.setOnClickListener {
                onQuantityChange(item.flower.id, item.quantity - 1)
            }
            
            increaseBtn.setOnClickListener {
                onQuantityChange(item.flower.id, item.quantity + 1)
            }
            
            removeBtn.setOnClickListener {
                onRemove(item.flower.id)
            }
            
            Glide.with(itemView.context)
                .load(item.flower.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(imageView)
        }
    }

    class CartDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.flower.id == newItem.flower.id
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem
        }
    }
}
