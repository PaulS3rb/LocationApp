package com.example.locationapp.repository

import com.example.locationapp.model.Location
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class LocationRepository {

    private val locationsCollection = FirebaseFirestore.getInstance().collection("locations")

    /**
     * Fetches the top locations from the global 'locations' collection,
     * sorted by the total points awarded in descending order.
     *
     * @param limit The maximum number of top locations to retrieve.
     * @return A Result containing the list of top Location objects or an exception.
     */
    suspend fun getTopLocations(limit: Int = 10): Result<List<Location>> {
        return try {
            val snapshot = locationsCollection
                .orderBy("totalPointsAwarded", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val topLocations = snapshot.toObjects(Location::class.java)
            Result.success(topLocations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
