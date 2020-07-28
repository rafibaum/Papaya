package com.rafibaum.papaya

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.rafibaum.papaya.service.MUSIC_STORAGE_PREFERENCES
import com.rafibaum.papaya.service.MUSIC_URI_PREF
import com.rafibaum.papaya.service.PapayaService

private const val DIR_CHOOSER_ID = 30

/**
 * Right now the main activity just contains the player fragment for mocking purposes but eventually
 * will contain the main music library.
 */
class MainActivity : AppCompatActivity() {
    private var mediaBrowser: MediaBrowserCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val preferences =
            applicationContext.getSharedPreferences(MUSIC_STORAGE_PREFERENCES, Context.MODE_PRIVATE)
        val dirUri = preferences.getString(MUSIC_URI_PREF, null)
        if (dirUri == null) {
            val dirChooser = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            dirChooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            dirChooser.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            startActivityForResult(
                Intent.createChooser(dirChooser, "Choose directory"),
                DIR_CHOOSER_ID
            )
        } else {
            initMediaBrowser()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            DIR_CHOOSER_ID -> {
                if (resultCode != Activity.RESULT_OK) {
                    throw IllegalStateException("Result not okay")
                }

                data?.data?.let {
                    applicationContext.contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    val preferences = applicationContext.getSharedPreferences(
                        MUSIC_STORAGE_PREFERENCES,
                        Context.MODE_PRIVATE
                    )
                    with(preferences.edit()) {
                        putString(MUSIC_URI_PREF, it.toString())
                        apply()
                    }

                    initMediaBrowser()
                }
            }
        }
    }

    private fun initMediaBrowser() {
        mediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, PapayaService::class.java),
            connectionCallback,
            null
        )
    }

    private val connectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            mediaBrowser!!.sessionToken.also { token ->
                val mediaController = MediaControllerCompat(this@MainActivity, token)
                MediaControllerCompat.setMediaController(this@MainActivity, mediaController)
            }

            val navController = findNavController(R.id.navHostFragment)
            navController.currentDestination?.let {
                if (it.id == R.id.splashFragment) {
                    navController.navigate(SplashFragmentDirections.showAlbums())
                }
            }
        }

        override fun onConnectionSuspended() {
            // Service crashed, will restart
            Log.w("BrowserConnection", "Unhandled connection suspension")
        }

        override fun onConnectionFailed() {
            // Service refused connection, handle
            Log.e("BrowserConnection", "Unhandled connection failure")
        }
    }

    override fun onStart() {
        super.onStart()
        mediaBrowser?.connect()
    }

    override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onStop() {
        super.onStop()
        mediaBrowser?.disconnect()
    }

    fun getMediaBrowser(): MediaBrowserCompat {
        return mediaBrowser!!
    }
}