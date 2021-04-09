package com.example.musicsuite.Fragments

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicsuite.Adapters.FavoriteAdapter
import com.example.musicsuite.databases.EchoDatabase
import com.example.musicsuite.R
import com.example.musicsuite.Songs


/**
 * A simple [Fragment] subclass.
 * Use the [FavoriteFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FavoriteFragment : Fragment() {
    var myActivity: Activity? = null

    var noFavorites: TextView? = null
    var nowPlayingBottomBar: RelativeLayout? = null
    var playPauseButton: ImageButton? = null
    var songTitle: TextView? = null
    var recyclerView: RecyclerView? = null
    var trackPosition: Int = 0
    var favoriteContent: EchoDatabase? = null

    var refreshList: ArrayList<Songs>? = null
    var getListfromDatabase: ArrayList<Songs>? = null

    object Statified {
        var mediaPlayer: MediaPlayer? = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favorite, container, false)
        activity?.title = "Favorites"
        setHasOptionsMenu(true)
        noFavorites = view?.findViewById(R.id.noFavorites)
        nowPlayingBottomBar = view.findViewById(R.id.hiddenBarFavScreen)
        songTitle = view.findViewById(R.id.songTitleFavScreen)
        playPauseButton = view.findViewById(R.id.playPauseButton)
        recyclerView = view.findViewById(R.id.favoriteRecycler)
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        favoriteContent = EchoDatabase(myActivity)
        display_favorites_by_searching()
        bottomBarSetup()

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val item = menu?.findItem(R.id.action_sort)
        item?.isVisible = false
        val item2 = menu?.findItem(R.id.action_search)
        item2?.isVisible = false
    }

    fun getSongsFromPhone(): ArrayList<Songs> {
        val arrayList = ArrayList<Songs>()
        val contentResolver = myActivity?.contentResolver
        val songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val songCursor = contentResolver?.query(songUri, null, null, null, null)
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

    fun bottomBarSetup() {
        try {
            bottomBarClickHandler()
            songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            SongPlayingFragment.Statified.mediaplayer?.setOnCompletionListener {
                songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
                SongPlayingFragment.Staticated.onSongComplete()
            }
            if (SongPlayingFragment.Statified.mediaplayer?.isPlaying as Boolean) {
                nowPlayingBottomBar?.visibility = View.VISIBLE
            } else {
                nowPlayingBottomBar?.visibility = View.INVISIBLE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun bottomBarClickHandler() {
        nowPlayingBottomBar?.setOnClickListener {
            Statified.mediaPlayer = SongPlayingFragment.Statified.mediaplayer
            var args = Bundle()
            val songPlayingFragment = SongPlayingFragment()
            args.putString("songArtist", SongPlayingFragment.Statified.currentSongHelper?.songArtist)
            args.putString("path", SongPlayingFragment.Statified.currentSongHelper?.songPath)
            args.putString("songTitle", SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            args.putInt("songId", SongPlayingFragment.Statified.currentSongHelper?.songId?.toInt() as Int)
            args.putInt("songPosition", SongPlayingFragment.Statified.currentSongHelper?.currentPosition?.toInt() as Int)
            args.putParcelableArrayList("songData", SongPlayingFragment.Statified.fetchSongs)
            args.putString("FavBottomBar", "success")
            songPlayingFragment.arguments = args
            //Here I change the code
            fragmentManager!!.beginTransaction()
                    .replace(R.id.details_fragment, songPlayingFragment)
                    .addToBackStack("SongPlayingFragment")
                    .commit()


        }

        playPauseButton?.setOnClickListener {
            if (SongPlayingFragment.Statified.mediaplayer?.isPlaying as Boolean) {
                SongPlayingFragment.Statified.mediaplayer?.pause()
                trackPosition = SongPlayingFragment.Statified.mediaplayer?.getCurrentPosition() as Int
                playPauseButton?.setBackgroundResource(R.drawable.play_icon)
            } else {
                SongPlayingFragment.Statified.mediaplayer?.seekTo(trackPosition)
                SongPlayingFragment.Statified.mediaplayer?.start()
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        }
    }

    fun display_favorites_by_searching() {
        if (favoriteContent?.checkSize() as Int > 0) {
            refreshList = ArrayList<Songs>()
            getListfromDatabase = favoriteContent?.queryDBList()
            var fetchListfromDevice = getSongsFromPhone()
            if (fetchListfromDevice != null) {
                for (i in 0 until fetchListfromDevice?.size) {
                    for (j in 0 until getListfromDatabase?.size as Int) {
                        if ((getListfromDatabase?.get(j)?.songID) == (fetchListfromDevice?.get(i)?.songID)) {
                            refreshList?.add((getListfromDatabase as ArrayList<Songs>)[j])
                        }
                    }
                }
            } else {

            }
            if (refreshList == null) {
                recyclerView?.visibility = View.INVISIBLE
                noFavorites?.visibility = View.VISIBLE
            } else {
                //Setting up the FavoriteAdapter
                var favoriteAdapter = FavoriteAdapter(refreshList as ArrayList<Songs>, myActivity as Context)
                //Setting the RecyclerView
                val mLayoutManager = LinearLayoutManager(activity)
                recyclerView?.layoutManager = mLayoutManager
                recyclerView?.itemAnimator = DefaultItemAnimator()
                recyclerView?.adapter = favoriteAdapter
                recyclerView?.setHasFixedSize(true)
            }

        } else {
            recyclerView?.visibility = View.INVISIBLE
            noFavorites?.visibility = View.VISIBLE
        }


    }

}