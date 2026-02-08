package com.provans.flowers.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.provans.flowers.data.CartManager
import com.provans.flowers.data.FavoritesManager
import com.provans.flowers.data.FlowerData
import com.provans.flowers.databinding.FragmentFlowerDetailBinding

class FlowerDetailFragment : Fragment() {

    private var _binding: FragmentFlowerDetailBinding? = null
    private val binding get() = _binding!!
    private var flowerId: Int = 0
    private var quantity = 1

    companion object {
        private const val ARG_FLOWER_ID = "flower_id"
        
        fun newInstance(flowerId: Int): FlowerDetailFragment {
            return FlowerDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_FLOWER_ID, flowerId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            flowerId = it.getInt(ARG_FLOWER_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFlowerDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val flower = FlowerData.getFlowerById(flowerId)
        flower?.let { loadFlowerData(it) }
        
        setupButtons()
    }

    private fun loadFlowerData(flower: com.provans.flowers.model.Flower) {
        binding.apply {
            flowerName.text = flower.name
            flowerDescription.text = flower.description
            flowerPrice.text = "${flower.price} ₽"
            
            if (flower.oldPrice != null) {
                oldPrice.visibility = View.VISIBLE
                oldPrice.text = "${flower.oldPrice} ₽"
                oldPrice.paintFlags = android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                discountBadge.visibility = View.VISIBLE
                val discount = ((flower.oldPrice - flower.price) * 100 / flower.oldPrice)
                discountBadge.text = "-$discount%"
            } else {
                oldPrice.visibility = View.GONE
                discountBadge.visibility = View.GONE
            }
            
            ratingBar.rating = flower.rating
            reviewCount.text = "(${flower.reviewCount} отзывов)"
            
            // Цвета
            colorsText.text = "Цвета: ${flower.colors.joinToString(", ")}"
            
            // Категория и повод
            categoryText.text = "Категория: ${flower.category.name}"
            occasionText.text = "Повод: ${flower.occasion.name}"
            
            // Наличие
            if (flower.inStock) {
                stockStatus.text = "✓ В наличии"
                stockStatus.setTextColor(0xFF4CAF50.toInt())
            } else {
                stockStatus.text = "✗ Нет в наличии"
                stockStatus.setTextColor(0xFFF44336.toInt())
            }
            
            // Загрузка изображения
            Glide.with(requireContext())
                .load(flower.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(flowerImage)
        }
    }

    private fun setupButtons() {
        // Назад
        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        // Уменьшить количество
        binding.decreaseButton.setOnClickListener {
            if (quantity > 1) {
                quantity--
                updateQuantity()
            }
        }
        
        // Увеличить количество
        binding.increaseButton.setOnClickListener {
            quantity++
            updateQuantity()
        }
        
        // В корзину
        binding.addToCartButton.setOnClickListener {
            val flower = FlowerData.getFlowerById(flowerId)
            flower?.let {
                CartManager.addToCart(it, quantity)
                Toast.makeText(context, "Добавлено в корзину: ${it.name} x$quantity", Toast.LENGTH_SHORT).show()
                quantity = 1
                updateQuantity()
            }
        }
        
        // Купить сейчас
        binding.buyNowButton.setOnClickListener {
            val flower = FlowerData.getFlowerById(flowerId)
            flower?.let {
                CartManager.addToCart(it, quantity)
                // Переход к оформлению заказа
                parentFragmentManager.beginTransaction()
                    .replace(com.provans.flowers.R.id.fragmentContainer, CheckoutFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
        
        // Отзывы
        binding.reviewsButton.setOnClickListener {
            val fragment = ReviewsFragment.newInstance(flowerId)
            parentFragmentManager.beginTransaction()
                .replace(com.provans.flowers.R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }
        
        // Избранное
        updateFavoriteButton()
        binding.favoriteButton?.setOnClickListener {
            FavoritesManager.toggleFavorite(flowerId)
            updateFavoriteButton()
            val isFav = FavoritesManager.isFavorite(flowerId)
            Toast.makeText(context, 
                if (isFav) "Добавлено в избранное" else "Удалено из избранного", 
                Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateFavoriteButton() {
        val isFavorite = FavoritesManager.isFavorite(flowerId)
        binding.favoriteButton?.text = if (isFavorite) "❤️ В избранном" else "🤍 Добавить в избранное"
    }

    private fun updateQuantity() {
        binding.quantityText.text = quantity.toString()
        val flower = FlowerData.getFlowerById(flowerId)
        flower?.let {
            binding.totalPrice.text = "${it.price * quantity} ₽"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
