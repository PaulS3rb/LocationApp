package com.example.locationapp.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {

    enum class AuthView { SIGNUP, LOGIN }

    private val _currentView = MutableStateFlow(AuthView.SIGNUP)
    val currentView = _currentView.asStateFlow()

    val loginEmail = MutableStateFlow("")
    val loginPassword = MutableStateFlow("")
    val signupName = MutableStateFlow("")
    val signupEmail = MutableStateFlow("")
    val signupPassword = MutableStateFlow("")
    val signupConfirmPassword = MutableStateFlow("")

    fun switchView(view: AuthView) {
        _currentView.value = view
    }

    fun handleLogin() {
        // TODO: implement your login logic (e.g., SQLite or Firebase)
        println("Login: ${loginEmail.value}, ${loginPassword.value}")
    }

    fun handleSignup() {
        // TODO: implement your signup logic
        println("Signup: ${signupName.value}, ${signupEmail.value}")
    }
}
