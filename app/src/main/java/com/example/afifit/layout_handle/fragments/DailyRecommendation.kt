package com.example.afifit.layout_handle.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.example.afifit.R
import com.example.afifit.data.CarouselAdapter
import com.example.afifit.data.CarouselItem
import com.example.afifit.databinding.FragmentDailyRecommendationBinding
import java.util.Timer
import java.util.TimerTask


class DailyRecommendation : Fragment() {
    private var _binding: FragmentDailyRecommendationBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDailyRecommendationBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setupCarouselWithTimer()

    }
    private fun setupCarouselWithTimer() {
        val viewPager: ViewPager2 = binding.viewPager
        val items = listOf(
            CarouselItem(R.drawable.hydrate, "Reminder", "Please Hydrate adequately"),
            CarouselItem(R.drawable.meal1, "Meal", "Please check your Meals to get your vitals up to task"),
            CarouselItem(R.drawable.med, "Medication", "Kindly Check your medication")
        )

        val adapter = CarouselAdapter(items)
        viewPager.adapter = adapter

        val timer = Timer()
        val handler = Handler(Looper.getMainLooper())
        val updateCarouselTask = object : TimerTask() {
            override fun run() {
                handler.post {
                    viewPager.currentItem = (viewPager.currentItem + 1) % items.size
                }
            }
        }
        timer.scheduleAtFixedRate(updateCarouselTask, 5000, 5000) // Auto-scroll every 5 seconds
    }


}