package com.example.locationapp.repository

import android.content.Context
import com.example.locationapp.model.Location
import com.example.locationapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class AuthRepository(context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val users = db.collection("users")
    private val locations = db.collection("locations") // Reference to the new collection
    private val locationService = LocationService(context)

    // --- ADDING THESE FUNCTIONS BACK ---

    suspend fun signup(userName: String, email: String, password: String): Result<Unit> {
        return try {
            // 1. Create user in Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Failed to get user after creation.")

            // 2. Update Firebase Auth profile with username
            val profileUpdates = userProfileChangeRequest {
                displayName = userName
            }
            firebaseUser.updateProfile(profileUpdates).await()

            // 3. Create user document in Firestore
            val user = User(
                userName = userName,
                email = email,
                // Other fields will have default values
            )
            users.document(firebaseUser.uid).set(user).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }


    // --- EXISTING FUNCTIONS ---

    suspend fun getCurrentUser(): Result<User> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            // 1. Get user data from Firestore
            val userSnapshot = users.document(uid).get().await()
            val user = userSnapshot.toObject(User::class.java)!!

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
            if (currentCity == user.currentLocation && user.currentLatitude == deviceLocation.latitude && user.currentLongitude == deviceLocation.longitude) {
                return Result.success(user) // No change, no write needed
            }

            // 5. City or Coordinates have changed, update Firestore
            val cityImage = getCityImage(currentCity)
            val updates = mapOf(
                "currentLocation" to currentCity,
                "currentLocationImage" to cityImage,
                "currentLatitude" to deviceLocation.latitude,
                "currentLongitude" to deviceLocation.longitude
            )
            users.document(uid).set(updates, SetOptions.merge()).await()

            // 6. Return the fully updated user object
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

            // Run as a transaction to ensure all writes succeed or fail together
            db.runTransaction { transaction ->
                // --- ALL READS MUST BE FIRST ---
                // 1. Read the user's document
                val userSnapshot = transaction.get(userRef)
                val user = userSnapshot.toObject(User::class.java)!!

                // 2. Read the location document (even if it might not exist yet)
                val cityId = user.currentLocation.replace(" ", "_").lowercase()
                val locationRef = locations.document(cityId)
                val locationSnapshot = transaction.get(locationRef)

                // --- PERFORM LOGIC AND CHECKS ---
                // 3. Safety Check: Ensure the location is valid and not already visited
                if (user.currentLocation.isBlank() || user.visitedCities.contains(user.currentLocation)) {
                    throw Exception("City is not claimable.")
                }

                // 4. Calculate Points
                val pointsToAward = 350L // Mock points

                // --- ALL WRITES MUST BE LAST ---
                // 5. Write to the User Document
                transaction.update(userRef, mapOf(
                    "points" to FieldValue.increment(pointsToAward),
                    "visitedCities" to FieldValue.arrayUnion(user.currentLocation),
                    "citiesVisited" to FieldValue.increment(1)
                ))

                // 6. Write to the Global Location Document
                if (locationSnapshot.exists()) {
                    // If the location exists, increment its counters
                    transaction.update(locationRef, mapOf(
                        "totalVisits" to FieldValue.increment(1),
                        "totalPointsAwarded" to FieldValue.increment(pointsToAward),
                        "lastVisited" to FieldValue.serverTimestamp()
                    ))
                } else {
                    // If it's a new location, create its document using the Location model
                    val newLocation = Location(
                        city = user.currentLocation,
                        latitude = user.currentLatitude,
                        longitude = user.currentLongitude,
                        image = user.currentLocationImage,
                        totalVisits = 1,
                        totalPointsAwarded = pointsToAward
                        // lastVisited is set by @ServerTimestamp or FieldValue
                    )
                    transaction.set(locationRef, newLocation)
                }

                null // Return value for a successful transaction
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setHomeLocation(latitude: Double, longitude: Double): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            val userRef = users.document(uid)

            if (latitude == 0.0 || longitude == 0.0) {
                return Result.failure(Exception("Invalid home location coordinates."))
            }

            // Prepare the updates
            val homeUpdates = mapOf(
                "homeLatitude" to latitude,
                "homeLongitude" to longitude,
                "hasSetHome" to true
            )

            userRef.update(homeUpdates).await()
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}
