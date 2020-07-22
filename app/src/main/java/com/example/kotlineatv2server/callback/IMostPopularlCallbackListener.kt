package com.example.kotlineatv2server.callback

import com.example.kotlineatv2server.model.BestDealModel
import com.example.kotlineatv2server.model.CategoryModel
import com.example.kotlineatv2server.model.MostPopularModel

interface IMostPopularlCallbackListener {
    fun onListMostPopularLoadSuccess(mostPopularModelList:List<MostPopularModel>)
    fun onListMostPopularLoadFailed(message:String)
}