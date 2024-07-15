package com.example.afifit.layout_handle.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.afifit.data.BpmData
import com.example.afifit.databinding.FragmentGraphBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class graph : Fragment() {

    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!

    private lateinit var lineChart: LineChart

    private val maxVisibleEntryCount = 30 // Maximum number of visible data points

    private val bpmDataList = mutableListOf<BpmData>()
    private val bloodOxygenDataList = mutableListOf<Entry>()
    private val avgBpmDataList = mutableListOf<Entry>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGraphBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lineChart = binding.lineChart

        // Set up chart properties
        setupChart()

        // Initialize Firebase database reference
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("/bpm")

        // Attach a listener to read the data
        databaseReference.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                bpmDataList.clear()
                bloodOxygenDataList.clear()
                avgBpmDataList.clear()

                // Iterate through each child snapshot and retrieve data
                for (dataSnapshotChild in dataSnapshot.children) {
                    val bpm = dataSnapshotChild.child("bpm").getValue(Float::class.java)
                    val bloodOxygen = dataSnapshotChild.child("bloodOxygen").getValue(Float::class.java)
                    val avgBpm = dataSnapshotChild.child("avgBpm").getValue(Long::class.java)?.toFloat()
                    val timestamp = dataSnapshotChild.child("timestamp").getValue(Long::class.java) ?: 0L

                    if (bpm != null) {
                        bpmDataList.add(BpmData(bpm, timestamp, avgBpm ?: 0.0f))
                    }
                    if (bloodOxygen != null) {
                        bloodOxygenDataList.add(Entry(timestamp.toFloat(), bloodOxygen))
                    }
                    if (avgBpm != null) {
                        avgBpmDataList.add(Entry(timestamp.toFloat(), avgBpm))
                    }
                }

                // Ensure only the last maxVisibleEntryCount entries are kept
                if (bpmDataList.size > maxVisibleEntryCount) {
                    bpmDataList.subList(0, bpmDataList.size - maxVisibleEntryCount).clear()
                }
                if (bloodOxygenDataList.size > maxVisibleEntryCount) {
                    bloodOxygenDataList.subList(0, bloodOxygenDataList.size - maxVisibleEntryCount).clear()
                }
                if (avgBpmDataList.size > maxVisibleEntryCount) {
                    avgBpmDataList.subList(0, avgBpmDataList.size - maxVisibleEntryCount).clear()
                }

                // Log data for debugging
                Log.d("GraphFragment", "BPM Data List: $bpmDataList")
                Log.d("GraphFragment", "Blood Oxygen Data List: $bloodOxygenDataList")
                Log.d("GraphFragment", "Avg BPM Data List: $avgBpmDataList")

                // Update line chart with retrieved health data
                updateLineChart()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
                Log.e("GraphFragment", "Database error: ${databaseError.message}")
            }
        })
    }

    private fun setupChart() {
        lineChart.apply {
            setDrawGridBackground(false)
            description.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            axisLeft.setDrawGridLines(false)
            axisRight.isEnabled = false
            legend.isEnabled = true

            // Format x-axis labels as AM/PM time
            val xAxisFormatter = object : ValueFormatter() {
                private val format = SimpleDateFormat("hh:mm a", Locale.getDefault())

                override fun getFormattedValue(value: Float): String {
                    // Convert timestamp to formatted time
                    return format.format(Date(value.toLong()))
                }
            }
            xAxis.valueFormatter = xAxisFormatter
        }
    }

    private fun updateLineChart() {
        // Prepare entries for BPM
        val entriesBpm = bpmDataList.map { Entry(it.timestamp.toFloat(), it.bpm) }

        // Create LineDataSets for BPM, Blood Oxygen, and AvgBPM
        val dataSetBpm = createLineDataSet(entriesBpm, "BPM", Color.BLUE)
        val dataSetBloodOxygen = createLineDataSet(bloodOxygenDataList, "Blood Oxygen", Color.RED)
        val dataSetAvgBpm = createLineDataSet(avgBpmDataList, "Avg BPM", Color.GREEN)

        // Create LineData with all datasets
        val lineData = LineData(dataSetBpm, dataSetBloodOxygen, dataSetAvgBpm)

        // Set data to chart
        lineChart.data = lineData

        // Scroll to the latest data point
        lineChart.moveViewToX(lineData.entryCount.toFloat() - 1)

        // Notify chart of data changes
        lineChart.notifyDataSetChanged()
        lineChart.invalidate()
    }

    private fun createLineDataSet(entries: List<Entry>, label: String, color: Int): LineDataSet {
        val dataSet = LineDataSet(entries, label)
        dataSet.color = color
        dataSet.setCircleColor(color)
        dataSet.circleRadius = 3f
        dataSet.setDrawCircles(true)
        dataSet.setDrawValues(false)
        dataSet.lineWidth = 2f
        dataSet.setDrawFilled(false)
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER // Smooth, continuous curve
        dataSet.setDrawHorizontalHighlightIndicator(false) // Disable horizontal highlight line
        dataSet.setDrawVerticalHighlightIndicator(false) // Disable vertical highlight line
        return dataSet
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
