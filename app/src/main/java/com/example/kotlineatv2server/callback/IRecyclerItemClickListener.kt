package com.example.kotlineatv2server.callback

import android.view.View

interface IRecyclerItemClickListener {
    fun onItemClick(view:View,pos:Int)
}