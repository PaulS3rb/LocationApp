package com.example.locationapp.repository

import android.content.Context
import android.net.Uri
import com.example.locationapp.model.Location
import com.example.locationapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

class AuthRepository(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val users = db.collection("users")
    private val locations = db.collection("locations")
    private val locationService = LocationService(context)

    suspend fun signup(userName: String, email: String, password: String): Result<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Failed to get user after creation.")

            val profileUpdates = userProfileChangeRequest {
                displayName = userName
            }
            firebaseUser.updateProfile(profileUpdates).await()

            val user = User(
                userName = userName,
                email = email,
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

    suspend fun getCurrentUser(): Result<User> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            val userSnapshot = users.document(uid).get().await()
            val user = userSnapshot.toObject(User::class.java)
                ?: return Result.failure(Exception("User data not found in Firestore."))

            Result.success(user)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadProfilePicture(uri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")
                
                // Create a local file in the app's internal storage
                val fileName = "profile_$uid.jpg"
                val file = File(context.filesDir, fileName)

                // Copy the content from the selected Uri to our local file
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                } ?: throw Exception("Failed to open image")

                val localPath = file.absolutePath
                
                // Update Firestore with the local path so the app knows where to find it
                users.document(uid).update("profileImage", localPath).await()

                Result.success(localPath)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun getCityImage(city: String): String {
        return when (city.lowercase()) {
            "tokyo" -> "https://images.unsplash.com/photo-1542051841857-5f90071e7989"
            "cluj-napoca" -> "https://images.unsplash.com/photo-1570168007204-dfb528c6958f"
            else -> "https://images.unsplash.com/photo-1554878516-1691fd114521"
        }
    }

    suspend fun claimCurrentCity(
        currentCity: String,
        currentLatitude: Double,
        currentLongitude: Double
    ): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            val userRef = users.document(uid)

            db.runTransaction { transaction ->
                val userSnapshot = transaction.get(userRef)
                val user = userSnapshot.toObject(User::class.java)!!

                val cityId = currentCity.replace(" ", "_").lowercase()
                val locationRef = locations.document(cityId)
                val locationSnapshot = transaction.get(locationRef)

                if (currentCity.isBlank() || user.visitedCities.contains(currentCity)) {
                    throw Exception("City is not claimable.")
                }

                val distance = calculateDistance(
                    user.homeLatitude,
                    user.homeLongitude,
                    currentLatitude,
                    currentLongitude
                )

                val distancePoints = max(25.0, distance * 0.5).toLong()
                val discoveryBonus = if (!locationSnapshot.exists() || locationSnapshot.getLong("totalVisits") == 0L) 200L else 0L
                val pointsToAward = distancePoints + discoveryBonus

                transaction.update(userRef, mapOf(
                    "points" to FieldValue.increment(pointsToAward),
                    "visitedCities" to FieldValue.arrayUnion(currentCity),
                    "citiesVisited" to FieldValue.increment(1)
                ))

                val cityImage = getCityImage(currentCity)
                if (locationSnapshot.exists()) {
                    transaction.update(locationRef, mapOf(
                        "totalVisits" to FieldValue.increment(1),
                        "totalPointsAwarded" to FieldValue.increment(pointsToAward),
                        "lastVisited" to FieldValue.serverTimestamp()
                    ))
                } else {
                    val newLocation = Location(
                        city = currentCity,
                        latitude = currentLatitude,
                        longitude = currentLongitude,
                        image = cityImage,
                        totalVisits = 1,
                        totalPointsAwarded = pointsToAward
                    )
                    transaction.set(locationRef, newLocation)
                }

                null
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(lonDistance / 2) * sin(lonDistance / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    suspend fun setHomeLocation(latitude: Double, longitude: Double): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            val userRef = users.document(uid)

            if (latitude == 0.0 || longitude == 0.0) {
                return Result.failure(Exception("Invalid home location coordinates."))
            }

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
