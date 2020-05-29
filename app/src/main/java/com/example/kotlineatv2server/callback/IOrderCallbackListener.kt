package com.example.kotlineatv2server.callback

import com.example.kotlineatv2server.model.CategoryModel
import com.example.kotlineatv2server.model.OrderModel

interface IOrderCallbackListener {

    fun onOrderLoadSuccess(orderModelList:List<OrderModel>)
    fun onOrderLoadFailed(message:String)
}