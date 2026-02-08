package com.provans.flowers.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.provans.flowers.adapter.FlowerAdapter
import com.provans.flowers.data.FlowerData
import com.provans.flowers.databinding.FragmentCatalogBinding

class CatalogFragment : Fragment() {

    private var _binding: FragmentCatalogBinding? = null
    private val binding get() = _binding!!
    private lateinit var flowerAdapter: FlowerAdapter
    private var currentCategoryId = 1
    private var currentOccasionId = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupCategoryFilter()
        setupOccasionFilter()
        setupSortOptions()
        setupBanners()
        
        loadFlowers()
    }

    private fun setupRecyclerView() {
        flowerAdapter = FlowerAdapter { flower ->
            // Открываем детали товара
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

    private fun setupCategoryFilter() {
        val categories = FlowerData.categories
        val categoryNames = categories.map { "${it.icon} ${it.name}" }.toTypedArray()
        
        binding.categoryChipGroup.removeAllViews()
        
        categories.forEachIndexed { index, category ->
            val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                text = category.name
                isCheckable = true
                isChecked = index == 0
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        currentCategoryId = category.id
                        loadFlowers()
                    }
                }
            }
            binding.categoryChipGroup.addView(chip)
        }
    }

    private fun setupOccasionFilter() {
        val occasions = FlowerData.occasions
        
        binding.occasionChipGroup.removeAllViews()
        
        occasions.forEach { occasion ->
            val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                text = occasion.name
                isCheckable = true
                isChecked = occasion.id == 1
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        currentOccasionId = occasion.id
                        loadFlowers()
                    }
                }
            }
            binding.occasionChipGroup.addView(chip)
        }
    }

    private fun setupSortOptions() {
        binding.sortButton.setOnClickListener {
            // Показать диалог сортировки
            showSortDialog()
        }
    }

    private fun showSortDialog() {
        val options = arrayOf("По популярности", "Сначала дешёвые", "Сначала дорогие", "По рейтингу")
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Сортировка")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> sortByPopularity()
                    1 -> sortByPriceAsc()
                    2 -> sortByPriceDesc()
                    3 -> sortByRating()
                }
            }
            .show()
    }

    private fun sortByPopularity() {
        val sorted = flowerAdapter.currentList.sortedByDescending { it.reviewCount }
        flowerAdapter.submitList(sorted)
    }

    private fun sortByPriceAsc() {
        val sorted = flowerAdapter.currentList.sortedBy { it.price }
        flowerAdapter.submitList(sorted)
    }

    private fun sortByPriceDesc() {
        val sorted = flowerAdapter.currentList.sortedByDescending { it.price }
        flowerAdapter.submitList(sorted)
    }

    private fun sortByRating() {
        val sorted = flowerAdapter.currentList.sortedByDescending { it.rating }
        flowerAdapter.submitList(sorted)
    }

    private fun setupBanners() {
        // Баннеры: Хиты, Новинки, Скидки
        binding.bannerHits.setOnClickListener {
            flowerAdapter.submitList(FlowerData.getBestsellers())
        }
        
        binding.bannerNew.setOnClickListener {
            flowerAdapter.submitList(FlowerData.getNewArrivals())
        }
        
        binding.bannerSale.setOnClickListener {
            flowerAdapter.submitList(FlowerData.getDiscounted())
        }
    }

    private fun loadFlowers() {
        var flowers = if (currentCategoryId == 1) {
            FlowerData.flowers
        } else {
            FlowerData.getFlowersByCategory(currentCategoryId)
        }
        
        if (currentOccasionId != 1) {
            flowers = flowers.filter { it.occasion.id == currentOccasionId }
        }
        
        flowerAdapter.submitList(flowers)
        binding.resultsCount.text = "${flowers.size} товаров"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
