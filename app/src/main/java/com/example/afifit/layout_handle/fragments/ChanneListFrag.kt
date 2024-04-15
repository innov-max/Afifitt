package com.example.afifit.layout_handle.fragments
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.afifit.R
import com.example.afifit.data.ChannelListAdapter
import com.example.afifit.databinding.FragmentChanneListBinding
import com.sendbird.android.GroupChannel

class ChanneListFrag : Fragment(), ChannelListAdapter.OnChannelClickedListener {
    private var binding: FragmentChanneListBinding? = null

    private val EXTRA_CHANNEL_URL = "EXTRA_CHANNEL_URL"
    lateinit var recyclerView: RecyclerView

    private lateinit var adapter: ChannelListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChanneListBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChanneListBinding.bind(view)


        adapter = ChannelListAdapter(this)

        val recyclerView = binding!!.recyclerGroupChannels
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding!!.fabGroupChannelCreate.setOnClickListener{
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.addToBackStack(null)
            transaction.replace(R.id.FragmentContainerMessaging, CreatingChannels())
            transaction.commit()
        }

        addChannels()
    }

    private fun addChannels() {
        val channelList = GroupChannel.createMyGroupChannelListQuery()
        channelList.limit = 100
        channelList.next { list, e ->
            if (e != null) {
                e.message?.let { Log.e("TAG", it) }
            }
            adapter.addChannels(list)
        }
    }

    override fun onItemClicked(channel: GroupChannel) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()

        val channelFragment = chat()
        val bundle = Bundle().apply {
            putString(EXTRA_CHANNEL_URL, channel.url)
        }
        channelFragment.arguments = bundle
        transaction.addToBackStack(null)
        transaction.replace(R.id.FragmentContainerMessaging, channelFragment)
        transaction.commit()

    }


}