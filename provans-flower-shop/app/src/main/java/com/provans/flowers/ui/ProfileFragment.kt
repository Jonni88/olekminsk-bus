package com.provans.flowers.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.provans.flowers.data.UserManager
import com.provans.flowers.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        updateUI()
        setupButtons()
        
        // Подписываемся на изменения
        UserManager.addListener { updateUI() }
    }

    private fun updateUI() {
        val user = UserManager.getCurrentUser()
        
        if (user != null) {
            // Пользователь авторизован
            binding.userNameText.text = user.name
            binding.userPhoneText.text = user.phone
            
            binding.loginForm.visibility = View.GONE
            binding.userMenu.visibility = View.VISIBLE
            
            // Показываем кнопку админа только для админов
            binding.adminPanelButton.visibility = if (user.isAdmin) View.VISIBLE else View.GONE
        } else {
            // Гость
            binding.userNameText.text = "Гость"
            binding.userPhoneText.text = "Войдите или зарегистрируйтесь"
            
            binding.loginForm.visibility = View.VISIBLE
            binding.userMenu.visibility = View.GONE
        }
    }

    private fun setupButtons() {
        // Вход
        binding.loginButton.setOnClickListener {
            val phone = binding.phoneInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            
            if (phone.isEmpty()) {
                binding.phoneInput.error = "Введите телефон"
                return@setOnClickListener
            }
            
            val user = UserManager.login(phone, password)
            if (user != null) {
                Toast.makeText(context, "Добро пожаловать, ${user.name}!", Toast.LENGTH_SHORT).show()
                updateUI()
            } else {
                Toast.makeText(context, "Неверный телефон или пароль", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Регистрация
        binding.registerButton.setOnClickListener {
            val name = binding.registerNameInput.text.toString().trim()
            val phone = binding.registerPhoneInput.text.toString().trim()
            
            if (name.isEmpty()) {
                binding.registerNameInput.error = "Введите имя"
                return@setOnClickListener
            }
            
            if (phone.isEmpty()) {
                binding.registerPhoneInput.error = "Введите телефон"
                return@setOnClickListener
            }
            
            if (UserManager.register(name, phone, null)) {
                Toast.makeText(context, "Регистрация успешна!", Toast.LENGTH_SHORT).show()
                updateUI()
            } else {
                Toast.makeText(context, "Пользователь с таким телефоном уже существует", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Мои заказы
        binding.myOrdersButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(com.provans.flowers.R.id.fragmentContainer, OrdersFragment())
                .addToBackStack(null)
                .commit()
        }
        
        // Избранное
        binding.favoritesButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(com.provans.flowers.R.id.fragmentContainer, FavoritesFragment())
                .addToBackStack(null)
                .commit()
        }
        
        // Адреса
        binding.addressesButton.setOnClickListener {
            Toast.makeText(context, "Мои адреса (скоро)", Toast.LENGTH_SHORT).show()
        }
        
        // Панель админа
        binding.adminPanelButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(com.provans.flowers.R.id.fragmentContainer, AdminFragment())
                .addToBackStack(null)
                .commit()
        }
        
        // Выход
        binding.logoutButton.setOnClickListener {
            UserManager.logout()
            Toast.makeText(context, "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show()
            updateUI()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
