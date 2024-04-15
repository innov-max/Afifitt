package com.example.afifit.layout_handle.fragments

import ChannelCreateAdapter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.afifit.R
import com.example.afifit.databinding.FragmentCreatingChannelsBinding
import com.sendbird.android.GroupChannel
import com.sendbird.android.GroupChannelParams
import com.sendbird.android.SendBird
import com.sendbird.android.User


class CreatingChannels : Fragment(),ChannelCreateAdapter.OnItemCheckedChangeListener{


    private val EXTRA_CHANNEL_URL = "EXTRA_CHANNEL_URL"


    private lateinit var selectedUsers: ArrayList<String>
    private lateinit var adapter: ChannelCreateAdapter
    private lateinit var recyclerView: RecyclerView


    private var binding: FragmentCreatingChannelsBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentCreatingChannelsBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCreatingChannelsBinding.bind(view)

        selectedUsers = ArrayList()
        adapter = ChannelCreateAdapter(this)
        recyclerView = binding!!.recyclerCreate
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadUsers()

        binding!!.buttonCreate.setOnClickListener { createChannel(selectedUsers)}

    }


    private fun loadUsers() {
        val userListQuery = SendBird.createApplicationUserListQuery()
        userListQuery.next() { list, e ->
            if (e != null) {
                e.message?.let { Log.e("TAG", it) }
            } else {
                adapter.addUsers(list)
            }
        }
    }

    private fun createChannel(users: MutableList<String>) {
        val params = GroupChannelParams()
        val operatorId = ArrayList<String>()
        operatorId.add(SendBird.getCurrentUser().userId)
        params.addUserIds(users)
        params.setOperatorUserIds(operatorId)
        GroupChannel.createChannel(params) { groupChannel, e ->
            if (e != null) {
                e.message?.let { Log.e("TAG", it) }
            } else {
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.addToBackStack(null)
                transaction.replace(R.id.FragmentContainerMessaging, chat())
                transaction.commit()

            }
        }
    }

    override fun onItemChecked(user: User, checked: Boolean) {
        if (checked) {
            selectedUsers.add(user.userId)
        } else {
            selectedUsers.remove(user.userId)
        }
    }


}