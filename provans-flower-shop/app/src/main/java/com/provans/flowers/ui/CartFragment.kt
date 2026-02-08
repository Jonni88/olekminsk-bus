package com.provans.flowers.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.provans.flowers.adapter.CartAdapter
import com.provans.flowers.data.CartManager
import com.provans.flowers.databinding.FragmentCartBinding

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private lateinit var cartAdapter: CartAdapter
    private val cartListener = { updateCartData() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupButtons()
        updateCartData()
        
        // Подписываемся на изменения корзины
        CartManager.addListener(cartListener)
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onQuantityChange = { flowerId, quantity ->
                CartManager.updateQuantity(flowerId, quantity)
            },
            onRemove = { flowerId ->
                CartManager.removeFromCart(flowerId)
            },
            onToggleSelect = { flowerId ->
                CartManager.toggleSelection(flowerId)
            }
        )
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = cartAdapter
        }
    }

    private fun setupButtons() {
        // Выбрать все
        binding.selectAllCheckbox.setOnCheckedChangeListener { _, isChecked ->
            CartManager.getCartItems().forEach {
                if (it.selected != isChecked) {
                    CartManager.toggleSelection(it.flower.id)
                }
            }
        }
        
        // Удалить выбранные
        binding.deleteSelectedButton.setOnClickListener {
            CartManager.clearSelected()
        }
        
        // Оформить заказ
        binding.checkoutButton.setOnClickListener {
            if (CartManager.getSelectedItems().isNotEmpty()) {
                parentFragmentManager.beginTransaction()
                    .replace(com.provans.flowers.R.id.fragmentContainer, CheckoutFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
        
        // Продолжить покупки
        binding.continueShoppingButton.setOnClickListener {
            (activity as? MainActivity)?.navigateToCatalog()
        }
    }

    private fun updateCartData() {
        // Проверяем, что binding не null
        if (_binding == null) return
        
        val items = CartManager.getCartItems()
        
        if (items.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.content.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.content.visibility = View.VISIBLE
            
            cartAdapter.submitList(items.toList())
            
            // Обновляем итоги
            val totalPrice = CartManager.getTotalPrice()
            val selectedCount = CartManager.getSelectedItems().sumOf { it.quantity }
            
            binding.totalPrice.text = "$totalPrice ₽"
            binding.checkoutButton.text = "Оформить заказ ($selectedCount шт.)"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        CartManager.removeListener(cartListener)
        _binding = null
    }
}
