package com.example.afifit.layout_handle.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.applandeo.materialcalendarview.EventDay
import com.example.afifit.R
import com.example.afifit.data.CalenderDialogFragment
import com.example.afifit.databinding.FragmentNutritionBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.picasso.Picasso
import data.ApiClient
import data.NutritionResponse
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar


class nutrition : Fragment() {

    private var _binding: FragmentNutritionBinding? = null
    private val binding get() = _binding
    private lateinit var btnOpenCalendar: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): LinearLayout? {
        _binding = FragmentNutritionBinding.inflate(inflater, container, false)
        return binding?.root
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FirebaseApp.initializeApp(requireContext())
        // ... Existing code ...

        // Fetch breakfast diet data when the fragment is created
        GlobalScope.launch(Dispatchers.Main) {
            fetchBreakfastDiet()
            sendTestNotification()
        }

        btnOpenCalendar = binding?.calender!!
        btnOpenCalendar.setOnClickListener {
            // Show the CalendarDialogFragment when the button is clicked
            showCalendarDialog()
        }

    }

    private fun showCalendarDialog() {
        // Create an instance of the CalendarDialogFragment
        val calendarDialogFragment = CalenderDialogFragment()

        // Set up events for the calendar (replace with your actual data fetching logic)
        val eventDays = getEventDays()
        calendarDialogFragment.setEvents(eventDays)

        // Show the CalendarDialogFragment
        val fragmentManager = childFragmentManager // Use childFragmentManager for fragments
        calendarDialogFragment.show(fragmentManager, "CalendarDialogFragment")
    }

    private fun getEventDays(): List<EventDay> {
        // Implement this method to fetch meal data from Firebase for the selected day
        // Return a list of EventDay objects representing meal data

        // Example: Fetch meal data from Firebase Firestore
        val selectedDate = Calendar.getInstance() // Replace this with the actual selected date
        selectedDate.add(Calendar.DAY_OF_MONTH, 1) // For demonstration purposes, adding 1 day

        val db = FirebaseFirestore.getInstance()

        // Replace "meals" with the actual collection name in your Firestore database
        val mealsCollection = db.collection("meals")

        val eventDays = mutableListOf<EventDay>()

        // Query Firestore for meals on the selected date
        mealsCollection
            .whereEqualTo("date", selectedDate.time)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // Replace "mealDate" with the actual field name in your Firestore document
                    val mealDate = document.getDate("date")

                    mealDate?.let {
                        // Use the appropriate constructor for EventDay based on your requirements
                        val eventDay = EventDay(selectedDate, R.drawable.logo)
                        eventDays.add(eventDay)
                    }
                }

                // Notify the CalendarDialogFragment about the updated events
                val calendarDialogFragment =
                    childFragmentManager.findFragmentByTag("CalendarDialogFragment") as? CalenderDialogFragment
                calendarDialogFragment?.setEvents(eventDays)
            }
            .addOnFailureListener {
                // Handle errors here
            }

        return eventDays
    }




    private suspend fun fetchBreakfastDiet() {
        try {
            val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            val query = "proteins" // You can customize the query based on your needs

            val response = withContext(Dispatchers.IO) {
                ApiClient.nutritionService.getBreakfastDiet(query,"2bf64324","4ff1583b62da0c4d7bf50dc05992f00e")
            }

            if (response.isSuccessful) {
                val breakfastDiet = response.body()?.get((day % response.body()?.size!!) ?: 0)
                updateUI(breakfastDiet)

            } else {
                val context = requireContext()
                Toast.makeText(context, "Api connection servered", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            // Handle exceptions
        }
    }
    private fun sendTestNotification() {
        FirebaseMessaging.getInstance().subscribeToTopic("updates")
    }

    private fun updateUI(breakfastDiet: NutritionResponse?) {
        binding?.let {
            // Assuming you have these views in your layout
            val breakfastImageView = it.ImgBreakfast
            val breakfastLabelTextView = it.BreakfastFood

            // Update ImageView with the breakfast image using Picasso
            Picasso.get().load(breakfastDiet?.image).into(breakfastImageView)

            // Update TextView with the breakfast label
            breakfastLabelTextView.text = breakfastDiet?.label
            Toast.makeText(context, "Connection established", Toast.LENGTH_SHORT).show()
        }

    }


    // ... Existing code ...

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
