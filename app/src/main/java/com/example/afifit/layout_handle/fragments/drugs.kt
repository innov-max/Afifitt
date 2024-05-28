package com.example.afifit.layout_handle.fragments

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.afifit.R
import com.example.afifit.data.Drug
import com.example.afifit.data.DrugAdapter
import com.example.afifit.databinding.FragmentDrugsBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class drugs : Fragment() {

    private var binding: FragmentDrugsBinding? = null
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DrugAdapter
    private val databaseReference = FirebaseDatabase.getInstance().reference.child("Drugs")
    private val drugList = mutableListOf<Drug>()
    private var selectedPhotoUri: Uri? = null

    companion object {
        private const val PICK_PHOTO_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { }
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

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = DrugAdapter(drugList)
        recyclerView.adapter = adapter

        // Fetch drugs from Firebase
        fetchDrugsFromFirebase()

        // Handle FAB click
        val fabAddDrug: FloatingActionButton = view.findViewById(R.id.fab_add_drug)
        fabAddDrug.setOnClickListener {
            showAddDrugDialog()
        }

        setupItemTouchHelper()
    }

    private fun showAddDrugDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_drug, null)
        val drugNameEditText: EditText = dialogView.findViewById(R.id.edit_text_drug_name)
        val drugTimeEditText: EditText = dialogView.findViewById(R.id.edit_text_drug_time)
        val selectPhotoButton: Button = dialogView.findViewById(R.id.button_select_photo)
        val drugPhotoImageView: ImageView = dialogView.findViewById(R.id.image_view_drug_photo)
        val addDrugButton: Button = dialogView.findViewById(R.id.button_add_drug)

        selectPhotoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_PHOTO_REQUEST)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add Drug")
            .setView(dialogView)
            .create()

        addDrugButton.setOnClickListener {
            val drugName = drugNameEditText.text.toString()
            val drugTime = drugTimeEditText.text.toString()
            val photoUri = selectedPhotoUri?.toString()

            if (drugName.isNotEmpty() && drugTime.isNotEmpty() && photoUri != null) {
                val drug = Drug(drugName, drugTime, photoUri)
                pushDrugToFirebase(drug)
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PHOTO_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data != null && data.data != null) {
            selectedPhotoUri = data.data
            // Update the ImageView in the dialog with the selected photo
            val drugPhotoImageView: ImageView? = view?.findViewById(R.id.image_view_drug_photo)
            drugPhotoImageView?.visibility = View.VISIBLE
            drugPhotoImageView?.setImageURI(selectedPhotoUri)
        }
    }

    private fun fetchDrugsFromFirebase() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                drugList.clear()
                for (dataSnapshot in snapshot.children) {
                    val drug = dataSnapshot.getValue(Drug::class.java)
                    if (drug != null) {
                        drugList.add(drug)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load drugs", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun pushDrugToFirebase(drug: Drug) {
        val drugKey = databaseReference.push().key
        if (drugKey != null) {
            databaseReference.child(drugKey).setValue(drug).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Drug added successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to add drug", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Error Occurred", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeDrugFromFirebase(drugKey: String) {
        databaseReference.child(drugKey).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Drug removed successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to remove drug", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupItemTouchHelper() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val drug = drugList[position]
                val drugKey = drug.key ?: return
                removeDrugFromFirebase(drugKey)
                drugList.removeAt(position)
                adapter.notifyItemRemoved(position)
            }
        })

        itemTouchHelper.attachToRecyclerView(recyclerView)
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
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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

        val builder = NotificationCompat.Builder(requireContext(), edit_profile.NOTIFICATION_CHANNEL_ID)
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
