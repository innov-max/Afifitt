package com.example.afifit.layout_handle.fragments
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.afifit.databinding.FragmentVideoCallBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


import com.zegocloud.uikit.service.defines.ZegoUIKitUser
import com.example.afifit.data.UserProfile

class video_call : Fragment() {

    private var _binding: FragmentVideoCallBinding? = null
    private lateinit var databaseReference: DatabaseReference
    private val binding get() = _binding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVideoCallBinding.inflate(inflater, container, false)

        return binding?.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        databaseReference = FirebaseDatabase.getInstance().reference.child("userProfiles")

        val userId = arguments?.getString("userID")
        binding?.userIdTextView?.text = "Hi $userId!"

        binding?.receiverUserIdTextField?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not implemented
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val _receiverId = binding?.receiverUserIdTextField?.text.toString()
                if (_receiverId.isNotEmpty()) {
                    binding?.buttonsLayout?.visibility = View.VISIBLE
                    startVideoCall(_receiverId)
                    startAudioCall(_receiverId)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Not implemented
            }
        })

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

    private fun startVideoCall(receiverId: String) {
        binding?.videoCallBtn?.setIsVideoCall(true)
        binding?.videoCallBtn?.resourceID = "zego_uikit_call"
        binding?.videoCallBtn?.setInvitees(listOf(ZegoUIKitUser(receiverId)))
    }

    private fun startAudioCall(receiverId: String) {
        binding?.audioCallBtn?.setIsVideoCall(false)
        binding?.audioCallBtn?.resourceID = "zego_uikit_call"
        binding?.audioCallBtn?.setInvitees(listOf(ZegoUIKitUser(receiverId)))
    }
    private fun updateUI(userProfile: UserProfile) {
        // Update other UI elements with data (e.g., name, occupation, about)

        // Load the image using Glide
        activity?.runOnUiThread {
            binding?.let {
                Glide.with(requireContext())
                    .load(userProfile.imageUrl)
                    .into(it.profileImageVideo)
            }
        }

    }


}
