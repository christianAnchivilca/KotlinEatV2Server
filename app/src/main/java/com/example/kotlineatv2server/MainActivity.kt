package com.example.kotlineatv2server

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.kotlineatv2server.Common.Common
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class MainActivity : AppCompatActivity() {

    //VARIABLES MIEMBROS
    private var firebaseAuth:FirebaseAuth?=null
    private var listener:FirebaseAuth.AuthStateListener? = null
    private var dialog:AlertDialog?=null
    private var serverRef:DatabaseReference?=null
    private var providers:List<AuthUI.IdpConfig>? = null

    override fun onStart() {
        super.onStart()
        firebaseAuth!!.addAuthStateListener(listener!!)
    }

    override fun onStop() {
        firebaseAuth!!.removeAuthStateListener(listener!!)
        super.onStop()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        init()
    }

    private fun init(){
        providers = Arrays.asList<AuthUI.IdpConfig>(AuthUI.IdpConfig.PhoneBuilder().build())
        serverRef = FirebaseDatabase.getInstance().getReference(Common.SERVER_REF)
    }
}
