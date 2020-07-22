package com.example.kotlineatv2server.callback

import com.example.kotlineatv2server.model.BestDealModel
import com.example.kotlineatv2server.model.CategoryModel

interface IBestDealCallabackListener {
    fun onListBestDealLoadSuccess(bestDealModelList:List<BestDealModel>)
    fun onListBestDealLoadFailed(message:String)
}