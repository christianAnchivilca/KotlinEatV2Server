package com.example.kotlineatv2server.remote

import com.example.kotlineatv2server.model.FCMResponse
import com.example.kotlineatv2server.model.FCMSendData
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IFCMService {
    @Headers(
        "Content-type:application/json",
        "Authorization:key=AAAAn_cdPTM:APA91bF2KEtmD1f-8tE26HLlKlFp3dWMnJVaSxPKiDpKDhB5pU9KkRRSk0w1S9fiy6nKgySC4D1O_pvVAT_cHOlZ0qz3aK_qP0SPVcQKvVEc22EWpH9zaodTDBnK00naOg90t9ugpOCM"
    )
    @POST("fcm/send")
    fun sendNotification(@Body body: FCMSendData): Observable<FCMResponse>

}