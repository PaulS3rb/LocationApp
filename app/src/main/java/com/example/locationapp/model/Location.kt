package com.example.locationapp.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Represents a unique, visitable location in the app.
 * This model is used for the global 'locations' collection in Firestore.
 */
data class Location(
    // Identity Properties
    val city: String = "",
    val country: String = "", // Keep for future use
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val image: String = "", // The main image for this city

    // Aggregated Statistics
    val totalPointsAwarded: Long = 0, // Use Long for Firestore numbers
    val totalVisits: Long = 0,

    @ServerTimestamp
    val lastVisited: Date? = null
)