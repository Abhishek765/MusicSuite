package com.example.musicsuite.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicsuite.Activities.ButtonsActivity
import com.example.musicsuite.Activities.MainActivity
import com.example.musicsuite.Fragments.AboutUsFragment
import com.example.musicsuite.Fragments.FavoriteFragment
import com.example.musicsuite.Fragments.MainScreenFragment
import com.example.musicsuite.Fragments.SettingsFragment
import com.example.musicsuite.R
import java.util.ArrayList

class NavigationDrawerAdapter(_contentList: ArrayList<String>, _getImages: IntArray, _context: Context)
    : RecyclerView.Adapter<NavigationDrawerAdapter.NavViewHolder>(){
    var contentList: ArrayList<String>?=null
    var getImages: IntArray?=null
    var mContext: Context?=null

    init {
        this.contentList = _contentList
        this.getImages = _getImages
        this.mContext = _context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavViewHolder {
        var itemView = LayoutInflater.from(parent?.context)
            .inflate(R.layout.row_custom_navigationdrawer, parent, false)
        val returnthis = NavViewHolder(itemView)
        return returnthis
    }

    override fun getItemCount(): Int {
        return contentList?.size as Int
    }

    override fun onBindViewHolder(holder: NavViewHolder, position: Int) {
        holder?.icon_GET?.setBackgroundResource(getImages?.get(position) as Int)
        holder?.text_GET?.setText(contentList?.get(position))
        holder?.contentHolder?.setOnClickListener {
            when (position) {
                0 -> {
                    mContext?.startActivity(Intent(mContext, ButtonsActivity::class.java))

                }
                1 -> {
                    val mainScreenFragment = MainScreenFragment()
                    (mContext as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.details_fragment, mainScreenFragment)
                        .commit()
                }
                2 -> {
                    val favoriteFragment = FavoriteFragment()
                    (mContext as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.details_fragment, favoriteFragment)
                        .commit()
                }
                3 -> {
                    val settingsFragment = SettingsFragment()
                    (mContext as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.details_fragment, settingsFragment)
                        .commit()
                }
                else -> {
                    val aboutUsFragment = AboutUsFragment()
                    (mContext as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.details_fragment, aboutUsFragment)
                        .commit()
                }
            }
            MainActivity.Statified.drawerLayout?.closeDrawers()
        }


    }

    class NavViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var icon_GET: ImageView?=null
        var text_GET: TextView?=null
        var contentHolder: RelativeLayout?=null
        init{
            icon_GET = itemView?.findViewById(R.id.icon_navdrawer)
            text_GET = itemView?.findViewById(R.id.text_navdrawer)
            contentHolder = itemView?.findViewById(R.id.navdrawer_item_content_holder)
        }


    }

}