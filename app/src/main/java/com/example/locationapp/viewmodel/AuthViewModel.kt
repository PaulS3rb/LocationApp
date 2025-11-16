package com.example.locationapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.locationapp.dao.UserDao
import com.example.locationapp.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val userDao: UserDao) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    fun signup(userName: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            if (password != confirmPassword) {
                _authState.value = AuthState.Error("Passwords do not match.")
                return@launch
            }
            if (userName.isBlank() || email.isBlank() || password.isBlank()) {
                _authState.value = AuthState.Error("Please fill all fields.")
                return@launch
            }

            _authState.value = AuthState.Loading
            if (userDao.findByEmail(email) != null) {
                _authState.value = AuthState.Error("User with this email already exists.")
                return@launch
            }

            // In a real app, hash the password before saving
            val user = User(userName = userName, email = email, password = password, points = 0f)
            userDao.insert(user)
            _authState.value = AuthState.Success("Signup successful!")
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            if (email.isBlank() || password.isBlank()) {
                _authState.value = AuthState.Error("Please enter email and password.")
                return@launch
            }

            _authState.value = AuthState.Loading
            val user = userDao.findByEmail(email)

            if (user == null) {
                _authState.value = AuthState.Error("Invalid email or password.")
            } else {
                if (user.password == password) {
                    _authState.value = AuthState.Success("Login successful!")
                } else {
                    _authState.value = AuthState.Error("Invalid email or password.")
                }
            }
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}

class AuthViewModelFactory(private val userDao: UserDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(userDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
