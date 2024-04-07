package com.example.afifit.layout_handle.fragments

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import androidx.core.graphics.drawable.DrawableCompat.applyTheme
import com.example.afifit.R
import com.example.afifit.databinding.FragmentComputerVisionBinding
import com.example.afifit.databinding.FragmentSettingsBinding
import io.grpc.Context

class Settings : Fragment() {

    private lateinit var switchTheme: Switch
    private lateinit var buttonSave: Button

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private var binding: FragmentSettingsBinding? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        }


    }

    private fun saveSettings() {

    }

