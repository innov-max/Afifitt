package com.example.afifit.layout_handle.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.afifit.Messaging
import com.example.afifit.calls
import com.example.afifit.databinding.FragmentContactBinding


class contact : Fragment() {
    private var _binding: FragmentContactBinding? = null
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentContactBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
// Tostart here tomorrow
        binding?.btmCall?.setOnClickListener {
            val intent = Intent(requireContext(), calls::class.java)
            startActivity(intent)
        }

        binding?.Consult?.setOnClickListener {

            val intent = Intent(requireContext(), Messaging::class.java)
            startActivity(intent)
        }


    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            contact().apply {
                arguments = Bundle().apply {

                }
            }
    }
}