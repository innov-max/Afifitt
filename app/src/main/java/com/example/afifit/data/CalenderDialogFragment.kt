package com.example.afifit.data

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.example.afifit.databinding.FragmentCalenderDialogBinding

class CalenderDialogFragment : DialogFragment() {

    private lateinit var calendarView: com.applandeo.materialcalendarview.CalendarView
    private var _binding: FragmentCalenderDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentCalenderDialogBinding.inflate(inflater, container, false)
        return binding.root


    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.calendarView.setOnDayClickListener(object : OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                val clickedDayCalendar: java.util.Calendar = eventDay.calendar
            }
        })


    }

    // Method to set events in the calendar
    fun setEvents(eventDays: List<EventDay>) {
        calendarView.setEvents(eventDays)
    }


}