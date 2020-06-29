package com.example.kotlineatv2server.model

class TokenModel {

    var uid:String?=null
    var token:String?=null
    var shipperToken:Boolean =false
    var serverToken:Boolean = false

    constructor(){}
    constructor(uid: String?, token: String?, shipperToken: Boolean, serverToken: Boolean) {
        this.uid = uid
        this.token = token
        this.shipperToken = shipperToken
        this.serverToken = serverToken
    }


}