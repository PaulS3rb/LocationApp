package com.example.locationapp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "username") val userName: String?,
    @ColumnInfo(name = "email") val email: String?,
    @ColumnInfo(name = "points") val points: Float,
    @ColumnInfo(name = "password") val password: String?
)

