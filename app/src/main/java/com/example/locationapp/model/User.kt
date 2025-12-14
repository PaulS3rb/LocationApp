package com.example.locationapp.model


data class User(
    val userName: String = "",
    val email: String = "",
    val points: Float = 0f,
    val profileImage: String = "",
    val citiesVisited: Int = 0,
    val countriesVisited: Int = 0,
    val homeLatitude: Double = 0.0,
    val homeLongitude: Double = 0.0,
    val visitedCities: List<String> = emptyList(),
    val hasSetHome: Boolean = false

)
