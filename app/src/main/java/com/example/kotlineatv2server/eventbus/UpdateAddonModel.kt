package com.example.kotlineatv2server.eventbus

import com.example.kotlineatv2server.model.AddonModel
import com.example.kotlineatv2server.model.SizeModel

class UpdateAddonModel {

    var addonModelList:List<AddonModel>?=null
    constructor(){}
    constructor(addonModelList:List<AddonModel>?){
      this.addonModelList = addonModelList
    }
}