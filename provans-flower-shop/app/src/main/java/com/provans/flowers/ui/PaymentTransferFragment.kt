package com.provans.flowers.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.provans.flowers.databinding.FragmentPaymentTransferBinding

class PaymentTransferFragment : Fragment() {

    private var _binding: FragmentPaymentTransferBinding? = null
    private val binding get() = _binding!!
    private var orderId: String = ""
    private var totalPrice: Int = 0

    companion object {
        private const val ARG_ORDER_ID = "order_id"
        private const val ARG_TOTAL_PRICE = "total_price"

        fun newInstance(orderId: String, totalPrice: Int): PaymentTransferFragment {
            return PaymentTransferFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ORDER_ID, orderId)
                    putInt(ARG_TOTAL_PRICE, totalPrice)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            orderId = it.getString(ARG_ORDER_ID, "")
            totalPrice = it.getInt(ARG_TOTAL_PRICE, 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentTransferBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Отображаем сумму и номер заказа
        binding.orderInfoText.text = "Заказ №$orderId\nСумма: $totalPrice ₽"

        // Реквизиты для перевода (пример)
        val phoneNumber = "+79001234567" // Телефон для СБП
        val cardNumber = "4276123456789012" // Номер карты
        val recipientName = "Иванов Иван Иванович" // Получатель

        binding.sbpPhoneText.text = phoneNumber
        binding.cardNumberText.text = cardNumber
        binding.recipientNameText.text = recipientName

        // Кнопки копирования
        binding.copyPhoneButton.setOnClickListener {
            copyToClipboard("Телефон", phoneNumber)
        }

        binding.copyCardButton.setOnClickListener {
            copyToClipboard("Номер карты", cardNumber)
        }

        // Кнопка "Я оплатил"
        binding.paidButton.setOnClickListener {
            Toast.makeText(context, "Спасибо! Мы проверим оплату и свяжемся с вами.", Toast.LENGTH_LONG).show()
            parentFragmentManager.beginTransaction()
                .replace(com.provans.flowers.R.id.fragmentContainer, OrdersFragment())
                .commit()
        }

        // Кнопка назад
        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun copyToClipboard(label: String, text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label скопирован", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
