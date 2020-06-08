package com.example.kotlineatv2server.callback


import com.example.kotlineatv2server.model.ShipperModel

interface IShipperLoadCallbackListener {
    fun onShipperLoadSuccess(shipperModelList:List<ShipperModel>)
    fun onShipperLoadFailed(message:String)
}