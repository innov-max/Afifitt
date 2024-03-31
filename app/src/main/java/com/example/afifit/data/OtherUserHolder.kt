package com.example.afifit.data

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.afifit.R
import com.sendbird.android.UserMessage

class OtherUserHolder(view: View) : RecyclerView.ViewHolder(view) {
    val messageText = view.findViewById<TextView>(R.id.text_gchat_message_other)
    val date = view.findViewById<TextView>(R.id.text_gchat_date_other)
    val timestamp = view.findViewById<TextView>(R.id.text_gchat_timestamp_other)
    val profileImage = view.findViewById<ImageView>(R.id.image_gchat_profile_other)
    val user = view.findViewById<TextView>(R.id.text_gchat_user_other)
    fun bindView(context: Context, message: UserMessage) {
        messageText.setText(message.message)
        timestamp.text = DateUtil.formatTime(message.createdAt)
        date.visibility = View.VISIBLE
        date.text = DateUtil.formatDate(message.createdAt)
        Glide.with(context).load(message.sender.profileUrl).apply(RequestOptions().override(75, 75))
            .into(profileImage)
        user.text = message.sender.nickname
    }
}

