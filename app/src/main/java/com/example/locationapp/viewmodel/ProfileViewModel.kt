package com.example.locationapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.locationapp.model.Location
import com.example.locationapp.model.User
import com.example.locationapp.repository.AuthRepository
import com.example.locationapp.repository.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _topLocations = MutableStateFlow<List<Location>>(emptyList())
    val topLocations: StateFlow<List<Location>> = _topLocations.asStateFlow()

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            authRepository.getCurrentUser().onSuccess {
                _user.value = it
            }
            locationRepository.getTopLocations(limit = 3).onSuccess { locations ->
                _topLocations.value = locations
            }
        }
    }

}

class ProfileViewModelFactory(
    private val authRepository: AuthRepository,
    private val locationRepository: LocationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(authRepository, locationRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
