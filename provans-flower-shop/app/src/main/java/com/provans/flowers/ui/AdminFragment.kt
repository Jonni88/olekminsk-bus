package com.provans.flowers.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.provans.flowers.data.FlowerData
import com.provans.flowers.data.UserManager
import com.provans.flowers.databinding.FragmentAdminBinding
import com.provans.flowers.model.Flower

class AdminFragment : Fragment() {

    private var _binding: FragmentAdminBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Проверяем, что пользователь - админ
        if (!UserManager.isAdmin()) {
            Toast.makeText(context, "Доступ запрещён", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }
        
        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        binding.addFlowerButton.setOnClickListener {
            addFlower()
        }
        
        binding.viewOrdersButton.setOnClickListener {
            // TODO: Просмотр заказов
            Toast.makeText(context, "Список заказов", Toast.LENGTH_SHORT).show()
        }
        
        binding.statisticsButton.setOnClickListener {
            // TODO: Статистика
            showStatistics()
        }
        
        // Показываем список товаров
        updateFlowersList()
    }
    
    private fun addFlower() {
        val name = binding.flowerNameInput.text.toString().trim()
        val price = binding.flowerPriceInput.text.toString().toIntOrNull() ?: 0
        val description = binding.flowerDescriptionInput.text.toString().trim()
        
        if (name.isEmpty() || price <= 0) {
            Toast.makeText(context, "Заполните название и цену", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Создаём новый цветок
        val newFlower = Flower(
            id = FlowerData.flowers.size + 1,
            name = name,
            description = description,
            price = price,
            imageUrl = "https://placehold.co/400x400/E91E63/FFFFFF?text=${name.replace(" ", "+")}",
            category = FlowerData.categories[1],
            occasion = FlowerData.occasions[1],
            colors = listOf("Разноцветный"),
            isNew = true
        )
        
        // Добавляем в список
        (FlowerData.flowers as MutableList).add(newFlower)
        
        Toast.makeText(context, "Букет \"$name\" добавлен!", Toast.LENGTH_SHORT).show()
        
        // Очищаем поля
        binding.flowerNameInput.text?.clear()
        binding.flowerPriceInput.text?.clear()
        binding.flowerDescriptionInput.text?.clear()
        
        updateFlowersList()
    }
    
    private fun showStatistics() {
        val totalFlowers = FlowerData.flowers.size
        val totalOrders = 0 // TODO
        val totalRevenue = 0 // TODO
        
        binding.statisticsText.text = """
            📊 Статистика магазина:
            
            Всего товаров: $totalFlowers
            Всего заказов: $totalOrders
            Общая выручка: $totalRevenue ₽
        """.trimIndent()
    }
    
    private fun updateFlowersList() {
        val flowersText = FlowerData.flowers.joinToString("\n") { "• ${it.name} - ${it.price} ₽" }
        binding.flowersListText.text = flowersText
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
