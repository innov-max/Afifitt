package com.example.afifit.layout_handle.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.afifit.Messaging
import com.example.afifit.databinding.FragmentContactBinding
import com.example.afifit.data.CallService

class contact : Fragment() {
    private var _binding: FragmentContactBinding? = null
    private val binding get() = _binding

    private val REQUEST_CALL_PERMISSION = 1
    private val REQUEST_SMS_PERMISSION = 2

    private val phoneNumbers = listOf("0704761843", "0774909759", "1122334455", "5566778899", "6677889900")
    private val message = "Patient in Crisis please hurry to location"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentContactBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.btmCall?.setOnClickListener {
            if (checkPermissions()) {
                startCallService()
                sendSMS()
            }
        }

        binding?.Consult?.setOnClickListener {
            val intent = Intent(requireContext(), Messaging::class.java)
            startActivity(intent)
        }
    }

    private fun checkPermissions(): Boolean {
        val callPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
        val smsPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)

        val permissionsNeeded = mutableListOf<String>()
        if (callPermission != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.CALL_PHONE)
        }
        if (smsPermission != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.SEND_SMS)
        }

        return if (permissionsNeeded.isNotEmpty()) {
            requestPermissions(permissionsNeeded.toTypedArray(), REQUEST_CALL_PERMISSION)
            false
        } else {
            true
        }
    }

    private fun startCallService() {
        val intent = Intent(requireContext(), CallService::class.java)
        intent.putStringArrayListExtra("phoneNumbers", ArrayList(phoneNumbers))
        ContextCompat.startForegroundService(requireContext(), intent)
    }

    private fun sendSMS() {
        val smsManager: SmsManager = SmsManager.getDefault()
        for (phoneNumber in phoneNumbers) {
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CALL_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[permissions.indexOf(Manifest.permission.CALL_PHONE)] == PackageManager.PERMISSION_GRANTED) {
                    startCallService()
                    Toast.makeText(requireContext(), "Panic signal sent", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Call permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_SMS_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[permissions.indexOf(Manifest.permission.SEND_SMS)] == PackageManager.PERMISSION_GRANTED) {
                    sendSMS()
                    Toast.makeText(requireContext(), "Doctor Notified", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "SMS permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
