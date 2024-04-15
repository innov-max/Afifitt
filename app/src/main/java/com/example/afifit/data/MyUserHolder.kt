package com.example.afifit.data

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.afifit.R
import com.sendbird.android.UserMessage

class MyUserHolder(view: View) : RecyclerView.ViewHolder(view) {
    val messageText = view.findViewById<TextView>(R.id.text_gchat_message_me)
    val date = view.findViewById<TextView>(R.id.text_gchat_date_me)
    val messageDate = view.findViewById<TextView>(R.id.text_gchat_timestamp_me)

    fun bindView(context: Context, message: UserMessage) {
        messageText.text = message.message
        messageDate.text = DateUtil.formatTime(message.createdAt)
        date.visibility = View.VISIBLE
        date.text = DateUtil.formatDate(message.createdAt)
    }
}


