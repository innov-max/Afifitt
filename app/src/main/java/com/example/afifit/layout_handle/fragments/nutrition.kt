package com.example.afifit.layout_handle.fragments

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.afifit.R
import com.example.afifit.data.CarouselAdapter
import com.example.afifit.data.CarouselItem
import com.example.afifit.databinding.FragmentNutritionBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

class nutrition : Fragment() {

    private var _binding: FragmentNutritionBinding? = null
    private val binding get() = _binding!!
    private lateinit var btnOpenCalendar: ImageView
    private lateinit var pieChart: PieChart
    private lateinit var lineChart: LineChart
    private lateinit var spinnerTimePeriod: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNutritionBinding.inflate(inflater, container, false)
        return binding.root
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        GlobalScope.launch(Dispatchers.Main) {
            sendTestNotification()
            pieChart = binding.piechart
            lineChart = binding.lineChart
            spinnerTimePeriod = binding.spinnerTimePeriod

            setupSpinner()
            setupLineChart("Last 7 Days") // Default to last 7 days
            setupPieChart()
            setupCarouselWithTimer()
        }
    }

    private fun setupSpinner() {
        val timePeriods = listOf("Select Time", "Last 7 Days", "Today", "Yesterday", "Last 2 Weeks")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, timePeriods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTimePeriod.adapter = adapter

        spinnerTimePeriod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    1 -> setupLineChart("Last 7 Days")
                    2 -> setupLineChart("Today")
                    3 -> setupLineChart("Yesterday")
                    4 -> setupLineChart("Last 2 Weeks")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun sendTestNotification() {
        FirebaseMessaging.getInstance().subscribeToTopic("updates")
    }

    private fun setupPieChart() {
        val entries = listOf(
            PieEntry(40f, "Carbs"),
            PieEntry(30f, "Proteins"),
            PieEntry(30f, "Fats")
        )

        val totalKcal = entries.sumOf { it.value.toDouble() }.toFloat()

        val dataSet = PieDataSet(entries, "Nutrients").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 16f
            valueTextColor = Color.BLACK
            sliceSpace = 3f
        }

        val data = PieData(dataSet).apply {
            setValueTextSize(16f)
        }

        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.isRotationEnabled = true
        pieChart.animateY(1000)

        pieChart.centerText = "Total Kcal\n$totalKcal"
        pieChart.setCenterTextSize(24f)
        pieChart.setCenterTextColor(Color.BLACK)

        pieChart.setDrawEntryLabels(true)
        pieChart.setEntryLabelTextSize(16f)
        pieChart.setEntryLabelColor(Color.BLACK)
    }


    private fun setupLineChart(timePeriod: String) {
        val entries = when (timePeriod) {
            "Last 7 Days" -> getLast7DaysData()
            "Today" -> getTodayData()
            "Yesterday" -> getYesterdayData()
            "Last 2 Weeks" -> getLast2WeeksData()
            else -> getLast7DaysData()
        }

        val dataSet = LineDataSet(entries, timePeriod).apply {
            color = ColorTemplate.COLORFUL_COLORS[0]
            setCircleColor(ColorTemplate.COLORFUL_COLORS[0])
            circleRadius = 5f
            circleHoleRadius = 2.5f
            setDrawCircleHole(true)
            lineWidth = 2f
            valueTextSize = 12f

            setDrawFilled(true)
            fillColor = Color.parseColor("#B2DFDB")
        }

        val data = LineData(dataSet).apply {
            setValueTextSize(12f)
        }

        lineChart.data = data
        lineChart.description.isEnabled = false
        lineChart.animateX(1000)

        lineChart.axisLeft.isEnabled = false
        lineChart.axisRight.isEnabled = false
        lineChart.xAxis.setDrawGridLines(false)
        lineChart.axisLeft.setDrawGridLines(false)
        lineChart.axisRight.setDrawGridLines(false)

        val labels = when (timePeriod) {
            "Last 7 Days" -> listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            "Today" -> listOf("Hour 1", "Hour 2", "Hour 3", "Hour 4", "Hour 5", "Hour 6", "Hour 7", "Hour 8", "Hour 9", "Hour 10", "Hour 11", "Hour 12")
            "Yesterday" -> listOf("Hour 1", "Hour 2", "Hour 3", "Hour 4", "Hour 5", "Hour 6", "Hour 7", "Hour 8", "Hour 9", "Hour 10", "Hour 11", "Hour 12")
            "Last 2 Weeks" -> List(14) { "Day ${it + 1}" }
            else -> listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        }
        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        lineChart.xAxis.granularity = 1f
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM

        lineChart.invalidate()
    }

    private fun getLast7DaysData(): List<Entry> {
        return listOf(
            Entry(0f, 60f),
            Entry(1f, 65f),
            Entry(2f, 70f),
            Entry(3f, 75f),
            Entry(4f, 80f),
            Entry(5f, 85f),
            Entry(6f, 90f)
        )
    }

    private fun getTodayData(): List<Entry> {
        return listOf(
            Entry(0f, 55f),
            Entry(1f, 60f),
            Entry(2f, 65f),
            Entry(3f, 70f),
            Entry(4f, 75f),
            Entry(5f, 80f),
            Entry(6f, 85f),
            Entry(7f, 90f),
            Entry(8f, 95f),
            Entry(9f, 100f),
            Entry(10f, 105f),
            Entry(11f, 110f)
        )
    }

    private fun getYesterdayData(): List<Entry> {
        return listOf(
            Entry(0f, 50f),
            Entry(1f, 55f),
            Entry(2f, 60f),
            Entry(3f, 65f),
            Entry(4f, 70f),
            Entry(5f, 75f),
            Entry(6f, 80f),
            Entry(7f, 85f),
            Entry(8f, 90f),
            Entry(9f, 95f),
            Entry(10f, 100f),
            Entry(11f, 105f)
        )
    }

    private fun getLast2WeeksData(): List<Entry> {
        return List(14) { i -> Entry(i.toFloat(), 60f + i * 5f) }
    }

    private fun setupCarouselWithTimer() {
        val viewPager: ViewPager2 = binding.viewPager
        val items = listOf(
            CarouselItem(R.drawable.anne, "Title 1", "Description 1"),
            CarouselItem(R.drawable.supp, "Title 2", "Description 2"),
            CarouselItem(R.drawable.white_pill, "Title 3", "Description 3")
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
