package com.example.afifit.authentication

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.afifit.R
import com.example.afifit.databinding.FragmentSignUpLoginBinding
import com.example.afifit.layout_handle.fragments.ComputerVision
import com.example.afifit.splash2
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class SignUp_Login : Fragment() {

    private var binding: FragmentSignUpLoginBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var gso: GoogleSignInOptions
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    val RC_SIGN_IN: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
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
        database = FirebaseDatabase.getInstance()

        binding?.signUp?.setOnClickListener {
            binding!!.signUp.background = resources.getDrawable(R.drawable.switch_tricks, null)
            binding!!.signUp.setTextColor(resources.getColor(R.color.textColor, null))
            binding!!.login.background = null
            binding!!.SignUpLayout.visibility = View.VISIBLE
            binding!!.loginLayout.visibility = View.GONE
            binding!!.login.setTextColor(resources.getColor(R.color.cyan, null))
        }

        binding?.btnsignUp?.setOnClickListener {
            binding!!.SignUpProgressBar.visibility = View.GONE
            val userName = binding!!.UserName.text?.trim().toString()
            val email = binding!!.emailSignUp.text?.trim().toString()
            val password = binding!!.PasswordSignUp.text?.trim().toString()
            val confirmPassword = binding!!.Passwordconfirm.text?.trim().toString()

            if (userName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password != confirmPassword) {
                    Toast.makeText(requireContext(), "The passwords are not matching, try again", Toast.LENGTH_SHORT).show()
                } else {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(requireActivity()) { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(requireContext(), "Registration Successful, WELCOME", Toast.LENGTH_SHORT).show()
                                Log.e("Task Message", "Successful")
                                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                                transaction.addToBackStack(null)
                                transaction.replace(R.id.FragmentContainer, phone_otp())
                                transaction.commit()
                                sendEmailAsync(email)
                            } else {
                                Log.e("Task Message", "Unsuccessful")
                                Toast.makeText(requireContext(), "Something went wrong, try again. Ensure you're connected to the internet.", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            } else {
                Toast.makeText(requireContext(), "Input Required", Toast.LENGTH_LONG).show()
            }
        }

        binding?.login?.setOnClickListener {
            binding!!.signUp.background = null
            binding!!.signUp.setTextColor(resources.getColor(R.color.cyan, null))
            binding!!.login.background = resources.getDrawable(R.drawable.switch_tricks, null)
            binding!!.SignUpLayout.visibility = View.GONE
            binding!!.loginLayout.visibility = View.VISIBLE
            binding!!.login.setTextColor(resources.getColor(R.color.textColor, null))
        }

        binding?.btnLogin?.setOnClickListener {
            val email = binding!!.idemail.text?.trim().toString()
            val password = binding!!.idPassword.text?.trim().toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                if (auth.currentUser == null) {
                    Toast.makeText(requireContext(), "You are not registered, kindly register", Toast.LENGTH_SHORT).show()
                } else {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(requireActivity()) { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT).show()
                                Log.e("login Message", "Login successful")
                                // Navigate to dash or another fragment as needed
                                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                                transaction.addToBackStack(null)
                                transaction.replace(R.id.FragmentContainer, phone_otp())
                                transaction.commit()
                            } else {
                                Toast.makeText(requireContext(), "Login Failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            } else {
                Toast.makeText(requireContext(), "Fill all the fields", Toast.LENGTH_SHORT).show()
            }
        }

        createRequest()
        binding?.btnGoogle?.setOnClickListener {
            signIn()
        }
    }

    override fun onStart() {
        super.onStart()
        val user = auth.currentUser
        if (user != null) {
            Log.e("Task Message", "User already signed in.")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Toast.makeText(requireContext(), "Login Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val fragment2 = phone_otp()
                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                    transaction.addToBackStack(null)
                    transaction.replace(R.id.FragmentContainer, fragment2)
                    transaction.commit()
                } else {
                    Toast.makeText(requireContext(), "Login Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun createRequest() {
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun sendEmailAsync(email: String) {
        val subject = "Welcome to Afifit"
        val body = "Thank you for signing up! We're excited to have you on board."
        SendEmailTask().execute(email, subject, body)
    }

    private class SendEmailTask : AsyncTask<String, Void, Void>() {
        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: String): Void? {
            val recipientEmail = params[0]
            val subject = params[1]
            val body = params[2]
            MailUtils.sendEmail(recipientEmail, subject, body)
            return null
        }
    }
}
