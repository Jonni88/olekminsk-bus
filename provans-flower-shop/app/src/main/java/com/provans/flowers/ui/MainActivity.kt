package com.provans.flowers.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.provans.flowers.R
import com.provans.flowers.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentFragmentId = R.id.nav_catalog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
        
        // Открываем каталог по умолчанию
        if (savedInstanceState == null) {
            openFragment(CatalogFragment())
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_catalog -> {
                    currentFragmentId = R.id.nav_catalog
                    openFragment(CatalogFragment())
                    true
                }
                R.id.nav_cart -> {
                    currentFragmentId = R.id.nav_cart
                    openFragment(CartFragment())
                    true
                }
                R.id.nav_orders -> {
                    currentFragmentId = R.id.nav_orders
                    openFragment(OrdersFragment())
                    true
                }
                R.id.nav_profile -> {
                    currentFragmentId = R.id.nav_profile
                    openFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun openFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun navigateToCart() {
        binding.bottomNavigation.selectedItemId = R.id.nav_cart
    }

    fun navigateToCatalog() {
        binding.bottomNavigation.selectedItemId = R.id.nav_catalog
    }
}
