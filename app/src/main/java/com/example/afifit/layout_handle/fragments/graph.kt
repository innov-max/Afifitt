package com.example.afifit.layout_handle.fragments

import BloodOxygenData
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.afifit.data.BpmData
import com.example.afifit.databinding.FragmentGraphBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.LineData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class graph : Fragment() {

    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!

    private val bpmDataList = mutableListOf<BpmData>()
    private val bloodOxygenDataList = mutableListOf<BloodOxygenData>()

    private lateinit var lineChart: LineChart

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

        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("/bpm")

        // Attach a listener to read the data
        databaseReference.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                bpmDataList.clear()
                bloodOxygenDataList.clear()

                for (dataSnapshotChild in dataSnapshot.children) {
                    val bpm = dataSnapshotChild.child("bpm").getValue(Float::class.java)
                    val bloodOxygen = dataSnapshotChild.child("bloodOxygen").getValue(Float::class.java)
                    val avgBpm = dataSnapshotChild.child("avgBpm").getValue(Long::class.java) // Retrieve as Long
                    val timestamp = dataSnapshotChild.child("timestamp").getValue(Long::class.java)

                    val bpmData = BpmData(bpm ?: 0.0f, timestamp ?: 0L, avgBpm?.toFloat() ?: 0.0f)
                    val bloodOxygenData = BloodOxygenData(bloodOxygen ?: 0f, timestamp ?: 0L)

                    bpmDataList.add(bpmData)
                    bloodOxygenDataList.add(bloodOxygenData)
                }

                // Update line chart with retrieved health data
                updateLineChart()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })
    }

    private fun updateLineChart() {
        val entriesBpm = mutableListOf<Entry>()
        val entriesBloodOxygen = mutableListOf<Entry>()

        for (data in bpmDataList) {
            entriesBpm.add(Entry(data.timestamp.toFloat(), data.bpm))
        }

        for (data in bloodOxygenDataList) {
            entriesBloodOxygen.add(Entry(data.timestamp.toFloat(), data.bloodOxygen))
        }

        entriesBpm.sortBy { it.x }
        entriesBloodOxygen.sortBy { it.x }

        val dataSetBpm = LineDataSet(entriesBpm, "BPM")
        dataSetBpm.color = Color.BLUE
        dataSetBpm.valueTextColor = Color.BLACK

        val dataSetBloodOxygen = LineDataSet(entriesBloodOxygen, "Blood Oxygen")
        dataSetBloodOxygen.color = Color.RED
        dataSetBloodOxygen.valueTextColor = Color.BLACK

        val lineData = LineData(dataSetBpm, dataSetBloodOxygen)
        lineChart.data = lineData

        // Set any additional styling or configurations for your line chart as needed
        // ...

        lineChart.notifyDataSetChanged() // Notify the chart that the data has changed
        lineChart.invalidate() // Refresh the chart
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
