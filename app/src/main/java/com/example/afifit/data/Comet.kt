package com.example.afifit.data

import android.app.Application
import android.util.Log
import com.cometchat.chat.core.AppSettings
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException

class Comet : Application() {
    val appID: String = "253853044dafc1c7"
    val region: String = "in" // Replace with your App Region ("eu" or "us")

    val TAG: String = "App "
    val appSettings = AppSettings.AppSettingsBuilder().subscribePresenceForAllUsers().setRegion(region).build()

    override fun onCreate() {
        super.onCreate()
        CometChat.init(this, appID, appSettings, object : CometChat.CallbackListener<String>() {
            override fun onSuccess(p0: String?) {
                Log.d(TAG, "Initialization completed successfully")
            }

            override fun onError(p0: CometChatException?) {
                Log.d(TAG, "Initialization failed with exception: " + p0?.message)
            }
        })
    }


}
