package com.provans.flowers.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.provans.flowers.data.CartManager
import com.provans.flowers.data.OrderManager
import com.provans.flowers.databinding.FragmentCheckoutBinding
import com.provans.flowers.model.DeliveryType
import com.provans.flowers.model.Order
import com.provans.flowers.model.PaymentMethod
import java.util.*

class CheckoutFragment : Fragment() {

    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!
    private var deliveryType = DeliveryType.DELIVERY
    private var paymentMethod = PaymentMethod.CASH
    private var selectedDate = ""
    private var selectedTime = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Проверяем, что есть товары в корзине
        if (CartManager.getSelectedItems().isEmpty()) {
            Toast.makeText(context, "Корзина пуста. Добавьте товары для оформления заказа.", Toast.LENGTH_LONG).show()
            parentFragmentManager.popBackStack()
            return
        }
        
        setupOrderSummary()
        setupDeliveryOptions()
        setupPaymentOptions()
        setupDateTimePickers()
        setupButtons()
    }

    private fun setupOrderSummary() {
        val items = CartManager.getSelectedItems()
        val totalPrice = CartManager.getTotalPrice()
        
        binding.orderSummaryText.text = items.joinToString("\n") {
            "${it.flower.name} x${it.quantity} = ${it.flower.price * it.quantity} ₽"
        }
        
        binding.totalPrice.text = "$totalPrice ₽"
    }

    private fun setupDeliveryOptions() {
        binding.deliveryTypeGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                com.provans.flowers.R.id.radioDelivery -> {
                    deliveryType = DeliveryType.DELIVERY
                    binding.addressInput.visibility = View.VISIBLE
                }
                com.provans.flowers.R.id.radioPickup -> {
                    deliveryType = DeliveryType.PICKUP
                    binding.addressInput.visibility = View.GONE
                }
            }
        }
    }

    private fun setupPaymentOptions() {
        binding.paymentMethodGroup.setOnCheckedChangeListener { _, checkedId ->
            paymentMethod = when (checkedId) {
                com.provans.flowers.R.id.radioCash -> PaymentMethod.CASH
                com.provans.flowers.R.id.radioCardOnline -> PaymentMethod.CARD_ONLINE
                com.provans.flowers.R.id.radioCardCourier -> PaymentMethod.CARD_COURIER
                com.provans.flowers.R.id.radioSbpTransfer -> PaymentMethod.SBP_TRANSFER
                else -> PaymentMethod.CASH
            }
        }
    }

    private fun setupDateTimePickers() {
        // Дата
        binding.dateButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    selectedDate = String.format("%02d.%02d.%d", day, month + 1, year)
                    binding.dateButton.text = selectedDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        
        // Время
        binding.timeButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                requireContext(),
                { _, hour, minute ->
                    selectedTime = String.format("%02d:%02d", hour, minute)
                    binding.timeButton.text = selectedTime
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }
    }

    private fun setupButtons() {
        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        binding.placeOrderButton.setOnClickListener {
            if (validateOrder()) {
                placeOrder()
            }
        }
    }

    private fun validateOrder(): Boolean {
        val name = binding.nameInput.text.toString().trim()
        val phone = binding.phoneInput.text.toString().trim()
        val address = binding.addressInput.text.toString().trim()
        
        if (name.isEmpty()) {
            binding.nameInput.error = "Введите имя"
            return false
        }
        
        if (phone.isEmpty()) {
            binding.phoneInput.error = "Введите телефон"
            return false
        }
        
        if (deliveryType == DeliveryType.DELIVERY && address.isEmpty()) {
            binding.addressInput.error = "Введите адрес доставки"
            return false
        }
        
        if (selectedDate.isEmpty()) {
            Toast.makeText(context, "Выберите дату доставки", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (selectedTime.isEmpty()) {
            Toast.makeText(context, "Выберите время доставки", Toast.LENGTH_SHORT).show()
            return false
        }
        
        return true
    }

    private fun placeOrder() {
        val selectedItems = CartManager.getSelectedItems()
        if (selectedItems.isEmpty()) {
            Toast.makeText(context, "Нет выбранных товаров", Toast.LENGTH_SHORT).show()
            return
        }

        val order = Order(
            id = UUID.randomUUID().toString().substring(0, 8).uppercase(),
            items = selectedItems,
            totalPrice = CartManager.getTotalPrice(),
            deliveryType = deliveryType,
            customerName = binding.nameInput.text.toString(),
            customerPhone = binding.phoneInput.text.toString(),
            deliveryAddress = if (deliveryType == DeliveryType.DELIVERY)
                binding.addressInput.text.toString() else null,
            deliveryDate = selectedDate,
            deliveryTime = selectedTime,
            comment = binding.commentInput.text.toString().ifEmpty { null },
            paymentMethod = paymentMethod
        )

        // Очищаем корзину
        CartManager.clearSelected()

        // Сохраняем заказ в историю
        OrderManager.addOrder(order)

        // Если выбран перевод СБП - показываем экран с реквизитами
        if (paymentMethod == PaymentMethod.SBP_TRANSFER) {
            parentFragmentManager.beginTransaction()
                .replace(
                    com.provans.flowers.R.id.fragmentContainer,
                    PaymentTransferFragment.newInstance(order.id, order.totalPrice)
                )
                .addToBackStack(null)
                .commit()
        } else {
            // Показываем подтверждение
            Toast.makeText(context, "Заказ №${order.id} оформлен!", Toast.LENGTH_LONG).show()

            // Переходим к заказам
            parentFragmentManager.beginTransaction()
                .replace(
                    com.provans.flowers.R.id.fragmentContainer,
                    OrderSuccessFragment.newInstance(order.id)
                )
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
