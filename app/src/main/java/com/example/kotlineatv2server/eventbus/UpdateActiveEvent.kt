package com.example.kotlineatv2server.eventbus

import com.example.kotlineatv2server.model.ShipperModel

class UpdateActiveEvent(var shipperModel: ShipperModel,var active:Boolean) {
}