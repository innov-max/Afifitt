package com.example.afifit.layout_handle.fragments


import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.afifit.R
import com.example.afifit.data.UserProfile
import com.example.afifit.databinding.FragmentDashFrag1Binding
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.storage
import data.HealthData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class dash_frag1 : Fragment(),SensorEventListener {

    private var binding: FragmentDashFrag1Binding? = null
    private lateinit var databaseReferenceUser: DatabaseReference
    private lateinit var databaseReferenceHealth: DatabaseReference
    private var bpmTextView: TextView? = null
    private var avgBpmTextView: TextView? = null
    private var bloodOxygenTextView: TextView? = null
    private var glucose: TextView? = null
    private var isWifiConnected: Boolean = false
    private var sensorManager: SensorManager? = null
    private var running = false
    private var totalSteps = 0f
    private var previousTotalSteps = 0f

    private lateinit var sharedPreferences: SharedPreferences
    private val ACTIVITY_RECOGNITION_REQUEST_CODE: Int = 100

    private val connectivityReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ConnectivityManager.CONNECTIVITY_ACTION) {
                isWifiConnected = checkWifiStatus()

                if (!isWifiConnected) {
                    showNotification("No Connection", "Connect to wifi or cellular data")
                } else {
                    showNotification("Yaay", "We are back online")
                }
            }
        }
    }

    private val handler = Handler(Looper.getMainLooper())

    private companion object {
        private const val READ_EXTERNAL_STORAGE_REQUEST_CODE = 1
        private const val profileImageUrlKey = "profileImageUrl"
    }

    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentDashFrag1Binding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDashFrag1Binding.bind(view)
        FirebaseApp.initializeApp(requireContext())
        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)

        bpmTextView = binding?.bpm
        avgBpmTextView = binding?.brr
        bloodOxygenTextView = binding?.OxyValue
        glucose = binding?.bpm
        userId = "yourUserId"


        displayGreeting()
        databaseReferenceUser = FirebaseDatabase.getInstance().reference.child("userProfiles")
        databaseReferenceHealth = FirebaseDatabase.getInstance().reference.child("healthData")

        binding?.profileImage?.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.addToBackStack(null)
            transaction.replace(R.id.mainfragContaier, profile())
            transaction.commit()
        }

        isWifiConnected = checkWifiStatus()

        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        requireContext().registerReceiver(connectivityReceiver, filter)

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_EXTERNAL_STORAGE_REQUEST_CODE
            )
        } else {
            loadImageWithGlide()
        }

        if (!isWifiConnected) {
            showNotification("No Connection", "Connect to wifi or cellular data")
        }

        databaseReferenceUser.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userProfile =
                    dataSnapshot.children.firstOrNull()?.getValue(UserProfile::class.java)
                userProfile?.let { updateUI(it) }

                readDataFromFirebaseAndDisplay(bpmTextView!!, avgBpmTextView, bloodOxygenTextView)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })

        databaseReferenceHealth.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val healthData = dataSnapshot.getValue(HealthData::class.java)
                healthData?.let {

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

        binding?.CvBtn?.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.addToBackStack(null)
            transaction.replace(R.id.mainfragContaier, ComputerVision())
            transaction.commit()
        }


        if (isPermissionGranted){

            requestPermission()
        }
        loadData()
        resetSteps()

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager

    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireContext().unregisterReceiver(connectivityReceiver)
        binding = null
    }

    override fun onResume() {
        super.onResume()
        running = true
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor == null) {
            Toast.makeText(
                requireContext(),
                "No sensor detected on this device",
                Toast.LENGTH_SHORT
            )
        } else {

            sensorManager?.registerListener(this,stepSensor,SensorManager.SENSOR_DELAY_UI)
        }
    }
    private fun requestPermission(){
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
    val ACTIVITY_RECOGNITION_REQUEST_CODE: Int
    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION),ACTIVITY_RECOGNITION_REQUEST_CODE)
}

    }
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun isPermissionGranted():Boolean{

        return ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACTIVITY_RECOGNITION) !=PackageManager.PERMISSION_GRANTED
    }



    private fun displayGreeting() {
        val currentTime = Calendar.getInstance().time
        val formattedTime = SimpleDateFormat("HH", Locale.getDefault()).format(currentTime).toInt()

        val greeting = when (formattedTime) {
            in 6..11 -> "Good Morning"
            in 12..17 -> "Good Afternoon"
            in 18..23, in 0..5 -> "Good Evening"
            else -> "Hello"
        }

        binding?.greeting?.text = greeting
    }
    private fun resetSteps(){

        binding!!.stepsTaken.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Long tap to reset steps",
                Toast.LENGTH_SHORT).show()
        }
        binding!!.stepsTaken.setOnLongClickListener {
            previousTotalSteps = totalSteps
            binding!!.stepsTaken.text = 0.toString()
            saveData()
            true
        }
    }
    private fun saveData(){

        val sharedPreferences = requireContext().getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val editor =sharedPreferences.edit()
        editor.putFloat("key1",previousTotalSteps)
        editor.apply()

    }
    private fun loadData(){

        val sharedPreferences = requireContext().getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val savedNumber:Float = sharedPreferences.getFloat("key1",0f)
        Log.d("dash","$savedNumber")
        previousTotalSteps = savedNumber

    }

    private fun checkWifiStatus(): Boolean {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities =
                connectivityManager?.getNetworkCapabilities(connectivityManager.activeNetwork)
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true ||
                    networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
        } else {
            @Suppress("DEPRECATION")
            val activeNetworkInfo = connectivityManager?.activeNetworkInfo
            activeNetworkInfo?.type == ConnectivityManager.TYPE_WIFI ||
                    activeNetworkInfo?.type == ConnectivityManager.TYPE_MOBILE
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                edit_profile.NOTIFICATION_CHANNEL_ID,
                "Wifi connection",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Internet Connection"
            channel.enableLights(true)
            channel.lightColor = Color.BLUE
            channel.enableVibration(true)
            notificationManager.createNotificationChannel(channel)
        }
        val builder = NotificationCompat.Builder(
            requireContext(),
            edit_profile.NOTIFICATION_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        val notification = builder.build()
        notificationManager.notify(edit_profile.NOTIFICATION_ID, notification)
        handler.postDelayed({
            notificationManager.cancel(edit_profile.NOTIFICATION_ID)
        }, 10000)
    }

    private fun updateUI(userProfile: UserProfile) {

        binding?.UserName?.text = userProfile.name

        val storedImageUrl = sharedPreferences.getString(profileImageUrlKey, "")
        if (storedImageUrl?.isNotEmpty() == true) {
            Glide.with(requireContext())
                .load(storedImageUrl)
                .into(binding?.profileImage!!)
        } else {
            // If URL is not stored, load placeholder
            binding?.let {
                Glide.with(requireContext())
                    .load(R.drawable.anne)
                    .into(it.profileImage)
            }
        }


    }

    private fun loadImageWithGlide() {
        val storageReference =
            Firebase.storage.reference.child("userProfileImages/$userId.jpg")
        storageReference.metadata.addOnSuccessListener { metadata ->
            if (metadata.sizeBytes > 0) {
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    sharedPreferences.edit().putString(profileImageUrlKey, uri.toString()).apply()
                    Glide.with(requireContext())
                        .load(uri)
                        .into(binding?.profileImage!!)
                }.addOnFailureListener { exception ->
                    Log.e(TAG, "Error getting download URL: $exception")
                }
            } else {

                Log.e(TAG, "File does not exist at the specified location.")
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error getting metadata: $exception")
        }
    }


    private fun readDataFromFirebaseAndDisplay(
        bpmTextView: TextView,
        avgBpmTextView: TextView?,
        bloodOxygenTextView: TextView?,

    ) {
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("/bpm")

        databaseReference.orderByChild("timestamp").limitToLast(1)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val latestUserId = dataSnapshot.children.lastOrNull()?.key

                        if (latestUserId != null) {
                            val latestUserData = dataSnapshot.child(latestUserId)
                            val bpm =
                                latestUserData.child("bpm").getValue(Float::class.java)
                            val avgBpm =
                                latestUserData.child("avgBpm").getValue(Int::class.java)
                            val bloodOxygen =
                                latestUserData.child("bloodOxygen").getValue(Int::class.java)
                            val timestamp =
                                latestUserData.child("timestamp").getValue(Long::class.java)

                            val bpmText = "BPM: ${bpm ?: 0.0f}"
                            val avgBpmText = "${avgBpm ?: 0} Per Min"
                            val bloodOxygenText = " ${bloodOxygen ?: 0}%"

                            val formattedTime =
                                SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                    .format(timestamp ?: 0L)

                            handler.post {
                                bpmTextView.text = bpmText
                                avgBpmTextView?.text = avgBpmText
                                bloodOxygenTextView?.text = bloodOxygenText

                                val Glucose = bpm
                                val GlucoseConvert = (Glucose?.div(2)?.times(3))
                                binding?.TempValue?.text = GlucoseConvert.toString()

                                binding?.time?.text = "Last update:$formattedTime"

                                if(GlucoseConvert.toString() <= 44.toString() && bpmText > 0.toString()){

                                    showNotification("BPM Alert", "Low glucose levels please contact health practitioner")
                                } else if(GlucoseConvert.toString() <= 84.toString() && bpmText >= 45.toString()){

                                    showNotification("BPM Alert", "glucose levels within range no need to worry")
                                }else if(bpmText >= 85 .toString()){
                                    showNotification("BPM Alert", "glucose levels above normal please contact practitioner")
                                }else
                                {
                                    showNotification("BPM Alert", "The wearable is getting inaccurate data please contact practitioner")

                                }
                            }
                        } else {
                            handler.post {
                                bpmTextView.text = "No data available"
                                avgBpmTextView?.text = "No data available"
                                bloodOxygenTextView?.text = "No data available"
                                binding?.time?.text = "Last Update: N/A"
                            }
                        }
                    } else {
                        handler.post {
                            bpmTextView.text = "No data available"
                            avgBpmTextView?.text = "No data available"
                            binding?.time?.text = "Last Update: N/A"
                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    handler.post {
                        bpmTextView.text = "Failed to read data: ${error.message}"
                        avgBpmTextView?.text = "Failed to read data: ${error.message}"
                        bloodOxygenTextView?.text = "Failed to read data: ${error.message}"
                    }
                }
            })
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (running) {
            totalSteps = event!!.values[0]
            val currentSteps: Int = totalSteps.toInt() - previousTotalSteps.toInt()
            binding!!.stepsTaken.text =("$currentSteps")
            binding!!.progressCircular.apply {
                setProgressWithAnimation(currentSteps.toFloat())

            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
}
