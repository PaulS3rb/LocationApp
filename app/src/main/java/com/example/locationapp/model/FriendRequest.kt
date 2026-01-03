package com.example.locationapp.model


data class FriendRequest(
    val fromId: String = "",
    val fromName: String = "",
    val fromImage: String = "",
    val fromPoints: Float = 0f,
    val status: String = ""
)