package com.example.afifit.authentication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.afifit.R
import com.example.afifit.databinding.FragmentSettingsBinding
import com.example.afifit.databinding.FragmentSignUpLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class SignUp_Login : Fragment() {

    private var binding: FragmentSignUpLoginBinding? = null
    private lateinit var auth : FirebaseAuth
    private lateinit var database: FirebaseDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpLoginBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        database = FirebaseDatabase.getInstance("https://console.firebase.google.com/project/e-m-a-28d80/database/e-m-a-28d80-default-rtdb/data/~2F")

        binding?.signUp?.setOnClickListener {
            binding!!.signUp.background = resources.getDrawable(R.drawable.switch_tricks,null)
            binding!!.signUp.setTextColor(resources.getColor(R.color.textColor,null))
            binding!!.login.background = null
            binding!!.SignUpLayout.visibility = View.VISIBLE
            binding!!.loginLayout.visibility = View.GONE
            binding!!.login.setTextColor(resources.getColor(R.color.cyan,null))
        }



    }


}