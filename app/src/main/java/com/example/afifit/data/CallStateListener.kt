package com.example.afifit.data

import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log

class CallStateListener : PhoneStateListener() {
    override fun onCallStateChanged(state: Int, phoneNumber: String?) {
        super.onCallStateChanged(state, phoneNumber)
        Log.d("CallStateListener", "Call state changed: $state")
        // Handle call state changes
    }
}

