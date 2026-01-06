package com.example.locationapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.locationapp.model.User
import com.example.locationapp.repository.AuthRepository
import com.example.locationapp.repository.LocationRepository
import com.example.locationapp.repository.LocationService
import com.example.locationapp.repository.FriendRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class CurrentLocationState(
    val cityName: String = "",
    val cityImage: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val locationRepository: LocationRepository,
    private val locationService: LocationService,
    private val friendRepository: FriendRepository // ✅ FIX
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

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

    // ✅ FIX: friends count state
    private val _friendsCount = MutableStateFlow(0)
    val friendsCount: StateFlow<Int> = _friendsCount.asStateFlow()

    init {
        fetchData()
        loadFriendsCount()
    }

    fun fetchData() {
        viewModelScope.launch {
            _claimResult.value = null

            authRepository.getCurrentUser().onSuccess { dbUser ->
                _user.value = dbUser

                val deviceLocation = locationService.getFreshCurrentLocation()
                val city = deviceLocation?.let {
                    locationService.getCityFromCoordinates(it.latitude, it.longitude)
                }

                _currentLocation.value = CurrentLocationState(
                    cityName = city ?: "",
                    cityImage = if (city != null)
                        "https://images.unsplash.com/photo-1554878516-1691fd114521"
                    else "",
                    latitude = deviceLocation?.latitude ?: 0.0,
                    longitude = deviceLocation?.longitude ?: 0.0
                )

                val isAwayFromHome =
                    _currentLocation.value.cityName.isNotBlank() &&
                            _currentLocation.value.cityName.lowercase() != "home"

                val hasVisited =
                    dbUser.visitedCities.contains(_currentLocation.value.cityName)

                _isClaimable.value = isAwayFromHome && !hasVisited

                if (_isClaimable.value) {
                    val distance = calculateDistance(
                        dbUser.homeLatitude,
                        dbUser.homeLongitude,
                        _currentLocation.value.latitude,
                        _currentLocation.value.longitude
                    )

                    val distancePoints =
                        kotlin.math.max(25.0, distance * 0.5).toInt()

                    val discoveryBonus =
                        if (!hasVisited) 200 else 0

                    _potentialPoints.value = distancePoints + discoveryBonus
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

            val locationToClaim = _currentLocation.value
            val result = authRepository.claimCurrentCity(
                currentCity = locationToClaim.cityName,
                currentLatitude = locationToClaim.latitude,
                currentLongitude = locationToClaim.longitude
            )

            result.onSuccess {
                _claimResult.value = "Points Claimed!"
                fetchData()
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

    private fun loadFriendsCount() {
        viewModelScope.launch {
            val result = friendRepository.getFriends()
            result.onSuccess { friends ->
                _friendsCount.value = friends.size
            }.onFailure {
                _friendsCount.value = 0
            }
        }
    }

    private fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val r = 6371
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a =
            kotlin.math.sin(latDistance / 2) * kotlin.math.sin(latDistance / 2) +
                    kotlin.math.cos(Math.toRadians(lat1)) *
                    kotlin.math.cos(Math.toRadians(lat2)) *
                    kotlin.math.sin(lonDistance / 2) *
                    kotlin.math.sin(lonDistance / 2)
        val c = 2 * kotlin.math.atan2(
            kotlin.math.sqrt(a),
            kotlin.math.sqrt(1 - a)
        )
        return r * c
    }
}

class HomeViewModelFactory(
    private val authRepository: AuthRepository,
    private val locationRepository: LocationRepository,
    private val locationService: LocationService,
    private val friendRepository: FriendRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(
                authRepository,
                locationRepository,
                locationService,
                friendRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
