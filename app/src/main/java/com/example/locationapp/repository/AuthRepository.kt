package com.example.locationapp.repository

import android.content.Context
import com.example.locationapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class AuthRepository(context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val users = FirebaseFirestore.getInstance().collection("users")
    private val locationService = LocationService(context) // Instantiate LocationService

    suspend fun signup(userName: String, email: String, password: String): Result<User> {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()

            val uid = auth.currentUser!!.uid

            val user = User(userName = userName, email = email, points = 0f)
            users.document(uid).set(user).await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()

            val uid = auth.currentUser!!.uid

            val snapshot = users.document(uid).get().await()
            val user = snapshot.toObject(User::class.java)!!

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): Result<User> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            // 1. Get user data from Firestore
            val userSnapshot = users.document(uid).get().await()
            var user = userSnapshot.toObject(User::class.java)!!

            // 2. Get fresh GPS location
            val deviceLocation = locationService.getFreshCurrentLocation() ?: return Result.success(user) // Return old user data if location fails

            // 3. Reverse Geocode: Get city name from coordinates
            val currentCity = locationService.getCityFromCoordinates(deviceLocation.latitude, deviceLocation.longitude)

            // If we are not in a city (e.g., on a highway), clear the current location info
            if (currentCity == null) {
                val updates = mapOf(
                    "currentLocation" to "",
                    "currentLocationImage" to "",
                    "currentLatitude" to deviceLocation.latitude,
                    "currentLongitude" to deviceLocation.longitude
                )
                users.document(uid).set(updates, SetOptions.merge()).await()
                return Result.success(user.copy(currentLocation = "", currentLocationImage = "", currentLatitude = deviceLocation.latitude, currentLongitude = deviceLocation.longitude))
            }

            // 4. If the city hasn't changed, just update coordinates and return
            if (currentCity == user.currentLocation) {
                val updates = mapOf(
                    "currentLatitude" to deviceLocation.latitude,
                    "currentLongitude" to deviceLocation.longitude
                )
                users.document(uid).set(updates, SetOptions.merge()).await()
                return Result.success(user.copy(currentLatitude = deviceLocation.latitude, currentLongitude = deviceLocation.longitude))
            }

            // 5. THIS IS A NEW CITY! Update everything in Firestore.
            val cityImage = getCityImage(currentCity) // Placeholder for an image service call
            val updates = mapOf(
                "currentLocation" to currentCity,
                "currentLocationImage" to cityImage,
                "currentLatitude" to deviceLocation.latitude,
                "currentLongitude" to deviceLocation.longitude
            )
            users.document(uid).set(updates, SetOptions.merge()).await()

            // 6. Return the fully updated user object to the UI
            val updatedUser = user.copy(
                currentLocation = currentCity,
                currentLocationImage = cityImage,
                currentLatitude = deviceLocation.latitude,
                currentLongitude = deviceLocation.longitude
            )
            Result.success(updatedUser)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getCityImage(city: String): String {
        // In a real app, you would make a network request here.
        // For now, let's return a consistent placeholder based on the city.
        return when (city.lowercase()) {
            "tokyo" -> "https://images.unsplash.com/photo-1542051841857-5f90071e7989"
            "cluj-napoca" -> "https://images.unsplash.com/photo-1570168007204-dfb528c6958f"
            else -> "https://images.unsplash.com/photo-1554878516-1691fd114521" // Default fallback
        }
    }

    suspend fun claimCurrentCity(): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            val userRef = users.document(uid)

            // 1. Get the latest user data to ensure we have the correct current location
            val snapshot = userRef.get().await()
            val user = snapshot.toObject(User::class.java)!!

            // 2. Safety Check: Ensure the location is valid and not already visited
            if (user.currentLocation.isBlank() || user.visitedCities.contains(user.currentLocation)) {
                return Result.failure(Exception("City is not claimable."))
            }

            // 3. Calculate Points (TODO: Replace mock points with real distance calculation)
            // This is where you would use user.homeLatitude/Longitude and user.currentLatitude/Longitude
            val pointsToAward = 350L // Using Long for Firestore numbers

            // 4. Perform a Firestore transaction to safely update everything
            userRef.update(
                "points", FieldValue.increment(pointsToAward),
                "visitedCities", FieldValue.arrayUnion(user.currentLocation),
                "citiesVisited", FieldValue.increment(1)
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
