package com.haard.echo.fragments


import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.*
import com.haard.echo.R
import com.haard.echo.Songs
import com.haard.echo.adapters.MainScreenAdapter
import java.util.*


/**
 * A simple [Fragment] subclass.
 *
 */
class MainScreenFragment : Fragment() {

    var getSongsList: ArrayList<Songs>? = null
    var nowPlayingBottomBar: RelativeLayout? = null
    var visibleLayout: RelativeLayout? = null
    var noSongs: RelativeLayout? = null
    var recyclerView: RecyclerView? = null

    var songTitle: TextView? = null

    var myActivity: Activity? = null
    var trackPosition: Int = 0

    var _mainScreenAdapter: MainScreenAdapter? = null


    object Statified{

        var playPauseButton: ImageButton? = null
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        val view = inflater!!.inflate(R.layout.fragment_main_screen, container, false)

        activity?.title = "All Songs"

        visibleLayout = view?.findViewById(R.id.visibleLayout)
        noSongs = view?.findViewById(R.id.noSongs)
        songTitle = view?.findViewById(R.id.songTitleMainScreen)
        nowPlayingBottomBar = view?.findViewById(R.id.hiddenBarMainScreen)
        recyclerView = view?.findViewById<RecyclerView>(R.id.contentMain)
        Statified.playPauseButton = view?.findViewById(R.id.playPauseButton)
        Statified.playPauseButton?.setBackgroundResource(R.drawable.play_icon)
        setHasOptionsMenu(true)
        return view


    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        getSongsList = getSongsFromPhone()

        val prefs =myActivity?.getSharedPreferences("action_sort",Context.MODE_PRIVATE)

        val action_sort_ascending = prefs?.getString("action_sort_ascending","true")
        val action_sort_recent = prefs?.getString("action_sort_recent","false")


        _mainScreenAdapter = MainScreenAdapter(getSongsList as ArrayList<Songs>, myActivity as Context)

        var mLayoutManager = LinearLayoutManager(myActivity)
        recyclerView?.layoutManager = mLayoutManager
        recyclerView?.itemAnimator = DefaultItemAnimator()

        recyclerView?.adapter = _mainScreenAdapter

        if(getSongsList == null){

            visibleLayout?.visibility = View.INVISIBLE
            noSongs?.visibility = View.VISIBLE
        }

        else {

            if (action_sort_ascending!!.equals("true", ignoreCase = true)) {
                Collections.sort(getSongsList, Songs.Statified.nameComparator)
                _mainScreenAdapter?.notifyDataSetChanged()
            } else if (action_sort_recent!!.equals("true", ignoreCase = true)) {

                Collections.sort(getSongsList, Songs.Statified.dateComparator)
                _mainScreenAdapter?.notifyDataSetChanged()

            }

        }
        bottomBarSetup()



    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity

    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()

        inflater!!.inflate(R.menu.main,menu)
        return
   }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        val switcher = item?.itemId

        if(switcher == R.id.action_sort_ascending){


            val editor = myActivity?.getSharedPreferences("action_sort",Context.MODE_PRIVATE)?.edit()
            editor?.putString("action_sort_ascending","true")
            editor?.putString("action_sort_recent","false")
            editor?.apply()

            if(getSongsList != null){

                Collections.sort(getSongsList,Songs.Statified.nameComparator)

            }
            _mainScreenAdapter?.notifyDataSetChanged()
            return false
        }

        else if(switcher == R.id.action_sort_recent){
            if(getSongsList != null){

                val editor = myActivity?.getSharedPreferences("action_sort",Context.MODE_PRIVATE)?.edit()
                editor?.putString("action_sort_ascending","false")
                editor?.putString("action_sort_recent","true")
                editor?.apply()


                Collections.sort(getSongsList,Songs.Statified.dateComparator)

                return false

            }
            _mainScreenAdapter?.notifyDataSetChanged()

        }

        return super.onOptionsItemSelected(item)
    }


    fun getSongsFromPhone(): ArrayList<Songs> {

        var arrayList = ArrayList<Songs>()

        var contentResolver = myActivity?.contentResolver
        var songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var songCursor = contentResolver?.query(songUri,null,null,null,null)
        if (songCursor != null && songCursor.moveToFirst()) {

            val songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val dateIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            while (songCursor.moveToNext()) {

                var currentId = songCursor.getLong(songId)
                var currentTite = songCursor.getString(songTitle)
                var currentArtist = songCursor.getString(songArtist)
                var currentData = songCursor.getString(songData)
                var currentDate = songCursor.getLong(dateIndex)

                arrayList.add(Songs(currentId, currentTite, currentArtist, currentData, currentDate))
            }

        }
        return arrayList
    }
    fun bottomBarSetup() {
        try {

            if(SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean){
            Statified.playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
            }
            else{
            Statified.playPauseButton?.setBackgroundResource(R.drawable.play_icon)
            }
            bottomBarClickHandler()

            songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            SongPlayingFragment.Statified.mediaPlayer?.setOnCompletionListener({
                songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
                SongPlayingFragment.Staticated.onSongComplete()

            })

     /*       if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean) {

                nowPlayingBottomBar?.visibility = View.VISIBLE
            } else {

                nowPlayingBottomBar?.visibility = View.INVISIBLE
            }*/
        } catch (e: Exception) {


        }

    }

    fun bottomBarClickHandler() {
        if(SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean || SongPlayingFragment.Statified.mediaPlayer != null ){
        nowPlayingBottomBar?.setOnClickListener({

            FavoriteFragment.Statified.mediaPlayer = SongPlayingFragment.Statified.mediaPlayer

            var songPlayingFragment = SongPlayingFragment()
            var args = Bundle()
            args.putString("songArtist", SongPlayingFragment.Statified.currentSongHelper?.songArtist)
            args.putString("songTitle", SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            args.putString("path", SongPlayingFragment.Statified.currentSongHelper?.songPath)
            args.putInt("songId", SongPlayingFragment.Statified.currentSongHelper?.songId?.toInt() as Int)
            args.putInt("songPosition", SongPlayingFragment.Statified.currentSongHelper?.currentPosition?.toInt() as Int)
            args.putParcelableArrayList("songData", SongPlayingFragment.Statified.fetchSongs)
            args.putString("FavBottomBar", "success")
            songPlayingFragment.arguments = args

            fragmentManager?.beginTransaction()?.replace(R.id.details_fragment, songPlayingFragment)
                    ?.addToBackStack("SongPlayingFragment")
                    ?.commit()


        })

            Statified.playPauseButton?.setOnClickListener({
            if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean) {
                SongPlayingFragment.Statified.mediaPlayer?.pause()
                trackPosition = SongPlayingFragment.Statified.mediaPlayer?.getCurrentPosition() as Int
                Statified.playPauseButton?.setBackgroundResource(R.drawable.play_icon)
                SongPlayingFragment.Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)

            } else {
                SongPlayingFragment.Statified.mediaPlayer?.start()
                trackPosition = SongPlayingFragment.Statified.mediaPlayer?.getCurrentPosition() as Int
                SongPlayingFragment.Statified.mediaPlayer?.seekTo(trackPosition)
                Statified.playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
                SongPlayingFragment.Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            }

        })

    }
    }
}
