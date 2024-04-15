package com.example.afifit.layout_handle.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.afifit.R
import com.example.afifit.databinding.FragmentVoiceCallBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.zegocloud.uikit.prebuilt.call.config.ZegoNotificationConfig
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService

import com.example.afifit.data.UserProfile

class voice_call : Fragment() {

    private lateinit var databaseReference: DatabaseReference
    private var _binding: FragmentVoiceCallBinding? = null
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVoiceCallBinding.inflate(inflater, container, false)
        return binding?.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        databaseReference = FirebaseDatabase.getInstance().reference.child("userProfiles")


        // userIdTextField = view.findViewById(R.id.user_id_text_field)
        binding?.buttonNext?.setOnClickListener {
            val userId = binding!!.userIdTextField.text.toString()
            if (userId.isNotEmpty()) {
                val videoCallFragment = video_call()
                val bundle = Bundle()
                bundle.putString("userID", userId)
                videoCallFragment.arguments = bundle

                val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.myFragmentContainerCall, videoCallFragment)
                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()
                voiceCallServices(userId)
            }

            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    //userProfile
                    val userProfile = dataSnapshot.children.firstOrNull()?.getValue(UserProfile::class.java)
                    userProfile?.let { updateUI(it) }

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors
                }
            })
        }

    }

    private fun voiceCallServices(userID: String) {
        val appID: Long = 1571827900  // your App ID of Zoge Cloud Project
        val appSign = "05683870c6cf117a35f7b6b906336727de1fe75279fb7ba3b4399d355c99e464" // your App Sign of Zoge Cloud Project
        val application = requireActivity().application // Android's application context
        val callInvitationConfig = ZegoUIKitPrebuiltCallInvitationConfig()
        val notificationConfig = ZegoNotificationConfig()
        notificationConfig.sound = "zego_uikit_sound_call"
        notificationConfig.channelID = "CallInvitation"
        notificationConfig.channelName = "CallInvitation"
        ZegoUIKitPrebuiltCallInvitationService.init(
            application,
            appID,
            appSign,
            userID,
            userID,
            callInvitationConfig
        )
    }

    private fun updateUI(userProfile: UserProfile) {
        // Update other UI elements with data (e.g., name, occupation, about)
        binding?.profileCallImage?.invalidate()
        // Load the image using Glide
        activity?.runOnUiThread {
            binding?.let {
                Glide.with(requireContext())
                    .load(userProfile.imageUrl)
                    .into(it.profileCallImage)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ZegoUIKitPrebuiltCallInvitationService.unInit()
    }
}
