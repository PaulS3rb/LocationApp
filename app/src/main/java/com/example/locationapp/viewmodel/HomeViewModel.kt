package com.example.locationapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.locationapp.model.User
import com.example.locationapp.repository.AuthRepository
import com.example.locationapp.repository.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay // Import delay

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _potentialPoints = MutableStateFlow(0)
    val potentialPoints: StateFlow<Int> = _potentialPoints.asStateFlow()

    private val _isClaimable = MutableStateFlow(false)
    val isClaimable: StateFlow<Boolean> = _isClaimable.asStateFlow()

    private val _isClaiming = MutableStateFlow(false)
    val isClaiming: StateFlow<Boolean> = _isClaiming.asStateFlow()

    // --- NEW STATE FOR SHOWING CONFIRMATION ---
    private val _claimResult = MutableStateFlow<String?>(null)
    val claimResult: StateFlow<String?> = _claimResult.asStateFlow()

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            // Reset claim message on data refresh
            _claimResult.value = null
            authRepository.getCurrentUser().onSuccess { user ->
                _user.value = user

                val isAway = user.currentLocation.isNotBlank()
                val hasVisited = user.visitedCities.contains(user.currentLocation)

                _isClaimable.value = isAway && !hasVisited

                if (_isClaimable.value) {
                    _potentialPoints.value = 350 // Mock value
                } else {
                    _potentialPoints.value = 0
                }
            }
        }
    }

    fun claimPoints() {
        viewModelScope.launch {
            if (!_isClaimable.value) return@launch // Safety check
            _isClaiming.value = true
            val result = authRepository.claimCurrentCity()
            result.onSuccess {
                // --- UPDATE UI WITH SUCCESS MESSAGE ---
                _claimResult.value = "Points Claimed!"
                // Refresh all user data to reflect the new points and visited status
                fetchData()
                // Optional: Hide the message after a few seconds
                delay(3000)
                _claimResult.value = null
            }
            result.onFailure {
                // --- UPDATE UI WITH ERROR MESSAGE ---
                _claimResult.value = "Error: ${it.message}"
                println("Failed to claim points: ${it.message}")
                // Optional: Hide the message after a few seconds
                delay(3000)
                _claimResult.value = null
            }
            _isClaiming.value = false
        }
    }
}

class HomeViewModelFactory(
    private val authRepository: AuthRepository,
    private val locationRepository: LocationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(authRepository, locationRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
