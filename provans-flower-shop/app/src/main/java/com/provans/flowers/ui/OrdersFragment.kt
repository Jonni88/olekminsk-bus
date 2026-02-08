package com.provans.flowers.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.provans.flowers.adapter.OrderAdapter
import com.provans.flowers.data.OrderManager
import com.provans.flowers.databinding.FragmentOrdersBinding

class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!
    private lateinit var orderAdapter: OrderAdapter
    private val orderListener = { updateOrdersList() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        updateOrdersList()
        
        OrderManager.addListener(orderListener)
        
        binding.newOrderButton.setOnClickListener {
            (activity as? MainActivity)?.navigateToCatalog()
        }
    }
    
    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter { order ->
            // Показать детали заказа
            // TODO: Открыть детали заказа
        }
        
        binding.ordersRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = orderAdapter
        }
    }
    
    private fun updateOrdersList() {
        if (_binding == null) return
        
        val orders = OrderManager.getOrders()
        
        if (orders.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.ordersRecyclerView.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.ordersRecyclerView.visibility = View.VISIBLE
            orderAdapter.submitList(orders)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        OrderManager.removeListener(orderListener)
        _binding = null
    }
}
