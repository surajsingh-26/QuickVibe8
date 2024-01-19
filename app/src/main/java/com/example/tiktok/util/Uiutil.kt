package com.example.tiktok.util

import android.content.Context
import android.widget.Toast

object Uiutil {

    fun showToast(context : Context,message : String){
        Toast.makeText(context,message,Toast.LENGTH_LONG).show()
    }
}