package com.example.afifit.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.afifit.R
import com.example.afifit.databinding.FragmentSettingsBinding
import com.example.afifit.databinding.FragmentSignUpLoginBinding
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
        database =
            FirebaseDatabase.getInstance()

        binding?.signUp?.setOnClickListener {
            binding!!.signUp.background = resources.getDrawable(R.drawable.switch_tricks, null)
            binding!!.signUp.setTextColor(resources.getColor(R.color.textColor, null))
            binding!!.login.background = null
            binding!!.SignUpLayout.visibility = View.VISIBLE
            binding!!.loginLayout.visibility = View.GONE
            binding!!.login.setTextColor(resources.getColor(R.color.cyan, null))
        }

        //SIGN UP BUTTON FUNCTIONALITY
        binding?.btnsignUp?.setOnClickListener {

            binding!!.SignUpProgressBar.visibility = View.GONE

            if (binding!!.UserName.text?.trim().toString()
                    .isNotEmpty() || binding!!.emailSignUp.text?.trim().toString()
                    .isNotEmpty() || binding!!.emailSignUp.text?.trim().toString()
                    .isNotEmpty() || binding!!.PasswordSignUp.text?.trim().toString()
                    .isNotEmpty() || binding!!.Passwordconfirm.text?.trim()
                    .toString().isNotEmpty()
            ) {

                if (binding!!.PasswordSignUp.text?.trim()
                        .toString() != binding!!.Passwordconfirm.text?.trim()
                        .toString()
                ) {
                    Toast.makeText(
                        requireContext(),
                        "The passwords are not matching try again",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {

                    auth.createUserWithEmailAndPassword(
                        binding!!.emailSignUp.text?.trim().toString(),
                        binding!!.emailSignUp.text?.trim().toString()
                    )
                        .addOnCompleteListener(requireActivity()) { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    requireContext(),
                                    "Registration Successfull WELCOME",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.e("Task Message", "Successful")
                               // val intent = Intent(this, send_otp::class.java)
                              //  startActivity(intent)
                            } else {
                                Log.e("Task Message", "Unsuccessful")
                                Toast.makeText(
                                    requireContext(),
                                    "Something went wrong try again, make sure your connected to the internet",
                                    Toast.LENGTH_SHORT
                                ).show()

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
            if (binding!!.idemail.text?.trim().toString().isNotEmpty()||binding!!.idPassword.text?.trim().toString().isNotEmpty()) {
                if (auth.currentUser == null) {
                    Toast.makeText(
                        requireContext(),
                        "You are not registered, kindly register",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (auth.currentUser != null) {
                    Toast.makeText(requireContext(), "Login Successfull", Toast.LENGTH_SHORT)
                        .show()
                    //val intent = Intent(this, landingActivity::class.java)
                  //  startActivity(intent)
                    Log.e("login Message", "login successful")
                }
            }else{
                Toast.makeText(requireContext(),"Fill all the fields",Toast.LENGTH_SHORT).show()
            }
        }
        createRequest()
        binding?.btnGoogle?.setOnClickListener {

            signIn();
        }
    }

    override fun onStart() {
        super.onStart()

        // Check if user is signed in (non-null) and update UI accordingly.

        val user = auth.currentUser
        if (user != null) {
            Log.e("Task Message", "Please Sign Up to continue")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val exception=task.exception

            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)
            }
            catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(requireContext(), "Login Failed", Toast.LENGTH_SHORT)
                    .show()
            }

        }
    }


    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val fragment2 = splash2()
                    val transaction = requireActivity().supportFragmentManager.beginTransaction()

                    transaction.addToBackStack(null)
                    transaction.replace(R.id.FragmentContainer, fragment2)
                    transaction.commit()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(requireContext(), "Login Failed: ", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun createRequest() {
        // Configure Google Sign In
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
    }
    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
}