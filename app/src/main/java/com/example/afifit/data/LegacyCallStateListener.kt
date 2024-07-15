package com.example.afifit.data

import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log

class LegacyCallStateListener : PhoneStateListener() {
    override fun onCallStateChanged(state: Int, phoneNumber: String?) {
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                // Phone is ringing
                Log.d("CallStateListener", "Phone is ringing")
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                // Call is active
                Log.d("CallStateListener", "Call is active")
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                // Call ended or idle
                Log.d("CallStateListener", "Call ended or idle")
            }
        }
    }
}
