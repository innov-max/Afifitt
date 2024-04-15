package com.example.afifit.layout_handle.fragments

import android.app.Activity.RESULT_OK
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.afifit.R
import com.example.afifit.data.EditProfileViewModel
import com.example.afifit.data.UserProfile
import com.example.afifit.databinding.FragmentEditProfileBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class edit_profile : Fragment() {
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var imageView: ImageView
    private lateinit var editTextName: EditText
    private lateinit var editTextOccupation: EditText
    private lateinit var editTextAbout: EditText

    private lateinit var databaseReference: DatabaseReference
    private lateinit var buttonSelectImage: TextView
    private lateinit var buttonPushData: Button

    private lateinit var viewModel: EditProfileViewModel

    companion object {
        const val REQUEST_IMAGE_PICK = 1
        const val NOTIFICATION_CHANNEL_ID = "channel_id"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseReference = FirebaseDatabase.getInstance().reference.child("userProfiles")
        viewModel = ViewModelProvider(this).get(EditProfileViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            val imageUri: Uri = data.data!!
            viewModel.selectedImageUri = imageUri
            Glide.with(requireContext()).load(imageUri).into(imageView)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageView = binding.profileImage
        editTextName = binding.nameEditText
        editTextOccupation = binding.occupation
        editTextAbout = binding.EditAbout

        buttonSelectImage = binding.EditProfile
        buttonPushData = binding.btnUpdate

        buttonSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }

        buttonPushData.setOnClickListener {
            if (!isNetworkAvailable()) {
                Toast.makeText(
                    context,
                    "No network available. Please check your connection.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val name = editTextName.text.toString()
            val occupation = editTextOccupation.text.toString()
            val about = editTextAbout.text.toString()

            if (imageView.drawable == null) {
                Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show()
            } else if (TextUtils.isEmpty(name) || TextUtils.isEmpty(occupation) || TextUtils.isEmpty(
                    about
                )
            ) {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch {
                    val userId = databaseReference.push().key
                    if (userId != null) {
                        val imageUri = viewModel.selectedImageUri

                        if (imageUri != null) {
                            withContext(Dispatchers.IO) {
                                // Upload the image to Firebase Storage
                                pushImageToFirebaseStorage(imageUri, userId)
                            }

                            showNotification("Profile Update", "Image uploaded successfully")

                            // Load the image from Firebase Storage using the download URL
                            loadImageFromFirebaseStorage(userId)

                            // Save the rest of the user profile data to the Realtime Database
                            val userProfile = UserProfile(name, occupation, about)
                            databaseReference.child(userId).setValue(userProfile)

                            Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                        } else {
                            showNotification("Profile Update", "Error getting image URI")
                        }
                    } else {
                        showNotification("Profile Update", "An error occurred, check your connection")
                    }
                }
            }
        }

        binding.profileBack.setOnClickListener {
            parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }

    private fun pushImageToFirebaseStorage(imageUri: Uri, userId: String) {
        val storageReference =
            Firebase.storage.reference.child("userProfileImages/$userId.jpg")

        // Upload the image to Firebase Storage
        storageReference.putFile(imageUri).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                showNotification(
                    "Profile Update",
                    "Error uploading image to Firebase Storage"
                )
            }
        }
    }

    private fun loadImageFromFirebaseStorage(userId: String) {
        val storageReference =
            Firebase.storage.reference.child("userProfileImages/$userId.jpg")

        // Load the image from Firebase Storage using the download URL
        storageReference.downloadUrl.addOnSuccessListener { uri ->
            // Load the image using Glide
            Glide.with(requireContext())
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.anne)
                .error(R.drawable.anne)
                .into(binding.profileImage)
        }.addOnFailureListener { exception ->
            showNotification(
                "Profile Update",
                "Error loading image from Firebase Storage"
            )
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Profile",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Update profile"
            channel.enableLights(true)
            channel.lightColor = Color.BLUE
            channel.enableVibration(true)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(requireContext(), NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notification = builder.build()

        notificationManager.notify(NOTIFICATION_ID, notification)

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            notificationManager.cancel(NOTIFICATION_ID)
        }, 10000)
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
