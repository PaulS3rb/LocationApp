package com.example.locationapp.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date


data class Location(
    val city: String = "",
    val country: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val image: String = "",

    val totalPointsAwarded: Long = 0,
    val totalVisits: Long = 0,

    @ServerTimestamp
    val lastVisited: Date? = null
)