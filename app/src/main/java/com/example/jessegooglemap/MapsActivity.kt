package com.example.jessegooglemap

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.jessegooglemap.databinding.ActivityMapsBinding
import com.google.android.gms.location.* // ktlint-disable no-wildcard-imports
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    // Declare variables
    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private val LOCATION_PERMISSION_REQUEST = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var databaseRef: DatabaseReference
    private var markerEmeka: Marker? = null

    /** This function checks if permission to access user location is granted or not,
     * if granted then enable user location.
     * */
    private fun getLocationAccess() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true // enable location
            getLocationUpdates()
            // startLocationUpdates()
        } else // request user to allow permission to access location
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
    }

    fun readPartnerLocation() {
        databaseRef.child("emeka").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val location = snapshot.getValue(LocationLogging::class.java)
                    if (location != null) {
                        val latLng = location.latitude?.let {
                            location.longitude?.let { it1 ->
                                LatLng(
                                    it,
                                    it1
                                )
                            }
                        }
                        if (latLng != null) {
                            markerEmeka?.position = latLng
                            map.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    latLng,
                                    15f
                                )
                            )
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    // check if permission is granted and enable location else toast a denied message

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                map.isMyLocationEnabled = true
            } else {
                Toast.makeText(
                    this,
                    "User has not granted location access permission",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseDatabase.getInstance().reference.child("hello").setValue("World")
        databaseRef = FirebaseDatabase.getInstance().reference
        Log.d("MainActivity", "onCreate: $databaseRef")

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun getLocationUpdates() {

        locationRequest = LocationRequest()
        locationRequest.interval = 7500
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationCallback = object : LocationCallback() {

            override fun onLocationResult(locationResult: LocationResult) {
                Log.d("MapTracking", "onLocationResult: ")

                if (locationResult.locations.isNotEmpty()) {

                    val location = locationResult.lastLocation
                    // map.addMarker(MarkerOptions().position(LatLng(location.latitude, location.longitude)).title("jesse"))

                    //  databaseRef = Firebase.database.reference
                    val locationlogging =
                        LocationLogging(location.latitude, location.longitude)
                    databaseRef.child("jesse").setValue(locationlogging)
                        .addOnSuccessListener {
                            Toast.makeText(
                                applicationContext,
                                "Locations written into the database",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                applicationContext,
                                "Error occured while writing the locations",
                                Toast.LENGTH_LONG
                            ).show()
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
//                override fun onMapReady(googleMap: GoogleMap) {
//                    // Add a marker in Sydney and move the camera
//                    // The latitude of Benin City, Nigeria is 6.339185, and the longitude is 5.617447
//                    /*val sydney = LatLng(6.339185, 5.617447)
//
//        val zoomLevel = 15f
//        mMap.addMarker(MarkerOptions().position(sydney).title("Benin City | Nigeria"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, zoomLevel))*/
//                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        markerEmeka = map.addMarker(MarkerOptions().position(LatLng(0.0, 0.5)).title("Emeka"))
        getLocationAccess()
        startLocationUpdates()
        readPartnerLocation()
    }

    fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }
}
