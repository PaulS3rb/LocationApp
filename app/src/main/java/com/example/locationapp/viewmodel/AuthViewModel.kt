package com.example.locationapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.locationapp.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val repo: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(FirebaseAuth.getInstance().currentUser != null)
    val isAuthenticated = _isAuthenticated.asStateFlow()


    fun signup(userName: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            if (password != confirmPassword) {
                _authState.value = AuthState.Error("Passwords do not match.")
                return@launch
            }

            _authState.value = AuthState.Loading

            val result = repo.signup(userName, email, password)

            result.onSuccess {
                _authState.value = AuthState.Success("Successfully authenticated")
                _isAuthenticated.value = true
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Unknown error")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repo.login(email, password)

            result.onSuccess {
                _authState.value = AuthState.Success("Successfully authenticated")
                _isAuthenticated.value = true
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Invalid email or password.")
            }
        }
    }

  fun logout() {
        repo.logout()
        _isAuthenticated.value = false
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }


}

class AuthViewModelFactory(private val repo: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}
