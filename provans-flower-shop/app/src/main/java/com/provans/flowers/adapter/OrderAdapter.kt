package com.provans.flowers.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.provans.flowers.R
import com.provans.flowers.model.Order
import com.provans.flowers.model.OrderStatus

class OrderAdapter(
    private val onOrderClick: (Order) -> Unit
) : ListAdapter<Order, OrderAdapter.OrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view, onOrderClick)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class OrderViewHolder(
        itemView: View,
        private val onOrderClick: (Order) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val orderIdView: TextView = itemView.findViewById(R.id.orderId)
        private val orderDateView: TextView = itemView.findViewById(R.id.orderDate)
        private val orderStatusView: TextView = itemView.findViewById(R.id.orderStatus)
        private val orderTotalView: TextView = itemView.findViewById(R.id.orderTotal)
        private val orderItemsView: TextView = itemView.findViewById(R.id.orderItems)

        fun bind(order: Order) {
            itemView.setOnClickListener { onOrderClick(order) }
            
            orderIdView.text = "Заказ №${order.id}"
            orderDateView.text = "${order.deliveryDate} ${order.deliveryTime}"
            orderStatusView.text = getStatusText(order.status)
            orderTotalView.text = "${order.totalPrice} ₽"
            
            val itemsText = order.items.joinToString(", ") { 
                "${it.flower.name} x${it.quantity}" 
            }
            orderItemsView.text = itemsText
            
            // Цвет статуса
            val statusColor = when (order.status) {
                OrderStatus.NEW -> 0xFF2196F3.toInt()
                OrderStatus.CONFIRMED -> 0xFF4CAF50.toInt()
                OrderStatus.IN_PROGRESS -> 0xFFFF9800.toInt()
                OrderStatus.READY -> 0xFF9C27B0.toInt()
                OrderStatus.DELIVERED -> 0xFF4CAF50.toInt()
                OrderStatus.CANCELLED -> 0xFFF44336.toInt()
            }
            orderStatusView.setTextColor(statusColor)
        }
        
        private fun getStatusText(status: OrderStatus): String {
            return when (status) {
                OrderStatus.NEW -> "Новый"
                OrderStatus.CONFIRMED -> "Подтверждён"
                OrderStatus.IN_PROGRESS -> "В работе"
                OrderStatus.READY -> "Готов"
                OrderStatus.DELIVERED -> "Доставлен"
                OrderStatus.CANCELLED -> "Отменён"
            }
        }
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }
}
