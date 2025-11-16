package com.example.locationapp.database
import com.example.locationapp.model.User
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.locationapp.dao.UserDao


@Database(entities = [User:: class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao
}