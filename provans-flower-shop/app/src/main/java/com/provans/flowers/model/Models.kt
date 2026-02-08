package com.provans.flowers.model

data class Flower(
    val id: Int,
    val name: String,
    val description: String,
    val price: Int,
    val oldPrice: Int? = null,
    val imageUrl: String,
    val category: Category,
    val occasion: Occasion,
    val colors: List<String>,
    val inStock: Boolean = true,
    val rating: Float = 4.5f,
    val reviewCount: Int = 0,
    val isBestseller: Boolean = false,
    val isNew: Boolean = false
)

data class Category(
    val id: Int,
    val name: String,
    val icon: String
)

data class Occasion(
    val id: Int,
    val name: String
)

data class CartItem(
    val flower: Flower,
    var quantity: Int = 1,
    var selected: Boolean = true
)

data class Order(
    val id: String,
    val items: List<CartItem>,
    val totalPrice: Int,
    val deliveryType: DeliveryType,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String?,
    val deliveryDate: String,
    val deliveryTime: String,
    val comment: String?,
    val paymentMethod: PaymentMethod,
    val status: OrderStatus = OrderStatus.NEW,
    val createdAt: Long = System.currentTimeMillis()
)

data class Review(
    val id: Int,
    val flowerId: Int,
    val authorName: String,
    val rating: Float,
    val text: String,
    val date: String
)

data class User(
    val id: Int,
    val name: String,
    val phone: String,
    val email: String? = null,
    val addresses: List<String> = emptyList(),
    val favoriteFlowers: List<Int> = emptyList(),
    val isAdmin: Boolean = false
)

enum class DeliveryType {
    DELIVERY, PICKUP
}

enum class PaymentMethod {
    CASH, CARD_ONLINE, CARD_COURIER, SBP_TRANSFER
}

enum class OrderStatus {
    NEW, CONFIRMED, IN_PROGRESS, READY, DELIVERED, CANCELLED
}
