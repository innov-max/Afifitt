package com.example.afifit.layout_handle.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.afifit.R
import com.example.afifit.data.MessageAdapter
import com.example.afifit.databinding.FragmentChatBinding
import com.sendbird.android.*
import com.sendbird.android.BaseChannel.SendUserMessageHandler
import com.sendbird.android.GroupChannel.GroupChannelGetHandler
import com.sendbird.android.SendBird.ChannelHandler

class chat : Fragment() {

    private val EXTRA_CHANNEL_URL = "EXTRA_CHANNEL_URL"
    private val CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_CHAT"

    private lateinit var adapter: MessageAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var groupChannel: GroupChannel
    private  var channelUrl: String? = null

    private var binding: FragmentChatBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChatBinding.bind(view)

        setUpRecyclerView()
        setButtonListeners()
    }

    override fun onResume() {
        super.onResume()
        channelUrl = getChannelURl()

        GroupChannel.getChannel(channelUrl,
            GroupChannelGetHandler { groupChannel, e ->
                if (e != null) {
                    // Error!
                    e.printStackTrace()
                    return@GroupChannelGetHandler
                }
                this.groupChannel = groupChannel
                getMessages()
            })

        SendBird.addChannelHandler(
            CHANNEL_HANDLER_ID,
            object : ChannelHandler() {
                override fun onMessageReceived(
                    baseChannel: BaseChannel,
                    baseMessage: BaseMessage
                ) {
                    if (baseChannel.url == channelUrl) {
                        // Add new message to view
                        adapter.addFirst(baseMessage)
                        groupChannel.markAsRead()
                    }
                }
            })
    }

    override fun onPause() {
        super.onPause()
        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID)
    }

    private fun setButtonListeners() {
        binding?.buttonGchatBack?.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.addToBackStack(null)
            transaction.replace(R.id.FragmentContainerMessaging, ChanneListFrag())
            transaction.commit()
        }

        binding?.btnSend?.setOnClickListener {
            sendMessage()
        }
    }

    private fun sendMessage() {
            val params = UserMessageParams()
                .setMessage(binding?.editGchatMessage?.text.toString())
            groupChannel.sendUserMessage(params, SendUserMessageHandler { userMessage, e ->
                if (e != null) {
                    // Handle the error.
                    return@SendUserMessageHandler
                }
                adapter.addFirst(userMessage)
                binding?.editGchatMessage?.text?.clear()
            })
    }

    private fun getMessages() {
        val previousMessageListQuery = groupChannel.createPreviousMessageListQuery()

        previousMessageListQuery.load(
            100,
            true
        ) { messages, e ->
            if (e != null) {
                e.message?.let { Log.e("Error", it) }
            }
            adapter.loadMessages(messages!!)
        }
    }

    private fun setUpRecyclerView() {
        adapter = MessageAdapter(requireContext())
        recyclerView = binding?.recyclerGchat!!
        recyclerView.adapter = adapter

        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.reverseLayout = true
        recyclerView.layoutManager = layoutManager
        recyclerView.scrollToPosition(0)
    }

    private fun getChannelURl(): String? {
        return arguments?.getString(EXTRA_CHANNEL_URL)
    }
}
