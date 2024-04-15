package com.example.afifit.layout_handle.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.afifit.R
import com.example.afifit.databinding.FragmentEditProfileBinding
import com.example.afifit.databinding.FragmentLoginMessagingBinding
import com.sendbird.android.SendBird


class login_messaging : Fragment() {

    private var _binding: FragmentLoginMessagingBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentLoginMessagingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        SendBird.init("AB1BD2A2-7578-4C11-BBF6-90A9841D6577", requireContext())
        binding.btnLogin.setOnClickListener {
            connectToSendBird(binding.UserId.text.toString(), binding.NickName.text.toString())
        }
        }

    private fun connectToSendBird(userID: String, nickname: String) {
        SendBird.connect(userID) { user, e ->
            if (e != null) {
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            } else {
                SendBird.updateCurrentUserInfo(nickname, null) { e ->
                    if (e != null) {
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                    }
                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                    transaction.addToBackStack(null)
                    transaction.replace(R.id.FragmentContainerMessaging, ChanneListFrag())
                    transaction.commit()
                }
            }
        }
    }


    }


