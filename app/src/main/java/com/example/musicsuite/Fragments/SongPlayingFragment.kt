package com.example.musicsuite.Fragments

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.example.musicsuite.CurrentSongHelper
import com.example.musicsuite.R
import com.example.musicsuite.Songs
import com.example.musicsuite.databases.EchoDatabase
import kotlinx.android.synthetic.main.fragment_song_playing.*
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * A simple [Fragment] subclass.
 * Use the [SongPlayingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SongPlayingFragment : Fragment() {

    object Statified {
        var myActivity: Activity? = null
        var mediaplayer: MediaPlayer? = null
        var startTimeText: TextView? = null
        var endTimeText: TextView? = null
        var playpauseImageButton: ImageButton? = null
        var previousImageButton: ImageButton? = null
        var nextImageButton: ImageButton? = null
        var loopImageButton: ImageButton? = null
        var seekbar: SeekBar? = null
        var songArtistView: TextView? = null
        var songTitleView: TextView? = null
        var shuffleImageButton: ImageButton? = null
        var currentPosition: Int = 0
        var fetchSongs: ArrayList<Songs>? = null
        var currentSongHelper: CurrentSongHelper? = null
        var audioVisualization: AudioVisualization? = null
        var glView: GLAudioVisualizationView? = null
        var fab: ImageButton? = null
        var favotiteContent: EchoDatabase? = null
        var mSensorManager: SensorManager? = null
        var mSensorListener: SensorEventListener? = null
        var MY_PREFS_NAME = "ShakeFeature"

        var updateSongTime = object : Runnable {
            override fun run() {
                val getcurrent = mediaplayer?.currentPosition
                startTimeText?.setText(String.format("%02d:%02d",

                        TimeUnit.MILLISECONDS.toMinutes(getcurrent?.toLong() as Long),
                        TimeUnit.MILLISECONDS.toSeconds(getcurrent?.toLong() as Long) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getcurrent?.toLong() as Long))))

                seekbar?.setProgress(getcurrent?.toInt() as Int)
                Handler().postDelayed(this, 1000)
            }

        }
    }


    object Staticated {
        var MY_PREFS_SHUFFLE = "Shuffle feature"
        var My_PREFS_LOOP = "Loop feature"

        fun onSongComplete() {
            if (Statified.currentSongHelper?.isShuffle as Boolean) {
                playNext("PlayNextLikeNormalShuffle")
                Statified.currentSongHelper?.isPlaying
            } else {
                if (Statified.currentSongHelper?.isLoop as Boolean) {
                    Statified.currentSongHelper?.isPlaying = true
                    var nextSong = Statified.fetchSongs?.get(Statified.currentPosition)
                    Statified.currentSongHelper?.currentPosition = Statified.currentPosition
                    Statified.currentSongHelper?.songTitle = nextSong?.songTitle
                    Statified.currentSongHelper?.songArtist = nextSong?.artist
                    Statified.currentSongHelper?.songPath = nextSong?.songData
                    Statified.currentSongHelper?.songId = nextSong?.songID as Long

                    updateTextViews(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)

                    Statified.mediaplayer?.reset()
                    try {
                        Statified.mediaplayer?.setDataSource(Statified.myActivity as Context, Uri.parse(Statified.currentSongHelper?.songPath))
                        Statified.mediaplayer?.prepare()
                        Statified.mediaplayer?.start()
                        processInformation(Statified.mediaplayer as MediaPlayer)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                } else {
                    playNext("PlayNextNormal")
                    Statified.currentSongHelper?.isPlaying
                }
            }
            if (Statified.favotiteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity!!, R.drawable.favorite_on))
            } else {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity!!, R.drawable.favorite_off))
            }
        }

        fun updateTextViews(songtitle: String, songArtist: String) {
            var songTitleUpdated = songtitle
            var songArtistUpdated = songArtist
            if (songtitle.equals("<unknown>", true)) {
                songTitleUpdated = "unknown"
            }
            if (songArtist.equals("<unknown>", true)) {
                songArtistUpdated = "unknown"
            }
            Statified.songTitleView?.setText(songTitleUpdated)
            Statified.songArtistView?.setText(songArtistUpdated)
        }

        fun processInformation(mediaPlayer: MediaPlayer) {
            val finalTime = mediaPlayer.duration
            val startTime = mediaPlayer.currentPosition
            Statified.seekbar?.max = finalTime
            Statified.startTimeText?.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(startTime.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(startTime.toLong()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime.toLong())))
            )
            Statified.endTimeText?.setText(String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong())))
            )
            Statified.seekbar?.setProgress(startTime)
            Handler().postDelayed(Statified.updateSongTime, 1000)
        }

        fun playNext(check: String) {
            if (check.equals("PlayNextNormal", true)) {
                Statified.currentPosition = Statified.currentPosition + 1
            } else if (check.equals("PlayNextLikeNormalShuffle", true)) {
                var randomObject = Random()
                var randomPosition = randomObject.nextInt(Statified.fetchSongs?.size?.plus(1) as Int)
                Statified.currentPosition = randomPosition

            }
            if (Statified.currentPosition == Statified.fetchSongs?.size) {
                Statified.currentPosition = 0
            }
            Statified.currentSongHelper?.isLoop = false
            var nextSong = Statified.fetchSongs?.get(Statified.currentPosition)
            Statified.currentSongHelper?.songTitle = nextSong?.songTitle
            Statified.currentSongHelper?.songArtist = nextSong?.artist
            Statified.currentSongHelper?.songPath = nextSong?.songData
            Statified.currentSongHelper?.currentPosition = Statified.currentPosition
            Statified.currentSongHelper?.songId = nextSong?.songID as Long

            updateTextViews(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)

            Statified.mediaplayer?.reset()
            try {
                Statified.mediaplayer?.setDataSource(Statified.myActivity, Uri.parse(Statified.currentSongHelper?.songPath))
                Statified.mediaplayer?.prepare()
                Statified.mediaplayer?.start()
                processInformation(Statified.mediaplayer as MediaPlayer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (Statified.favotiteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity!!, R.drawable.favorite_on))
            } else {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity!!, R.drawable.favorite_off))
            }
        }
    }

    var mAcceleration: Float = 0f
    var mAccelerationCurrent: Float = 0f
    var mAccelerationLast: Float = 0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var view = inflater!!.inflate(R.layout.fragment_song_playing, container, false)
        setHasOptionsMenu(true)
        activity?.title = "Now Playing"
        Statified.seekbar = view?.findViewById(R.id.seekBar)
        Statified.startTimeText = view?.findViewById(R.id.startTime)
        Statified.endTimeText = view?.findViewById(R.id.endTime)
        Statified.playpauseImageButton = view?.findViewById(R.id.playPauseButton)
        Statified.nextImageButton = view?.findViewById(R.id.nextButton)
        Statified.previousImageButton = view?.findViewById(R.id.previousButton)
        Statified.loopImageButton = view?.findViewById(R.id.loopButton)
        Statified.shuffleImageButton = view?.findViewById(R.id.shuffleButton)
        Statified.songArtistView = view?.findViewById(R.id.songArtist)
        Statified.songTitleView = view?.findViewById(R.id.songTitle)
        Statified.glView = view?.findViewById(R.id.visualizer_view)
        Statified.fab = view?.findViewById(R.id.favoriteIcon)
        Statified.fab?.alpha = 0.8f

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Statified.audioVisualization = Statified.glView as AudioVisualization
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Statified.myActivity = context as Activity
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        Statified.myActivity = activity
    }

    override fun onResume() {
        super.onResume()
        /*Here we register the sensor*/
        Statified.audioVisualization?.onResume()
        Statified.mSensorManager?.registerListener(Statified.mSensorListener,
                Statified.mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL)

    }

    override fun onPause() {
        Statified.audioVisualization?.onPause()
        super.onPause()
        /*When fragment is paused, we remove the sensor to prevent the battery drain*/
        Statified.mSensorManager?.unregisterListener(Statified.mSensorListener)
    }

    override fun onDestroyView() {
        Statified.audioVisualization?.release()
        super.onDestroyView()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*Sensor service is activate when the fragment is created*/
        Statified.mSensorManager = Statified.myActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAcceleration = 0.0f
        mAccelerationCurrent = SensorManager.GRAVITY_EARTH
        mAccelerationLast = SensorManager.GRAVITY_EARTH
        bindShakeListener()
    }

    /*This function is used to create the menu
    * Mainly we create the menu file n XML and inflate it here in this function*/
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu?.clear()
        inflater?.inflate(R.menu.song_playing_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    /*Here we handle the click event of the menu item*/
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            /*If the id of the item click is action_redirect
        * we navigate back to the list*/
            R.id.action_redirect -> {
                Statified.myActivity?.onBackPressed()
                return false
            }
        }
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val item: MenuItem? = menu?.findItem(R.id.action_redirect)
        item?.isVisible = true
        val item2: MenuItem? = menu?.findItem(R.id.action_sort)
        item2?.isVisible = false
    }

    fun bindShakeListener() {
        Statified.mSensorListener = object : SensorEventListener {
            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

            }

            override fun onSensorChanged(p0: SensorEvent) {
                /*We need this onSensorChanged function
              * This function is called when there is a new sensor event*/
                /*The sensor event has 3 dimensions i.e. the x, y and z in which the changes can occur*/
                val x = p0.values[0]
                val y = p0.values[1]
                val z = p0.values[2]
                /*Now lets see how we calculate the changes in the acceleration*/
                /*Now we shook the phone so the current acceleration will be the first to start with*/
                mAccelerationLast = mAccelerationCurrent
                /*Since we could have moved the phone in any direction, we calculate the Euclidean distance to get the normalized distance*/
                mAccelerationCurrent = Math.sqrt(((x * x + y * y + z * z).toDouble())).toFloat()
                /*Delta gives the change in acceleration*/
                val delta = mAccelerationCurrent - mAccelerationLast
                /*Here we calculate thelower filter
             * The written below is a formula to get it*/
                mAcceleration = mAcceleration * 0.9f + delta
                /*We obtain a real number for acceleration
                              * and we check if the acceleration was noticeable, considering 12 here*/
                if (mAcceleration > 12) {
                    /*If the accel was greater than 12 we change the song, given the fact our shake to change was active*/
                    val prefs = Statified.myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME, Context.MODE_PRIVATE)
                    val isAllowed = prefs?.getBoolean("feature", false)
                    if (isAllowed as Boolean) {
                        //if shake feature is allowed then next song
                        Staticated.playNext("PlayNextNormal")
                        Statified.playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
                    }

                }
            }

        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Statified.favotiteContent = EchoDatabase(Statified.myActivity)
        Statified.currentSongHelper = CurrentSongHelper()
        Statified.currentSongHelper?.isPlaying = true
        Statified.currentSongHelper?.isLoop = false
        Statified.currentSongHelper?.isShuffle = false

        var path: String? = null
        var _songTitle: String? = null
        var _songArtist: String? = null
        var songId: Long = 0
        try {
            path = arguments?.getString("path")
            _songTitle = arguments?.getString("songTitle")
            _songArtist = arguments?.getString("songArtist")
            songId = arguments?.getInt("songId")!!.toLong()
            Statified.currentPosition = arguments?.getInt("position")!!
            Statified.fetchSongs = arguments?.getParcelableArrayList("songData")

            Statified.currentSongHelper?.songPath = path
            Statified.currentSongHelper?.songArtist = _songArtist
            Statified.currentSongHelper?.songId = songId
            Statified.currentSongHelper?.songTitle = _songTitle
            Statified.currentSongHelper?.currentPosition = Statified.currentPosition

            Staticated.updateTextViews(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        // TODO: 12-11-2020 Make changes here to get song of happy Fragment also
        var fromFavBottomBar = arguments!!.get("FavBottomBar") as? String
//        var fromHapBottomBar = arguments!!.get("HapBottomBar") as? String
//        var fromSadBottomBar = arguments!!.get("SadBottomBar") as? String

        if (fromFavBottomBar != null) {
            Statified.mediaplayer = FavoriteFragment.Statified.mediaPlayer
        } else {
            Statified.mediaplayer = MediaPlayer()
            Statified.mediaplayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                Statified.mediaplayer?.setDataSource(Statified.myActivity, Uri.parse(path))
                Statified.mediaplayer?.prepare()

            } catch (e: Exception) {
                e.printStackTrace()
            }
            Statified.mediaplayer?.start()

        }
        Staticated.processInformation(Statified.mediaplayer as MediaPlayer)
        if (Statified.currentSongHelper?.isPlaying as Boolean) {
            Statified.playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            Statified.playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        Statified.mediaplayer?.setOnCompletionListener {
            Staticated.onSongComplete()
        }
        clickHandler()
        var visualizationHandler = DbmHandler.Factory.newVisualizerHandler(Statified.myActivity as Context, 0)
        Statified.audioVisualization?.linkTo(visualizationHandler)

        var prefsForShuffle = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)
        var isShuffleAllowed = prefsForShuffle?.getBoolean("feature", false)
        if (isShuffleAllowed as Boolean) {
            Statified.currentSongHelper?.isShuffle = true
            Statified.currentSongHelper?.isLoop = false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        } else {
            Statified.currentSongHelper?.isShuffle = false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
        }
        var prefsForLoop = Statified.myActivity?.getSharedPreferences(Staticated.My_PREFS_LOOP, Context.MODE_PRIVATE)
        var isLoopAllowed = prefsForLoop?.getBoolean("feature", false)
        if (isLoopAllowed as Boolean) {
            Statified.currentSongHelper?.isShuffle = false
            Statified.currentSongHelper?.isLoop = true
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
        } else {
            Statified.currentSongHelper?.isLoop = false
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        }

        if (Statified.favotiteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity!!, R.drawable.favorite_on))
        } else {
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity!!, R.drawable.favorite_off))
        }
        if (Statified.mediaplayer?.isPlaying as Boolean) {
            Statified.playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            Statified.playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }

        //to make seekbar adjustable
        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2) {
                    Statified.mediaplayer?.seekTo(p1)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })
    }


    fun clickHandler() {
//        Favorite Button
        Statified.fab?.setOnClickListener {
            if (Statified.favotiteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity!!, R.drawable.favorite_off))
                Statified.favotiteContent?.deleteFavourite(Statified.currentSongHelper?.songId?.toInt() as Int)
                Toast.makeText(Statified.myActivity, "Removed from favorites", Toast.LENGTH_SHORT).show()
            } else {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity!!, R.drawable.favorite_on))
                Statified.favotiteContent?.storeAsFavorite(Statified.currentSongHelper?.songId?.toInt(), Statified.currentSongHelper?.songArtist,
                        Statified.currentSongHelper?.songTitle, Statified.currentSongHelper?.songPath)
                Toast.makeText(Statified.myActivity, "Added to favorites", Toast.LENGTH_SHORT).show()
                Log.e("SongPlaying Fragment", "Pathof Song: ${Statified.currentSongHelper?.songPath}")
            }
        }

//        Shuffle button
        Statified.shuffleImageButton?.setOnClickListener {
            var editorShuffle = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)?.edit()
            var editorLoop = Statified.myActivity?.getSharedPreferences(Staticated.My_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()

            if (Statified.currentSongHelper?.isShuffle as Boolean) {
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                Statified.currentSongHelper?.isShuffle = false
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
            } else {
                Statified.currentSongHelper?.isShuffle = true
                Statified.currentSongHelper?.isLoop = false
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorShuffle?.putBoolean("feature", true)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            }
        }
//        Next Button
        Statified.nextImageButton?.setOnClickListener {
            Statified.currentSongHelper?.isPlaying = true
            Statified.playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            if (Statified.currentSongHelper?.isShuffle as Boolean) {
                Staticated.playNext("PlayNextLikeNormalShuffle")
            } else {
                Staticated.playNext("PlayNextNormal")
            }
        }
        //        Previous Button
        Statified.previousImageButton?.setOnClickListener {
            Statified.currentSongHelper?.isPlaying = true
            if (Statified.currentSongHelper?.isLoop as Boolean) {
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            }
            playPrevious()
        }
//        Loop button
        Statified.loopImageButton?.setOnClickListener {
            var editorShuffle = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)?.edit()
            var editorLoop = Statified.myActivity?.getSharedPreferences(Staticated.My_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()
            if (Statified.currentSongHelper?.isLoop as Boolean) {
                Statified.currentSongHelper?.isLoop = false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            } else {
                Statified.currentSongHelper?.isLoop = true
                Statified.currentSongHelper?.isShuffle = false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorLoop?.putBoolean("feature", true)
                editorLoop?.apply()
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
            }
        }

//        Playpause Button
        Statified.playpauseImageButton?.setOnClickListener {
            if (Statified.mediaplayer?.isPlaying as Boolean) {
                Statified.mediaplayer?.pause()
                Statified.currentSongHelper?.isPlaying = false
                Statified.playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
            } else {
                Statified.mediaplayer?.start()
                Statified.currentSongHelper?.isPlaying = true
                Statified.playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        }
    }


    fun playPrevious() {
        Statified.currentPosition = Statified.currentPosition - 1
        if (Statified.currentPosition == -1) {
            Statified.currentPosition = 0
        }
        if (Statified.currentSongHelper?.isPlaying as Boolean) {
            Statified.playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            Statified.playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        Statified.currentSongHelper?.isLoop = false
        var nextSong = Statified.fetchSongs?.get(Statified.currentPosition)
        Statified.currentSongHelper?.songTitle = nextSong?.songTitle
        Statified.currentSongHelper?.songArtist = nextSong?.artist
        Statified.currentSongHelper?.songPath = nextSong?.songData
        Statified.currentSongHelper?.currentPosition = Statified.currentPosition
        Statified.currentSongHelper?.songId = nextSong?.songID as Long

        Staticated.updateTextViews(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)

        Statified.mediaplayer?.reset()
        try {
            Statified.mediaplayer?.setDataSource(Statified.myActivity as Context, Uri.parse(Statified.currentSongHelper?.songPath))
            Statified.mediaplayer?.prepare()
            Statified.mediaplayer?.start()
            Staticated.processInformation(Statified.mediaplayer as MediaPlayer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (Statified.favotiteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity!!, R.drawable.favorite_on))
        } else {
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity!!, R.drawable.favorite_off))
        }
    }
}