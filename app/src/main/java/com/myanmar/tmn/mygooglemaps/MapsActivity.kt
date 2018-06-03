package com.myanmar.tmn.mygooglemaps

import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.myanmar.tmn.mygooglemaps.common.Common
import com.myanmar.tmn.mygooglemaps.model.MyPlaces
import com.myanmar.tmn.mygooglemaps.remote.IGoogleService
import kotlinx.android.synthetic.main.activity_maps.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {


    override fun onMarkerClick(p0: Marker?) = false

    private lateinit var mMap: GoogleMap

    //location
    private lateinit var mFusedClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var latitude: Double = 0.toDouble()
    private var longitude: Double = 0.toDouble()

    private lateinit var lastLocation: Location
    private var marker: Marker? = null


    companion object {
        private const val LOCATION_PERMISSION_CODE = 1
        private const val PLACE_PICKER_REQUEST = 3
    }

    lateinit var iGoogleService: IGoogleService
    internal lateinit var currentPlace: MyPlaces

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //init service
        iGoogleService = Common.iGoogleService

        //runtime exception
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (checkLocationPermission()) {
                buildLocationRequest()
                buildLocationCallback()

                mFusedClient = LocationServices.getFusedLocationProviderClient(this)
                mFusedClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
            }
        } else {
            buildLocationRequest()
            buildLocationCallback()

            mFusedClient = LocationServices.getFusedLocationProviderClient(this)
            mFusedClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        }

        navi_view.setOnNavigationItemSelectedListener {  item ->
            when (item.itemId) {
                R.id.hospital -> nearPlaces("Hospital")
                R.id.restaurant -> nearPlaces("Restaurant")
                R.id.school -> nearPlaces("School")
                R.id.shopping -> nearPlaces("Shopping")
            }
            true
        }
    }

    private fun buildLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                lastLocation = p0!!.locations.get(p0!!.locations.size - 1) //get last location
                if (marker != null) {
                    marker!!.remove()
                }
                latitude = lastLocation.latitude
                longitude = lastLocation.longitude

                var lagCurrent = LatLng(latitude, longitude)
                val markerOptions = MarkerOptions().position(lagCurrent).title("Your Position")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                marker = mMap!!.addMarker(markerOptions)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lagCurrent, 12.0f))
            }
        }
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement = 10f
    }

    private fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION))
                ActivityCompat.requestPermissions(this,
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_CODE)
            else
                ActivityCompat.requestPermissions(this,
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_CODE)
            return false
        }
        return true
    }

    override fun onStop() {
        mFusedClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (checkLocationPermission()) {
                            buildLocationRequest()
                            buildLocationCallback()

                            mFusedClient = LocationServices.getFusedLocationProviderClient(this)
                            mFusedClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
                            mMap.isMyLocationEnabled = true
                        }
                    } else {
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //last location
        mMap.uiSettings.isZoomControlsEnabled
        mMap.uiSettings.isRotateGesturesEnabled
        mMap.setOnMarkerClickListener(this)

        // Add a marker in Sydney and move the camera with red one
        val yangon = LatLng(16.871311, 96.199379)
        mMap.addMarker(MarkerOptions().position(yangon).title("My Fav City"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(yangon, 12.0f))

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap!!.isMyLocationEnabled = true
            }
        } else {
            mMap.isMyLocationEnabled = true
        }

        //setUpMap()
    }

    private fun nearPlaces(typePlace: String) {
        //clear all marker on Map
        mMap.clear()

        //build url request base on location
        val url = getUrl(latitude, longitude, typePlace)

        iGoogleService.getNearByServices(url).enqueue(object : Callback<MyPlaces> {
            override fun onResponse(call: Call<MyPlaces>?, response: Response<MyPlaces>?) {
                currentPlace = response!!.body()!!
                if (response.isSuccessful) {
                    for (i in 0 until response.body()!!.results!!.size) {
                        val markerOptions = MarkerOptions()
                        val googlePlace = response.body()!!.results!![i]
                        val lat = googlePlace.geometry!!.location!!.lat
                        val lng = googlePlace.geometry!!.location!!.lng
                        val placeName = googlePlace.name
                        val latLng = LatLng(lat, lng)

                        markerOptions.position(latLng)
                        markerOptions.title(placeName)
                        if (typePlace.equals("Hospital"))
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_hospital))
                        else if (typePlace.equals("Restaurant"))
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_restaurant))
                        else if (typePlace.equals("School"))
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_school))
                        else if (typePlace.equals("Shopping"))
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_shopping))
                        else
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))

                        markerOptions.snippet(i.toString()) //Assign to snippet

                        mMap!!.addMarker(markerOptions)
                        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                        mMap!!.animateCamera(CameraUpdateFactory.zoomTo(15f))
                    }
                }
            }

            override fun onFailure(call: Call<MyPlaces>?, t: Throwable?) {
                Toast.makeText(baseContext, "Fail" + t!!.message, Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun getUrl(latitude: Double, longitude: Double, typePlace: String): String {
        val googlePlaceUrl = StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
        googlePlaceUrl.append("?location=$latitude,$longitude")
        googlePlaceUrl.append("&radius=1500")
        googlePlaceUrl.append("&type=$typePlace")
        googlePlaceUrl.append("&key=AIzaSyB_C4n0JW-byXt2LBexuLt2enbwnJKhkMQ")

        return googlePlaceUrl.toString()
    }
}



