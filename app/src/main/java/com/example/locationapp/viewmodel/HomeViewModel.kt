package com.example.locationapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.locationapp.model.User
import com.example.locationapp.repository.AuthRepository
import com.example.locationapp.repository.LocationRepository
import com.example.locationapp.repository.LocationVisit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _recentVisits = MutableStateFlow<List<LocationVisit>>(emptyList())
    val recentVisits: StateFlow<List<LocationVisit>> = _recentVisits.asStateFlow()

    private val _potentialPoints = MutableStateFlow(0)
    val potentialPoints: StateFlow<Int> = _potentialPoints.asStateFlow()

    private val _isAwayFromHome = MutableStateFlow(false)
    val isAwayFromHome: StateFlow<Boolean> = _isAwayFromHome.asStateFlow()

    private val _isClaimable = MutableStateFlow(false)
    val isClaimable: StateFlow<Boolean> = _isClaimable.asStateFlow()

    private val _isClaiming = MutableStateFlow(false)
    val isClaiming: StateFlow<Boolean> = _isClaiming.asStateFlow()


    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            authRepository.getCurrentUser().onSuccess { user ->
                _user.value = user

                // --- UPDATED CORE LOGIC ---
                val isAway = user.currentLocation.isNotBlank()
                val hasVisited = user.visitedCities.contains(user.currentLocation)

                // The city is claimable if they are away AND they haven't visited it before.
                _isClaimable.value = isAway && !hasVisited

                if (_isClaimable.value) {
                    // TODO: Calculate real points based on distance from home coordinates
                    _potentialPoints.value = 350 // Mock value
                } else {
                    _potentialPoints.value = 0
                }
            }
            //...
        }
    }
    fun claimPoints() {
        viewModelScope.launch {
            _isClaiming.value = true
            val result = authRepository.claimCurrentCity()
            result.onSuccess {
                // Refresh the user data to reflect the new points and visited status
                fetchData()
            }
            result.onFailure {
                // Optionally handle the error, e.g., show a toast
                println("Failed to claim points: ${it.message}")
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
