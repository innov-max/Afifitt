package com.example.afifit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.afifit.layout_handle.fragments.splash1

class splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        supportFragmentManager.beginTransaction()

            .replace(R.id.FragmentContainer, splash1())
            .commit()
    }
}