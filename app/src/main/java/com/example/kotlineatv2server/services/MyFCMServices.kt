package com.example.kotlineatv2server.services

import android.content.Intent
import android.util.Log
import com.example.kotlineatv2server.MainActivity
import com.example.kotlineatv2server.common.Common
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*


class MyFCMServices: FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Common.updateToken(this,token,true,false)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val dataRecv = remoteMessage.data
        if (dataRecv != null ){

            if (dataRecv[Common.NOTI_TITLE]!!.equals("Nueva Orden"))
            {
                Log.d("VALIDATE","NOTI_TITLE es igual a Nueva Orden")
                val intent = Intent(this,MainActivity::class.java)
                intent.putExtra(Common.IS_OPEN_ACTIVITY_NEW_ORDER,true)
                Common.showNotification(this,Random().nextInt(),
                    dataRecv[Common.NOTI_TITLE], dataRecv[Common.NOTI_CONTENT],intent!!)

            }
            else
                Common.showNotification(this, Random().nextInt(),
                    dataRecv[Common.NOTI_TITLE], dataRecv[Common.NOTI_CONTENT],null)

        }
    }

}