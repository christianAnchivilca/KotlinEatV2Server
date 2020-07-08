package com.example.kotlineatv2server.callback

import com.example.kotlineatv2server.model.ShippingOrderModel

interface ISingleShippingOrderCallbackListener {

    fun onSingleShippingOrderSuccess(shippingOrderModel:ShippingOrderModel)
}