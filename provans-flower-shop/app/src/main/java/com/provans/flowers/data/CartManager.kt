package com.provans.flowers.data

import com.provans.flowers.model.CartItem
import com.provans.flowers.model.Flower

object CartManager {
    private val cartItems = mutableListOf<CartItem>()
    private var listeners = mutableListOf<() -> Unit>()
    
    fun addToCart(flower: Flower, quantity: Int = 1) {
        val existingItem = cartItems.find { it.flower.id == flower.id }
        if (existingItem != null) {
            existingItem.quantity += quantity
        } else {
            cartItems.add(CartItem(flower, quantity))
        }
        notifyListeners()
    }
    
    fun removeFromCart(flowerId: Int) {
        cartItems.removeAll { it.flower.id == flowerId }
        notifyListeners()
    }
    
    fun updateQuantity(flowerId: Int, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(flowerId)
            return
        }
        val item = cartItems.find { it.flower.id == flowerId }
        item?.let {
            it.quantity = quantity
            notifyListeners()
        }
    }
    
    fun toggleSelection(flowerId: Int) {
        val item = cartItems.find { it.flower.id == flowerId }
        item?.let {
            it.selected = !it.selected
            notifyListeners()
        }
    }
    
    fun getCartItems(): List<CartItem> = cartItems.toList()
    
    fun getSelectedItems(): List<CartItem> = cartItems.filter { it.selected }
    
    fun getTotalPrice(): Int {
        return cartItems.filter { it.selected }.sumOf { it.flower.price * it.quantity }
    }
    
    fun getTotalCount(): Int {
        return cartItems.sumOf { it.quantity }
    }
    
    fun clearCart() {
        cartItems.clear()
        notifyListeners()
    }
    
    fun clearSelected() {
        cartItems.removeAll { it.selected }
        notifyListeners()
    }
    
    fun isInCart(flowerId: Int): Boolean {
        return cartItems.any { it.flower.id == flowerId }
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
