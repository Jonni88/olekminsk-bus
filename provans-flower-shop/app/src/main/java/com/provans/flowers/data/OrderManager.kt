package com.provans.flowers.data

import com.provans.flowers.model.Order
import com.provans.flowers.model.OrderStatus

object OrderManager {
    private val orders = mutableListOf<Order>()
    private var listeners = mutableListOf<() -> Unit>()
    
    fun addOrder(order: Order) {
        orders.add(order)
        notifyListeners()
    }
    
    fun getOrders(): List<Order> = orders.toList()
    
    fun getOrderById(id: String): Order? = orders.find { it.id == id }
    
    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        val order = orders.find { it.id == orderId }
        order?.let {
            // Создаём новый заказ с обновлённым статусом
            val index = orders.indexOf(it)
            orders[index] = it.copy(status = newStatus)
            notifyListeners()
        }
    }
    
    fun getOrdersByStatus(status: OrderStatus): List<Order> {
        return orders.filter { it.status == status }
    }
    
    fun addListener(listener: () -> Unit) {
        listeners.add(listener)
    }
    
    fun removeListener(listener: () -> Unit) {
        listeners.remove(listener)
    }
    
    private fun notifyListeners() {
        listeners.forEach { it() }
    }
}
