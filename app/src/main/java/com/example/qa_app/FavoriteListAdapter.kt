package com.example.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

import java.util.ArrayList

class FavoriteListAdapter(context: Context) : BaseAdapter() {
    private var mLayoutInflater: LayoutInflater
    private var mFavoriteArrayList = ArrayList<Favorite>()

    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        Log.d("FavoriteListAdapter", "Count")
        return mFavoriteArrayList.size
    }

    override fun getItem(position: Int): Any {
        Log.d("FavoriteListAdapter", "Item")
        return mFavoriteArrayList[position]
    }

    override fun getItemId(position: Int): Long {
        Log.d("FavoriteListAdapter", "Id")
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        Log.d("FavoriteListAdapter", convertView.toString())
        var convertView = convertView
        if(position == 0) {
            Log.d("zero", "zero")

        } else {
            Log.d("not zero", "not zero")
        }
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_favorites, parent, false)
            Log.d("FavoriteListAdapter2", convertView.toString())
        }

        val titleText = convertView!!.findViewById<View>(R.id.nameTextView) as TextView
        //titleText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50.0F)
        //titleText.setGravity(Gravity.LEFT)
        //titleText.layoutDirection
        titleText.text = mFavoriteArrayList[position].title


        return convertView
    }

    fun setFavoriteArrayList(favoriteArrayList: ArrayList<Favorite>) {
        Log.d("FavoriteListAdapter", "List")
        mFavoriteArrayList = favoriteArrayList
    }
}