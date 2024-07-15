package com.example.afifit.layout_handle.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.db.williamchart.view.DonutChartView
import com.db.williamchart.view.HorizontalBarChartView
import com.example.afifit.R
import com.example.afifit.data.CardsPopularAdapter
import com.example.afifit.data.RandomView
import com.example.afifit.databinding.FragmentExerciseBinding
import com.example.afifit.databinding.FragmentGraphBinding


class exercise : Fragment() {
    private lateinit var donutChartView: DonutChartView
    private var _binding: FragmentExerciseBinding? = null
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
        _binding = FragmentExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val textView4 : TextView = view.findViewById(R.id.textView4)
        val textView5 : TextView = view.findViewById(R.id.textView5)
        val textView6 : TextView = view.findViewById(R.id.caloriesTextView)
        textView4.text = RandomView.donutdatahome.toString()
        textView5.text = RandomView.barchartdatahome.toString()
        textView6.text = RandomView.caloriesdatahome.toString()

        val button = view.findViewById<Button>(R.id.button5)


        val donutChartDatalist = listOf(
            RandomView.normalizeddonutdatahome,
            100f - RandomView.normalizeddonutdatahome
        )

        val donutchart = view.findViewById<DonutChartView>(R.id.chartDonut)
        donutchart.donutColors = intArrayOf(
            Color.parseColor("#9FEC00"),
            Color.parseColor("#D9D9D9")
        )
        donutchart.animation.duration = 1000L
        donutchart.animate(donutChartDatalist)

        val adapter = CardsPopularAdapter(requireContext(), RandomView.cardPopularList)
        view.findViewById<RecyclerView>(R.id.recyclerView).adapter = adapter
}


}