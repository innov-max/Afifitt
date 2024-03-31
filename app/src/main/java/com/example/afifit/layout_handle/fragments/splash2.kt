package com.example.afifit

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.afifit.dash
import com.example.afifit.databinding.FragmentSplash2Binding


class splash2 : Fragment() {
    private val hideHandler = Handler(Looper.myLooper()!!)
    private val textToType = "Lorem ipsum dolor sit amet, consectetur adipiscing elit"
    private val textToType1 = "Lorem ipsum dolor sit amet, consectetur adipiscing elit"
    private val textToType2 = "Lorem ipsum dolor sit amet, consectetur adipiscing elit"
    private val textToType3 = "Lorem ipsum dolor sit amet, consectetur adipiscing elit"

    @Suppress("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        val flags =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        activity?.window?.decorView?.systemUiVisibility = flags
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        fullscreenContentControls?.visibility = View.VISIBLE
    }
    private var visible: Boolean = false
    private val hideRunnable = Runnable { hide() }

    private var fullscreenContent: View? = null
    private var fullscreenContentControls: View? = null

    private var _binding: FragmentSplash2Binding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSplash2Binding.inflate(inflater, container, false)
        return binding?.root





    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        visible = true

        // Set up the user interaction to manually show or hide the system UI.
        fullscreenContent?.setOnClickListener { toggle()

        }
        startTypewriterAnimation()

        binding?.btnContinue1?.setOnClickListener {
            val intent = Intent(activity, dash::class.java)
            startActivity(intent)

        }

    }


    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    override fun onPause() {
        super.onPause()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Clear the systemUiVisibility flag
        activity?.window?.decorView?.systemUiVisibility = 0
        // show()
    }

    override fun onDestroy() {
        super.onDestroy()
        fullscreenContent = null
        fullscreenContentControls = null
    }

    private fun toggle() {
        if (visible) {
            hide()
        } else {
            //show()
        }
    }

    private fun startTypewriterAnimation() {
        val handler = Handler(Looper.myLooper()!!)

        var charIndex = 0

        val runnable = object : Runnable {
            override fun run() {
                if (charIndex <= textToType.length) {
                    val displayedText = textToType.substring(0, charIndex++)
                    val displayedText1 = textToType1.substring(0, charIndex++)
                    val displayedText2 = textToType2.substring(0, charIndex++)
                    val displayedText3 = textToType3.substring(0, charIndex++)

                    binding?.introText?.text = displayedText
                    binding?.introText2?.text = displayedText1
                    binding?.introText3?.text = displayedText2
                    binding?.introText4?.text = displayedText3


                    handler.postDelayed(this, 50) // Adjust the delay as needed
                }
            }
        }

        handler.post(runnable)
    }

    private fun hide() {
        // Hide UI first
        fullscreenContentControls?.visibility = View.GONE
        visible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }


    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    companion object {

        private const val UI_ANIMATION_DELAY = 300
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}