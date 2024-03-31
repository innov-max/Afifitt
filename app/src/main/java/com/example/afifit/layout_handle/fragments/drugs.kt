package com.example.afifit.layout_handle.fragments

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import com.example.afifit.R
import com.example.afifit.data.Prescription
import com.example.afifit.databinding.FragmentDashFrag1Binding
import com.example.afifit.databinding.FragmentDrugsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class drugs : Fragment() {



    private var binding: FragmentDrugsBinding? = null
    private val handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDrugsBinding.inflate(inflater, container, false)
        return binding?.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDrugsBinding.bind(view)

        binding!!.pushData.setOnClickListener {
            if (!isNetworkAvailable(requireContext())) {
                Toast.makeText(context, "No network available. Please check your connection.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(binding!!.radioBtn2.isChecked &&binding!!.radioBtn1.isChecked&&binding!!.radioBtn3.isChecked){


            lifecycleScope.launch {

                val currentTime = getCurrentTime()


                val message = "Drug taken at $currentTime"
                val prescription = Prescription(message)

                withContext(Dispatchers.IO) {
                    pushDataToFirebase(prescription)
                }

                Toast.makeText(requireContext(), "Notified practitioner", Toast.LENGTH_SHORT).show()
            }


            }else{
                Toast.makeText(context, "Please take all the days pills then check them above", Toast.LENGTH_SHORT).show()
            }

        }




    }
    private fun pushDataToFirebase(prescription: Prescription) {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("Precription")
        lifecycleScope.launch {
            val prescriptionKey = databaseReference.push().key
            if (prescriptionKey != null) {
                databaseReference.child(prescriptionKey).setValue(prescription)
            } else {
                Toast.makeText(context, "Error Occured", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
    private fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentTime = Calendar.getInstance().time
        return dateFormat.format(currentTime)
    }
    private fun showNotification(title: String, message: String) {
        val notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                edit_profile.NOTIFICATION_CHANNEL_ID,
                "Drug notification",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Prescription"
            channel.enableLights(true)
            channel.lightColor = Color.BLUE
            channel.enableVibration(true)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(requireContext(),
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




}
