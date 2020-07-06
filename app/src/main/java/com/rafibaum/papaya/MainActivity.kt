package com.rafibaum.papaya

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Right now the main activity just contains the player fragment for mocking purposes but eventually
 * will contain the main music library.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}