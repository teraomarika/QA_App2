package com.example.qa_app

import java.io.Serializable
import java.util.ArrayList
import android.util.Log
class Favorite(val id: String, val title: String) : Serializable {
    //val imageBytes: ByteArray

    init {
        Log.d("Favoritemodel",title.toString())
        //mageBytes = bytes.clone()
    }
}