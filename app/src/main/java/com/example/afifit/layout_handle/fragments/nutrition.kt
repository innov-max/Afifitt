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
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*

class nutrition : Fragment() {

    private var _binding: FragmentNutritionBinding? = null
    private val binding get() = _binding!!
    private lateinit var pieChart: PieChart
    private lateinit var lineChart: LineChart
    private lateinit var spinnerTimePeriod: Spinner
    private lateinit var databaseReference: DatabaseReference
    private var currentJob: Job? = null // Track current coroutine job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(requireContext())
        databaseReference = FirebaseDatabase.getInstance().reference.child("bpm")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNutritionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        setupFirebaseListener()
        setupSpinner()
        setupLineChart("Last 7 Days")
        setupPieChart()
        setupCarouselWithTimer()
    }

    private fun setupViews() {
        pieChart = binding.piechart
        lineChart = binding.lineChart
        spinnerTimePeriod = binding.spinnerTimePeriod
    }

    private fun setupFirebaseListener() {
        // No need for ValueEventListener, using coroutines instead
    }

    private suspend fun fetchData(timePeriod: String): List<HealthReading> = withContext(Dispatchers.IO) {
        val query = when (timePeriod) {
            "Last 7 Days" -> databaseReference.orderByChild("timestamp").startAt(getTimestampDaysAgo(7).toDouble())
            "Today" -> databaseReference.orderByChild("timestamp").startAt(getStartOfDayTimestamp().toDouble())
            "Yesterday" -> databaseReference.orderByChild("timestamp").startAt(getStartOfYesterdayTimestamp().toDouble())
            "Last 2 Weeks" -> databaseReference.orderByChild("timestamp").startAt(getTimestampDaysAgo(14).toDouble())
            else -> databaseReference
        }

        val snapshot = query.get().await()  // Assuming you have implemented await() correctly

        val readings = mutableListOf<HealthReading>()
        snapshot.children.forEach { dataSnapshotChild ->
            val bpm = dataSnapshotChild.child("bpm").getValue(Float::class.java) ?: 0f
            val bloodOxygen = dataSnapshotChild.child("bloodOxygen").getValue(Float::class.java) ?: 0f
            val avgBpm = dataSnapshotChild.child("avgBpm").getValue(Long::class.java)?.toFloat() ?: 0f
            val timestamp = dataSnapshotChild.child("timestamp").getValue(Long::class.java) ?: 0L

            readings.add(HealthReading(bpm, bloodOxygen, avgBpm, timestamp))
        }

        readings
    }


    private fun updatePieChart(readings: List<HealthReading>) {
        // Same as before
        val recommendedNutrients = calculateRecommendedNutrients(readings)

        val entries = listOf(
            PieEntry(recommendedNutrients.carbs, "Carbs"),
            PieEntry(recommendedNutrients.proteins, "Proteins"),
            PieEntry(recommendedNutrients.fats, "Fats")
        )

        // Rest of the pie chart setup
        // ...
    }

    private fun calculateRecommendedNutrients(readings: List<HealthReading>): RecommendedNutrients {
        // Example logic to calculate recommended percentages based on readings
        // Replace with your actual logic
        val totalBpm = readings.map { it.bpm }.average()
        val totalBloodOxygen = readings.map { it.bloodOxygen }.average()

        // Example thresholds (replace with your actual thresholds)
        val carbThreshold = 80f
        val proteinThreshold = 95f
        val fatThreshold = 60f

        val carbs = if (totalBpm > carbThreshold && totalBloodOxygen > carbThreshold) 40f else 30f
        val proteins = if (totalBpm > proteinThreshold && totalBloodOxygen > proteinThreshold) 30f else 25f
        val fats = if (totalBpm > fatThreshold && totalBloodOxygen > fatThreshold) 30f else 45f

        return RecommendedNutrients(carbs, proteins, fats)
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

    private fun setupPieChart() {
        // Dummy data for initialization
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

        pieChart.invalidate()
    }

    private fun setupLineChart(timePeriod: String) {
        currentJob?.cancel() // Cancel previous job if any
        currentJob = GlobalScope.launch {
            try {
                val entries = fetchData(timePeriod).mapIndexed { index, reading ->
                    Entry(index.toFloat(), reading.avgBpm)
                }
                updateLineChart(entries, timePeriod)
            } catch (e: Exception) {
                // Handle exceptions
            }
        }
    }

    private fun updateLineChart(entries: List<Entry>, timePeriod: String) {
        // Same as before
        // ...
    }

    private fun setupCarouselWithTimer() {
        val viewPager: ViewPager2 = binding.viewPager
        val items = listOf(
            CarouselItem(R.drawable.chapo, "Lunch: Chapo Veges and Beans", "Chapo Veges and Beans typically offers moderate levels of fat " +
                    "and carbs, with higher protein content from beans." +
                    " It's rich in vitamins like A and C from vegetables, contributing to its nutritional profile."),
            CarouselItem(R.drawable.breko, "Breakfast: Omelette, Sandwich and tea", "Omelette, Sandwich, and Tea provide a balanced meal with moderate protein from eggs in the omelette," +
                    " carbohydrates from bread in the sandwich, and comforting hydration from tea, enriched with " +
                    "antioxidants and a small amount of milk or sugar."),
            CarouselItem(R.drawable.sapa, "Dinner: Ugali Fish and Cabbage", "Ugali, Fish, and Cabbage offer a nutritious meal: Ugali provides complex carbohydrates, " +
                    "fish offers lean protein and omega-3 fatty acids for heart health, " +
                    "while cabbage contributes fiber, vitamins (like C and K), and minerals, supporting digestion and overall immunity.")
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
        currentJob?.cancel()
    }

    data class HealthReading(
        val bpm: Float,
        val bloodOxygen: Float,
        val avgBpm: Float,
        val timestamp: Long
    )

    data class RecommendedNutrients(
        val carbs: Float,
        val proteins: Float,
        val fats: Float
    )

    private fun getTimestampDaysAgo(days: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        return calendar.timeInMillis
    }

    private fun getStartOfDayTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getStartOfYesterdayTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
