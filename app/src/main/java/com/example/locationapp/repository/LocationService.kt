package com.example.locationapp.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import android.location.Location as AndroidLocation // Rename to avoid conflicts
import java.util.Locale
import kotlin.coroutines.resume


class LocationService(private val context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val geocoder = Geocoder(context, Locale.getDefault()) // This will now resolve correctly

    @SuppressLint("MissingPermission") // We will handle permissions before calling this
    suspend fun getFreshCurrentLocation(): AndroidLocation? {
        val cancellationTokenSource = CancellationTokenSource()
        return fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).await()
    }

    suspend fun getCityFromCoordinates(latitude: Double, longitude: Double): String? {
        // Geocoding can be slow, so it should be on a background thread
        return withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // --- REVISED LOGIC FOR NEWER ANDROID ---
                    // Use a coroutine to wait for the asynchronous callback
                    suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                            val city = addresses.firstOrNull()?.locality
                            if (continuation.isActive) {
                                continuation.resume(city)
                            }
                        }
                    }
                } else {
                    // For older Android versions (deprecated but necessary)
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    addresses?.firstOrNull()?.locality // This returns correctly from the withContext block
                }
            } catch (e: Exception) {
                // Handle exceptions like no network connection
                e.printStackTrace()
                null // Return null on error
            }
        }
    }

    suspend fun getCoordinatesFromCityName(cityName: String, maxResults: Int = 5): List<Address> {
        return withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocationName(cityName, maxResults) { addresses ->
                            if (continuation.isActive) {
                                continuation.resume(addresses)
                            }
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocationName(cityName, maxResults) ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    fun getFormattedAddress(address: Address): String {
        // We use a set to avoid duplicates (e.g., if city and admin area are the same)
        val parts = linkedSetOf<String>()

        address.locality?.let { parts.add(it) } // City
        address.adminArea?.let { parts.add(it) } // State or Region
        address.countryName?.let { parts.add(it) } // Country

        return parts.joinToString(", ")
    }

}
