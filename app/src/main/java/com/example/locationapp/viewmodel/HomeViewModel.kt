package com.example.locationapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.locationapp.model.User
import com.example.locationapp.repository.AuthRepository
import com.example.locationapp.repository.LocationRepository
import com.example.locationapp.repository.LocationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay // Import delay


data class CurrentLocationState(
    val cityName: String = "",
    val cityImage: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
class HomeViewModel(
    private val authRepository: AuthRepository,
    private val locationRepository: LocationRepository,
    private val locationService: LocationService // <-- ADD LocationService
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    // --- NEW: State for the locally managed current location ---
    private val _currentLocation = MutableStateFlow(CurrentLocationState())
    val currentLocation: StateFlow<CurrentLocationState> = _currentLocation.asStateFlow()

    private val _potentialPoints = MutableStateFlow(0)
    val potentialPoints: StateFlow<Int> = _potentialPoints.asStateFlow()

    private val _isClaimable = MutableStateFlow(false)
    val isClaimable: StateFlow<Boolean> = _isClaimable.asStateFlow()

    private val _isClaiming = MutableStateFlow(false)
    val isClaiming: StateFlow<Boolean> = _isClaiming.asStateFlow()

    private val _claimResult = MutableStateFlow<String?>(null)
    val claimResult: StateFlow<String?> = _claimResult.asStateFlow()

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            _claimResult.value = null
            // First, get the static user profile from the database
            authRepository.getCurrentUser().onSuccess { dbUser ->
                _user.value = dbUser

                // Second, get the live device location
                val deviceLocation = locationService.getFreshCurrentLocation()
                val city = deviceLocation?.let {
                    locationService.getCityFromCoordinates(it.latitude, it.longitude)
                }

                // Update the local-only current location state
                _currentLocation.value = CurrentLocationState(
                    cityName = city ?: "",
                    // In a real app, you would fetch this image URL
                    cityImage = if (city != null) "https://images.unsplash.com/photo-1554878516-1691fd114521" else "",
                    latitude = deviceLocation?.latitude ?: 0.0,
                    longitude = deviceLocation?.longitude ?: 0.0
                )

                // Third, determine claimability based on both database and local state
                val isAwayFromHome = _currentLocation.value.cityName.isNotBlank() && (
                        // A simple distance check could be added here later if needed
                        _currentLocation.value.cityName.lowercase() != "home" // Placeholder logic
                        )
                val hasVisited = dbUser.visitedCities.contains(_currentLocation.value.cityName)

                _isClaimable.value = isAwayFromHome && !hasVisited

                if (_isClaimable.value) {
                    _potentialPoints.value = 350
                } else {
                    _potentialPoints.value = 0
                }
            }
        }
    }

    fun claimPoints() {
        viewModelScope.launch {
            if (!_isClaimable.value) return@launch
            _isClaiming.value = true

            // Pass the locally-stored current location info to the repository
            val locationToClaim = _currentLocation.value
            val result = authRepository.claimCurrentCity(
                currentCity = locationToClaim.cityName,
                currentLatitude = locationToClaim.latitude,
                currentLongitude = locationToClaim.longitude
            )

            result.onSuccess {
                _claimResult.value = "Points Claimed!"
                fetchData() // Refresh everything
                delay(3000)
                _claimResult.value = null
            }
            result.onFailure {
                _claimResult.value = "Error: ${it.message}"
                delay(3000)
                _claimResult.value = null
            }
            _isClaiming.value = false
        }
    }
}


class HomeViewModelFactory(
    private val authRepository: AuthRepository,
    private val locationRepository: LocationRepository,
    private val locationService: LocationService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(authRepository, locationRepository, locationService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}