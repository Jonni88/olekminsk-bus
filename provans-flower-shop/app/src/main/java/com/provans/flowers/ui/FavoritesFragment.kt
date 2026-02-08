package com.provans.flowers.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.provans.flowers.adapter.FlowerAdapter
import com.provans.flowers.data.FavoritesManager
import com.provans.flowers.data.FlowerData
import com.provans.flowers.databinding.FragmentFavoritesBinding

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private lateinit var flowerAdapter: FlowerAdapter
    private val favoritesListener = { updateFavoritesList() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        updateFavoritesList()
        
        FavoritesManager.addListener(favoritesListener)
        
        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        binding.clearButton.setOnClickListener {
            FavoritesManager.clear()
            Toast.makeText(context, "Избранное очищено", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupRecyclerView() {
        flowerAdapter = FlowerAdapter { flower ->
            val fragment = FlowerDetailFragment.newInstance(flower.id)
            parentFragmentManager.beginTransaction()
                .replace(com.provans.flowers.R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }
        
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = flowerAdapter
        }
    }
    
    private fun updateFavoritesList() {
        if (_binding == null) return
        
        val favoriteIds = FavoritesManager.getFavorites()
        val favoriteFlowers = FlowerData.flowers.filter { it.id in favoriteIds }
        
        if (favoriteFlowers.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
            binding.clearButton.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
            binding.clearButton.visibility = View.VISIBLE
            flowerAdapter.submitList(favoriteFlowers)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        FavoritesManager.removeListener(favoritesListener)
        _binding = null
    }
}
