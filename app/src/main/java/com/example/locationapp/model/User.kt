package com.example.locationapp.model

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId val userId: String = "",
    val userName: String = "",
    val email: String = "",
    val points: Float = 0f,
    val profileImage: String = "",
    val citiesVisited: Int = 0,
    val countriesVisited: Int = 0,
    val homeLatitude: Double = 0.0,
    val homeLongitude: Double = 0.0,
    val visitedCities: List<String> = emptyList(),
    val visitedCountries: List<String> = emptyList(),
    val hasSetHome: Boolean = false
)
