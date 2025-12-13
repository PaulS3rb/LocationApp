package com.example.locationapp.model

data class Location(
    val city: String = "",
    val country: String = "",
    val points: Int = 0,
    val distance: Int = 0,
    val image: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)