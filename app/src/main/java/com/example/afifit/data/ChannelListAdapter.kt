package com.example.afifit.data
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.afifit.R
import com.example.afifit.layout_handle.fragments.ChanneListFrag
import com.sendbird.android.AdminMessage
import com.sendbird.android.FileMessage
import com.sendbird.android.GroupChannel
import com.sendbird.android.UserMessage

class ChannelListAdapter(private val listener: ChanneListFrag) :
    RecyclerView.Adapter<ChannelListAdapter.ChannelHolder>() {

    interface OnChannelClickedListener {
        fun onItemClicked(channel: GroupChannel)
    }

    private var channels: MutableList<GroupChannel> = ArrayList()

    fun addChannels(newChannels: List<GroupChannel>) {
        val startPosition = channels.size
        channels.addAll(newChannels)
        notifyItemRangeInserted(startPosition, newChannels.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ChannelHolder(layoutInflater.inflate(R.layout.item_chanel_chooser, parent, false))
    }

    override fun getItemCount() = channels.size

    override fun onBindViewHolder(holder: ChannelHolder, position: Int) {
        holder.bindViews(channels[position], listener)
    }

    class ChannelHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val channelName = v.findViewById<TextView>(R.id.text_channel_name)
        private val channelRecentMessage = v.findViewById<TextView>(R.id.text_channel_recent)
        private val channelMemberCount = v.findViewById<TextView>(R.id.text_channel_member_count)

        fun bindViews(groupChannel: GroupChannel, listener: OnChannelClickedListener) {
            val lastMessage = groupChannel.lastMessage
            if (lastMessage != null) {
                when (lastMessage) {
                    is UserMessage, is AdminMessage -> channelRecentMessage.text = lastMessage.message
                    is FileMessage -> {
                        val sender = lastMessage.sender.nickname
                        channelRecentMessage.text = sender
                    }
                }
            }

            itemView.setOnClickListener {
                listener.onItemClicked(groupChannel)
            }
            channelName.text = groupChannel.members[0].nickname
            channelMemberCount.text = groupChannel.memberCount.toString()
        }
    }
}
