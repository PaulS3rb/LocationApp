package com.example.locationapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.locationapp.repository.AuthRepository
import com.example.locationapp.repository.LocationService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class CitySearchResult(
    val formattedAddress: String,
    val latitude: Double,
    val longitude: Double
)

class SetHomeViewModel(
    private val authRepository: AuthRepository,
    private val locationService: LocationService
) : ViewModel() {

private val _detectedLocation = MutableStateFlow(CitySearchResult("", 0.0, 0.0))
    val detectedLocation = _detectedLocation.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _searchResults = MutableStateFlow<List<CitySearchResult>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private var searchJob: Job? = null

    init {
       viewModelScope.launch {
            val deviceLocation = locationService.getFreshCurrentLocation()
            if (deviceLocation != null) {
                val city = locationService.getCityFromCoordinates(deviceLocation.latitude, deviceLocation.longitude)
                if (city != null) {
                    _detectedLocation.value = CitySearchResult(
                        formattedAddress = city,
                        latitude = deviceLocation.latitude,
                        longitude = deviceLocation.longitude
                    )
                }
            }
        }
    }

    fun onSearchTextChanged(text: String) {
        _searchText.value = text
        searchJob?.cancel()
        if (text.length < 3) {
            _searchResults.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)
            _isSearching.value = true
            val results = locationService.getCoordinatesFromCityName(text)
            _searchResults.value = results
                .mapNotNull { address ->
                    if (address.locality != null) {
                        CitySearchResult(
                            formattedAddress = locationService.getFormattedAddress(address),
                            latitude = address.latitude,
                            longitude = address.longitude
                        )
                    } else {
                        null
                    }
                }
                .distinctBy { it.formattedAddress }

            _isSearching.value = false
        }
    }

    fun setHomeLocation(city: CitySearchResult, onHomeSet: () -> Unit) {
        viewModelScope.launch {
            authRepository.setHomeLocation(city.latitude, city.longitude).onSuccess {
                onHomeSet()
            }
        }
    }
}

class SetHomeViewModelFactory(
    private val authRepository: AuthRepository,
    private val locationService: LocationService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SetHomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SetHomeViewModel(authRepository, locationService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
