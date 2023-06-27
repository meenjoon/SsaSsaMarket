package com.mbj.ssassamarket.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

class LocationManager(
    private val context: Context,
    private var timeInterval: Long,
    private var minimalDistance: Float,
    private val locationUpdateListener: LocationUpdateListener? = null
) : LocationCallback() {

    private var request: LocationRequest
    private var locationClient: FusedLocationProviderClient

    init {
        locationClient = LocationServices.getFusedLocationProviderClient(context)
        request = createRequest()
    }

    fun checkLocationPermission(locationPermissionLauncher: ActivityResultLauncher<Array<String>>) {
        val fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        val coarseLocationPermission = Manifest.permission.ACCESS_COARSE_LOCATION
        val permissions = arrayOf(fineLocationPermission, coarseLocationPermission)
        val grantedPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (grantedPermissions.size < permissions.size) {
            locationPermissionLauncher.launch(permissions)
        } else {
            startLocationTracking()
        }
    }

    fun isAnyLocationPermissionGranted(context: Context): Boolean {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocationGranted || coarseLocationGranted
    }

    private fun createRequest(): LocationRequest =
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, timeInterval).apply {
            setMinUpdateDistanceMeters(minimalDistance)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(true)
        }.build()

    fun changeRequest(timeInterval: Long, minimalDistance: Float) {
        this.timeInterval = timeInterval
        this.minimalDistance = minimalDistance
        createRequest()
        stopLocationTracking()
        startLocationTracking()
    }

    fun startLocationTracking() {
        if (!(ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
                    )
        ) {
            locationClient.requestLocationUpdates(request, this, Looper.getMainLooper())
        }
    }

    fun stopLocationTracking() {
        locationClient.flushLocations()
        locationClient.removeLocationUpdates(this)
    }

    override fun onLocationResult(locationResult: LocationResult) {
        val location = locationResult.lastLocation
        val latitude = location?.latitude
        val longitude = location?.longitude
        locationUpdateListener?.onLocationUpdated(latitude, longitude)
    }

    interface LocationUpdateListener {
        fun onLocationUpdated(latitude: Double?, longitude: Double?)
    }
}
