package com.example.musicsuite.Activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.musicsuite.Activities.ButtonsActivity.Statified.songList
import com.example.musicsuite.Fragments.MainScreenFragment
import com.example.musicsuite.Fragments.SongPlayingFragment
import com.example.musicsuite.R
import com.example.musicsuite.Songs
import kotlinx.android.synthetic.main.activity_buttons.*

class ButtonsActivity : AppCompatActivity() {

    var mActivity: Activity? = null
    private var isSongPlaying: Boolean? = null

    object Statified {
        var songList: ArrayList<Songs>? = null
    }
    var backPressedTime:Long = 0
    lateinit var backToast: Toast
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buttons)

        //        songList = getSongsFromPhone()
        this.title = "Home"
        songList = MainScreenFragment.Statified.getSongsList
        Log.e("Inside Button Activity", "onCreate: List:  $songList")
//
        bt_play_random.setOnClickListener {
            playRandomSong()
        }
    }

    //    For Emotion Detection
    fun openEmotionActivity(view: View) {
        isSongPlaying = SongPlayingFragment.Statified.mediaplayer?.isPlaying

        if (isSongPlaying == true) {
            //Stop the song and remove the notification
            MainActivity.Statified.notificationManager?.cancel(1888)

            SongPlayingFragment.Statified.mediaplayer?.stop()
            SongPlayingFragment.Statified.playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)

        }

        val intent = Intent(this, DemoActivity::class.java)
        startActivity(intent)

    }


    //    Open All songs list
    fun openSongList(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun playRandomSong() {

        var MainIntent = Intent(this, MainActivity::class.java)
        MainIntent.putExtra("RandomSong", "playRandom")
        Toast.makeText(this, "Starting Random Song", Toast.LENGTH_SHORT).show()
        startActivity(MainIntent)
    }

// To get the songs from phone
    fun getSongsFromPhone(): ArrayList<Songs> {
        var arrayList = ArrayList<Songs>()
        var contentResolver = mActivity?.contentResolver
        var songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var songCursor = contentResolver?.query(songUri, null, null, null, null)
        if (songCursor != null && songCursor.moveToFirst()) {
            val songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val dateIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            while (songCursor.moveToNext()) {
                var currentId = songCursor.getLong(songId)
                var currentTile = songCursor.getString(songTitle)
                var currentArtist = songCursor.getString(songArtist)
                var currentData = songCursor.getString(songData)
                var currentDate = songCursor.getLong(dateIndex)

                arrayList.add(Songs(currentId, currentTile, currentArtist, currentData, currentDate))
            }
        }
        return arrayList
}

    // On backpressed to exit the app
    override fun onBackPressed() {
        if(backPressedTime + 2000 > System.currentTimeMillis()){
            backToast.cancel()
            super.onBackPressed()
            finishAffinity()
        }
        else{
            backToast =  Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT)
            backToast.show()
        }

        backPressedTime = System.currentTimeMillis()
    }
}