package jbs.com.stunninghelloworld

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
    
    private fun uiSetupForCurrentLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        uiSetup1(location.latitude, location.longitude)
                    } else {
                        checkLocationSetting()
                    }
                    // Got last known location. In some rare situations this can be null.
                }
    }

    /**
     * Checks Device's location settings and Request's location update if location is enabled.
     * @see LocationRequest
     * @see LocationCallback
     * @see LocationServices
     * */
    @SuppressLint("MissingPermission")
    private fun checkLocationSetting() {
        // Check for LocationSetting
        // Do we need to request for location
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = (5 * 1000).toLong()
        locationRequest.fastestInterval = (2 * 1000).toLong()
        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

        builder.setAlwaysShow(true)

        val result =
                LocationServices.getSettingsClient(activity).checkLocationSettings(builder.build())
        result.addOnCompleteListener { task ->
            try {
                val response = task.getResult(ApiException::class.java)
                if (response.locationSettingsStates.isLocationPresent) {
                    locationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult?) {
                            locationResult ?: return
                            Log.e("UPDATE", "location updates")
                            fusedLocationClient.removeLocationUpdates(locationCallback)
                            uiSetup1(locationResult.locations[0].latitude, locationResult.locations[0].longitude)
                            /*for (location in locationResult.locations) {
                                currentLocation=location
                            }*/
                        }
                    }
                    val locationRequest1 = LocationRequest().apply {
                        interval = 10000
                        fastestInterval = 5000
                        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                    }

                    fusedLocationClient.requestLocationUpdates(locationRequest1,
                            locationCallback,
                            null)
                    /*if(v.isSuccessful){
                        Log.e("UPDATE", "location updates")
                    }else{
                        Log.e("UPDATE", "location updates")
                    }*/
                }
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                        (activity as MainActivity).showNoLocationDetailDialog()
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE ->
                        showCommonAlertDialog(exception.localizedMessage, activity, false)
                }
            }
        }
    }
}
