package com.example.afifit.layout_handle.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationProvider
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.example.afifit.R
import com.example.afifit.databinding.FragmentProfileBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.afifit.data.UserProfile
import java.util.Locale

class profile : Fragment(), LocationListener {

    private val hideHandler = Handler(Looper.myLooper()!!)
    private lateinit var locationManager: LocationManager

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding

    private var visible: Boolean = false
    private lateinit var databaseReference: DatabaseReference

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val latitude = location.latitude
            val longitude = location.longitude
            // Do something with latitude and longitude
            updateLocationInfo(latitude, longitude)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            when (status) {
                LocationProvider.AVAILABLE -> {
                    // Location provider is available
                }

                LocationProvider.OUT_OF_SERVICE -> {
                    // Location provider is out of service
                }

                LocationProvider.TEMPORARILY_UNAVAILABLE -> {
                    // Location provider is temporarily unavailable
                }
            }
        }

        override fun onProviderEnabled(provider: String) {
            // Handle provider enabled
        }

        override fun onProviderDisabled(provider: String) {
            // Handle provider disabled
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): LinearLayout? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        databaseReference = FirebaseDatabase.getInstance().reference.child("userProfiles")

        visible = true
        // Location handling
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            initializeLocationManager()
        } else {
            requestLocationPermissions()
        }

        binding?.btnEditProfile?.setOnClickListener {
            val newFragment = edit_profile()
            val transaction: FragmentTransaction =
                requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.mainfragContaier, newFragment)
            transaction.addToBackStack(null)  // Optional: Add to back stack for navigation back
            transaction.commit()
        }

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // UserProfile
                val userProfile =
                    dataSnapshot.children.firstOrNull()?.getValue(UserProfile::class.java)
                userProfile?.let { updateUI(it) }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })

        binding?.profileBack?.setOnClickListener {
            parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }

    private fun initializeLocationManager() {
        locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            50,
            5f,
            this
        )
    }

    private fun requestLocationPermissions() {
        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Request location updates
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0f,
                locationListener
            )
        } else {
            // Request the location permission
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::locationManager.isInitialized) {
            locationManager.removeUpdates(locationListener)
        }
        _binding = null
    }

    override fun onLocationChanged(location: Location) {
        try {
            val latitude = location.latitude
            val longitude = location.longitude

            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)

            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val address = addresses[0]
                    val regionName = address.locality ?: address.subAdminArea ?: address.adminArea ?: "Unknown Region"
                    binding?.profileLocationtext?.text = regionName
                } else {
                    binding?.profileLocationtext?.text = "Location not available"
                }
            }
        } catch (e: Exception) {
            // Handle the exception, log it, or show an error message
            e.printStackTrace()
            binding?.profileLocationtext?.text = "Error retrieving location"
        }
    }


    private fun updateLocationInfo(latitude: Double, longitude: Double) {
        binding?.profileLocationtext?.text = "Latitude: $latitude, Longitude: $longitude"
    }

    private fun updateUI(userProfile: UserProfile) {
        // Load the image using Glide
        activity?.runOnUiThread {
            binding?.let {
                    it1 ->
                Glide.with(it1.profileImageProfile)
                    .load(userProfile.imageUrl)
                    .into(it1.profileImageProfile)
            }
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 123
    }
}
