package com.example.wearzone.data.local.datasource

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.example.wearzone.domain.customer.address.model.AddressCoordinates
import com.example.wearzone.domain.customer.address.model.CurrentLocationPermissionDeniedException
import com.example.wearzone.domain.customer.address.model.CurrentLocationUnavailableException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout

class CurrentLocationDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : ICurrentLocationDataSource {

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    override suspend fun getCurrentCoordinates(): AddressCoordinates {
        if (!context.hasLocationPermission()) {
            throw CurrentLocationPermissionDeniedException()
        }

        if (!context.isLocationEnabled()) {
            throw CurrentLocationUnavailableException()
        }

        val cancellationTokenSource = CancellationTokenSource()

        return try {
            val location = withTimeout(LOCATION_TIMEOUT_MILLIS) {
                suspendCancellableCoroutine { continuation ->
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cancellationTokenSource.token,
                    ).addOnSuccessListener { location ->
                        if (continuation.isActive) continuation.resume(location)
                    }.addOnFailureListener {
                        if (continuation.isActive) continuation.resume(null)
                    }

                    continuation.invokeOnCancellation {
                        cancellationTokenSource.cancel()
                    }
                }
            }

            location?.let {
                AddressCoordinates(
                    latitude = it.latitude,
                    longitude = it.longitude,
                )
            } ?: throw CurrentLocationUnavailableException()
        } catch (_: TimeoutCancellationException) {
            cancellationTokenSource.cancel()
            throw CurrentLocationUnavailableException()
        } catch (throwable: Throwable) {
            if (throwable is CurrentLocationPermissionDeniedException) throw throwable
            throw CurrentLocationUnavailableException()
        }
    }

    private fun Context.hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        return fine || coarse
    }

    private fun Context.isLocationEnabled(): Boolean {
        return try {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (_: Throwable) {
            false
        }
    }

    private companion object {
        const val LOCATION_TIMEOUT_MILLIS = 10_000L
    }
}
