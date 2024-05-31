package com.example.afifit.layout_handle.fragments

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.afifit.R
import com.example.afifit.data.Drug
import com.example.afifit.data.DrugAdapter
import com.example.afifit.databinding.FragmentDrugsBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.ByteBuffer
import java.nio.ByteOrder

class drugs : Fragment() {

    private var binding: FragmentDrugsBinding? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DrugAdapter
    private val databaseReference = FirebaseDatabase.getInstance().reference.child("Drugs")
    private val drugList = mutableListOf<Drug>()
    private var selectedPhotoUri: Uri? = null
    private lateinit var tflite: Interpreter
    private var currentPhotoPath: String? = null

    companion object {
        private const val PICK_PHOTO_REQUEST = 1
        private const val REQUEST_IMAGE_CAPTURE = 2
        private const val IMAGE_SIZE = 150
        private const val MODEL_PATH = "drug_classification_model.tflite"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { }

        try {
            tflite = Interpreter(loadModelFile())
        } catch (e: IOException) {
            e.printStackTrace()
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

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = DrugAdapter(drugList)
        recyclerView.adapter = adapter

        fetchDrugsFromFirebase()

        val fabAddDrug: FloatingActionButton = view.findViewById(R.id.fab_add_drug)
        fabAddDrug.setOnClickListener {
            showAddDrugDialog()
        }
    }

    private fun showAddDrugDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_drug, null)
        val drugNameEditText: EditText = dialogView.findViewById(R.id.edit_text_drug_name)
        val drugTimeEditText: EditText = dialogView.findViewById(R.id.edit_text_drug_time)
        val selectPhotoButton: FloatingActionButton = dialogView.findViewById(R.id.button_select_photo)
        val capturePhotoButton: FloatingActionButton = dialogView.findViewById(R.id.btnOpencv)
        val drugPhotoImageView: ImageView = dialogView.findViewById(R.id.image_view_drug_photo)
        val addDrugButton: Button = dialogView.findViewById(R.id.button_add_drug)

        selectPhotoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_PHOTO_REQUEST)
        }

        capturePhotoButton.setOnClickListener {
            dispatchTakePictureIntent()
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

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                null
            }
            if (photoFile != null) {
                val photoURI: Uri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.example.afifit.fileprovider",
                    photoFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val imageFileName = "JPEG_${System.currentTimeMillis()}_"
        val storageDir = requireActivity().getExternalFilesDir(null)
        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PHOTO_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data != null && data.data != null) {
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, selectedPhotoUri)
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, false)
            val drugName = classifyImage(resizedBitmap)
            Toast.makeText(requireContext(), "Identified Drug: $drugName", Toast.LENGTH_SHORT).show()

            val drugPhotoImageView: ImageView? = view?.findViewById(R.id.image_view_drug_photo)
            drugPhotoImageView?.visibility = View.VISIBLE
            drugPhotoImageView?.setImageURI(selectedPhotoUri)
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {
            val file = File(currentPhotoPath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, false)
                val drugName = classifyImage(resizedBitmap)
                Toast.makeText(requireContext(), "Identified Drug: $drugName", Toast.LENGTH_SHORT).show()

                val drugPhotoImageView: ImageView? = view?.findViewById(R.id.image_view_drug_photo)
                drugPhotoImageView?.visibility = View.VISIBLE
                drugPhotoImageView?.setImageBitmap(resizedBitmap)
            }
        }
    }

    private fun classifyImage(bitmap: Bitmap): String {
        val byteBuffer = convertBitmapToByteBuffer(bitmap)
        val output = Array(1) { FloatArray(10) }
        tflite.run(byteBuffer, output)

        val predictedIndex = output[0].indices.maxByOrNull { output[0][it] } ?: -1
        val classNames = arrayOf("Drug1", "Drug2", "Drug3", "Drug4", "Drug5", "Drug6", "Drug7", "Drug8", "Drug9", "Drug10")
        return if (predictedIndex != -1) classNames[predictedIndex] else "Unknown"
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * IMAGE_SIZE * IMAGE_SIZE * 3)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(IMAGE_SIZE * IMAGE_SIZE)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        for (pixelValue in intValues) {
            byteBuffer.putFloat(((pixelValue shr 16) and 0xFF) / 255.0f)
            byteBuffer.putFloat(((pixelValue shr 8) and 0xFF) / 255.0f)
            byteBuffer.putFloat((pixelValue and 0xFF) / 255.0f)
        }
        return byteBuffer
    }

    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = requireContext().assets.openFd(MODEL_PATH)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
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
}
