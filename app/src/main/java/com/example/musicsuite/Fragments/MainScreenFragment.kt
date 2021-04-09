package com.example.musicsuite.Fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicsuite.Adapters.MainScreenAdapter
import com.example.musicsuite.R
import com.example.musicsuite.Songs
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 * Use the [MainScreenFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainScreenFragment : Fragment() {

    object Statified {
        var getSongsList: ArrayList<Songs>? = null
//        var happyContent: EchoDatabase? = null
//        var sadContent: EchoDatabase? = null
//        var currentSongHelper: CurrentSongHelper? = null

    }



    var nowPlayingBottomBar: RelativeLayout? = null
    var playPauseButton: ImageButton? = null
    var songTitle: TextView? = null
    var visibleLayout: RelativeLayout? = null
    var noSongs: RelativeLayout? = null
    var recyclerView: RecyclerView? = null
    var myActivity: Activity? = null
    var _mainScreenAdapter: MainScreenAdapter? = null
    var trackPosition: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_main_screen, container, false)
        /*This is used to tell the activity that the fragment has a menu*/
        setHasOptionsMenu(true)
        activity?.title = "All songs"
        visibleLayout = view?.findViewById<RelativeLayout>(R.id.visibleLayout)
//        visibleLayout = view?.findViewById<RelativeLayout>(R.id.visibleLayout)
        noSongs = view?.findViewById<RelativeLayout>(R.id.noSongs)
        nowPlayingBottomBar = view.findViewById<RelativeLayout>(R.id.hiddenBarMainScreen)
        songTitle = view?.findViewById<TextView>(R.id.songTitleMainScreen)
        playPauseButton = view?.findViewById<ImageButton>(R.id.playPauseButton)
        recyclerView = view?.findViewById<RecyclerView>(R.id.contentMain)

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu?.clear()
        inflater?.inflate(R.menu.main, menu)

//        val searchItem = menu?.findItem(R.id.action_search)
//        if (searchItem != null ) {
//            val searchView = searchItem?.actionView as SearchView
//
//            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//                override fun onQueryTextSubmit(query: String?): Boolean {
//
//                    return false
//                }
//
//                override fun onQueryTextChange(newText: String?): Boolean {
//
//                    var temp = getSongsFromPhone()
//
//                    if (!newText!!.toLowerCase().trim().equals("")) {
//                        //filter the list
//                        displayList?.clear()
//                        val search = newText.toLowerCase().trim()
//                        Statified.getSongsList?.forEach {
//                            if (it.songTitle.toLowerCase().contains(search)) {
//                                displayList?.add(it)
//                            }
//                        }
//                        recyclerView?.adapter = MainScreenAdapter(Statified.getSongsList as ArrayList<Songs>, myActivity as Context)
//                    } else {
//                        Statified.getSongsList = temp
//                        recyclerView?.adapter = MainScreenAdapter(Statified.getSongsList as ArrayList<Songs>, myActivity as Context)
//                    }
//                    return false
//                }
//            })
//        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val item: MenuItem? = menu?.findItem(R.id.action_sort)
        item?.isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val switcher = item?.itemId
        if (switcher == R.id.action_sort_ascending) {
            /*Whichever action item is selected, we save the preferences and perform the operation of comparison*/
            val editor = myActivity?.getSharedPreferences("action_sort", Context.MODE_PRIVATE)?.edit()
            editor?.putString("action_sort_ascending", "true")
            editor?.putString("action_sort_recent", "false")
            editor?.apply()
            if (Statified.getSongsList != null) {
                Collections.sort(Statified.getSongsList, Songs.Statified.nameComparator)
            }
            _mainScreenAdapter?.notifyDataSetChanged()
            return false
        } else if (switcher == R.id.action_sort_recent) {
            val editortwo = myActivity?.getSharedPreferences("action_sort", Context.MODE_PRIVATE)?.edit()
            editortwo?.putString("action_sort_recent", "true")
            editortwo?.putString("action_sort_ascending", "false")
            editortwo?.apply()
            if (Statified.getSongsList != null) {
                Collections.sort(Statified.getSongsList, Songs.Statified.dateComparator)
            }
            _mainScreenAdapter?.notifyDataSetChanged()
            return false
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        Statified.happyContent = EchoDatabase(myActivity)
//        Statified.sadContent = EchoDatabase(myActivity)

//        Statified.currentSongHelper = CurrentSongHelper()
        Statified.getSongsList = getSongsFromPhone()
        val set: HashSet<Songs> = HashSet(Statified.getSongsList)
        Statified.getSongsList!!.clear();
        Statified.getSongsList!!.addAll(set);
        // TODO: 10-11-2020 Fix this 2 times calling of activity result method
        Log.e("This is me Songlist", "SongLIST: " + Statified.getSongsList)

//        Statified.currentSongHelper?.isPlaying = false
//        Statified.currentSongHelper?.isLoop = false
//        Statified.currentSongHelper?.isShuffle = false

//        var path: String? = null
//        var _songTitle: String? = null
//        var _songArtist: String? = null
//        var songId: Long = 0
//
//        try {
//            path = arguments?.getString("path")
//            _songTitle = arguments?.getString("songTitle")
//            _songArtist = arguments?.getString("songArtist")
//            songId = arguments?.getInt("songId")!!.toLong()
//
//
//
//            Statified.currentSongHelper?.songPath = path
//            Statified.currentSongHelper?.songArtist = _songArtist
//            Statified.currentSongHelper?.songId = songId
//            Statified.currentSongHelper?.songTitle = _songTitle
//
//            Log.e("TAG", "onActivityCreated: SongPAth: $path" )
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }


        /*Declaring the preferences to save the sorting order which we select*/
        val prefs = activity?.getSharedPreferences("action_sort", Context.MODE_PRIVATE)
        val action_sort_ascending = prefs?.getString("action_sort_ascending", "true")
        val action_sort_recent = prefs?.getString("action_sort_recent", "false")
        /*If there are no songs we do not display the list instead we display no songs message*/
        if (Statified.getSongsList == null) {
            visibleLayout?.visibility = View.INVISIBLE
            noSongs?.visibility = View.VISIBLE
        } else {
            //here i have to change getSongList to DisplayList
            _mainScreenAdapter = MainScreenAdapter(Statified.getSongsList as ArrayList<Songs>, myActivity as Context)
            val mLayoutManager = LinearLayoutManager(myActivity)
            recyclerView?.layoutManager = mLayoutManager
            recyclerView?.itemAnimator = DefaultItemAnimator()
            recyclerView?.adapter = _mainScreenAdapter
            recyclerView?.setHasFixedSize(true)
        }


        /*If the songs list is not empty, then we check whether applied any comparator
       * And we use that comparator and sort the list accordingly*/
        if (Statified.getSongsList != null) {
            if (action_sort_ascending!!.equals("true", true)) {
                Collections.sort(Statified.getSongsList, Songs.Statified.nameComparator)
                _mainScreenAdapter?.notifyDataSetChanged()
            } else if (action_sort_recent!!.equals("true", true)) {
                Collections.sort(Statified.getSongsList, Songs.Statified.dateComparator)
                _mainScreenAdapter?.notifyDataSetChanged()
            }
        }
        bottomBarSetup()
//        displayList?.addAll(Statified.getSongsList!!)
    }

    /*Called when the fragment is first attached to its context*/
    override fun onAttach(context: Context) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        myActivity = activity
    }


    fun getSongsFromPhone(): ArrayList<Songs> {
        var arrayList = ArrayList<Songs>()
        var contentResolver = myActivity?.contentResolver
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
            FavoriteFragment.Statified.mediaPlayer = SongPlayingFragment.Statified.mediaplayer
//            HappyFragment.Statified.mediaPlayer = SongPlayingFragment.Statified.mediaplayer
            var args = Bundle()
            val songPlayingFragment = SongPlayingFragment()
            args.putString("songArtist", SongPlayingFragment.Statified.currentSongHelper?.songArtist)
            args.putString("path", SongPlayingFragment.Statified.currentSongHelper?.songPath)
            args.putString("songTitle", SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            args.putInt("songId", SongPlayingFragment.Statified.currentSongHelper?.songId?.toInt() as Int)
            args.putInt("songPosition", SongPlayingFragment.Statified.currentSongHelper?.currentPosition?.toInt() as Int)
            args.putParcelableArrayList("songData", SongPlayingFragment.Statified.fetchSongs)
            args.putString("FavBottomBar", "success")
//            args.putString("HapBottomBar", "success")
            songPlayingFragment.arguments = args
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
}