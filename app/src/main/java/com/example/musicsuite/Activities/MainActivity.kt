@file:Suppress("DEPRECATION")

package com.example.musicsuite.Activities

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicsuite.Adapters.NavigationDrawerAdapter
import com.example.musicsuite.Fragments.MainScreenFragment
import com.example.musicsuite.Fragments.SongPlayingFragment
import com.example.musicsuite.R
import kotlin.random.Random

private var mIsInForegroundMode = false

class MainActivity : AppCompatActivity() {

    var navigationDrawerIconList: ArrayList<String> = arrayListOf()
    var images_for_navdrawer = intArrayOf(R.drawable.home, R.drawable.navigation_allsongs, R.drawable.navigation_favorites, R.drawable.navigation_settings, R.drawable.navigation_aboutus)
    lateinit var mBuilder: NotificationCompat.Builder
    val CHANNEL_ID = "Music200"

    object Statified {
        var drawerLayout: DrawerLayout? = null
        var notificationManager: NotificationManager? = null
    }


    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var intentStart = intent.getStringExtra("fromStart")
        if (intentStart == "FromStart") {
            startActivity(Intent(this, ButtonsActivity::class.java))
        }
        setContentView(R.layout.activity_main)


        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        MainActivity.Statified.drawerLayout = findViewById(R.id.drawer_layout)
        navigationDrawerIconList.add("Home")
        navigationDrawerIconList.add("All Songs")
        navigationDrawerIconList.add("Favorites")
        navigationDrawerIconList.add("Settings")
        navigationDrawerIconList.add("About Us")

        val toggle = ActionBarDrawerToggle(this@MainActivity, Statified.drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        MainActivity.Statified.drawerLayout?.addDrawerListener(toggle)
        toggle.syncState()

        val emotionString = intent.getStringExtra("emotion")


        when {
            // if emotionString is happy open happy Fragment as default fragment
            emotionString == "happiness" -> {
//                val happyScreenFragment = HappyFragment()
//                this.supportFragmentManager
//                        .beginTransaction()
//                        .add(R.id.details_fragment, happyScreenFragment, "HappyScreenFragment")
////                        .replace(R.id.details_fragment, happyScreenFragment)
//                        .commit()
            }
            // If emotion is Sad
//            emotionString == "sadness" -> {
//                val sadScreenFragment = SadFragment()
//                this.supportFragmentManager
//                        .beginTransaction()
//                        .add(R.id.details_fragment, sadScreenFragment, "SadScreenFragment")
////                        .replace(R.id.details_fragment, happyScreenFragment)
//                        .commit()
//            }
            else -> {
                val mainScreenFragment = MainScreenFragment()
                this.supportFragmentManager
                        .beginTransaction()
                        .add(R.id.details_fragment, mainScreenFragment, "MainScreenFragment")
//                        .replace(R.id.details_fragment, mainScreenFragment)
                        .commit()
            }
        }

        val ButtonCode = intent.getStringExtra("RandomSong")

        if (ButtonCode == "playRandom") {
            //play random song
            val songLIst = ButtonsActivity.Statified.songList
            Log.e("MainActivity", "SongList : $songLIst")

            val randomPosition = (0..songLIst!!.size).random()
            val songObject = songLIst[randomPosition]
            val args = Bundle()
            val songPlayingFragment = SongPlayingFragment()
            args.putString("songArtist", songObject.artist)
            args.putString("path", songObject.songData)
            args.putString("songTitle", songObject.songTitle)
            args.putInt("songId", songObject.songID.toInt() as Int)
            args.putInt("songPosition", randomPosition)
            args.putParcelableArrayList("songData", songLIst)
            songPlayingFragment.arguments = args

            try {
                if (SongPlayingFragment.Statified.mediaplayer?.isPlaying as Boolean) {
                    SongPlayingFragment.Statified.mediaplayer?.stop()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            (this).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.details_fragment, songPlayingFragment)
                    .addToBackStack("SongPlayingFragment")
                    .commit()
        }

        var _navigationAdapter = NavigationDrawerAdapter(navigationDrawerIconList, images_for_navdrawer, this)
        _navigationAdapter.notifyDataSetChanged()

        var navigation_recycler_view = findViewById<RecyclerView>(R.id.navigation_recycler_view)
        navigation_recycler_view.layoutManager = LinearLayoutManager(this)
        navigation_recycler_view.itemAnimator = DefaultItemAnimator()
        navigation_recycler_view.adapter = _navigationAdapter
        navigation_recycler_view.setHasFixedSize(true)

//       Implement the notification alert of song using pending Intent
//        val intent = Intent(this@MainActivity, MainActivity::class.java)
//        val pIntent = PendingIntent.getActivity(this@MainActivity, System.currentTimeMillis().toInt(),
//                intent, 0)
//        trackNotificationBuilder = Notification.Builder(this)
//                .setContentTitle("A track is playing in background")
//                .setSmallIcon(R.drawable.ic_launcher_foreground)
//                .setContentIntent(pIntent)
//                .setOngoing(true)
//                .setAutoCancel(true)
//                .build()
//
//
//        Statified.notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        Statified.notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID,
                    "MUSICO_Channel",
                    NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "A track is playing in background"
            Statified.notificationManager!!.createNotificationChannel(channel)
        }

        val intent = Intent(this@MainActivity, MainActivity::class.java)
        val pIntent = PendingIntent.getActivity(this@MainActivity, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)

        mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // notification icon
                .setContentTitle("A track is playing in background") // title for notification
                .setAutoCancel(true) // clear notification after click
                .setOngoing(true)
                .setContentIntent(pIntent)

        mBuilder.build()
    }

    // to generate random number
    fun rand(start: Int, end: Int): Int {
        require(start <= end) {}
        return Random(System.nanoTime()).nextInt(start, end + 1)
    }

    override fun onStart() {
        super.onStart()
        try {
            Statified.notificationManager?.cancel(1888)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStop() {
        super.onStop()

        try {
            if (SongPlayingFragment.Statified.mediaplayer?.isPlaying as Boolean) {

                Statified.notificationManager?.notify(1888, mBuilder.build())

            } else {
                Statified.notificationManager?.cancel(1888)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        mIsInForegroundMode = false
    }

    override fun onResume() {
        super.onResume()

        mIsInForegroundMode = true
        try {
            Statified.notificationManager?.cancel(1888)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun isInForeground(): Boolean {
        return mIsInForegroundMode
    }
}