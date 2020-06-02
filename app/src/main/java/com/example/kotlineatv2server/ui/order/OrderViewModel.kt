package com.example.kotlineatv2server.ui.order

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlineatv2server.callback.IOrderCallbackListener
import com.example.kotlineatv2server.common.Common

import com.example.kotlineatv2server.model.OrderModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.ArrayList

class OrderViewModel:ViewModel(), IOrderCallbackListener {
    override fun onOrderLoadSuccess(orderModel: List<OrderModel>) {
        if (orderModel.size >= 0){

            Collections.sort(orderModel){t1,t2->
                if (t1.createDate < t2.createDate)return@sort -1
                if (t1.createDate == t2.createDate) 0 else 1
            }
            orderModelList.value = orderModel

        }


    }

    override fun onOrderLoadFailed(message: String) {
        messageError.value = message
    }

     private val orderModelList = MutableLiveData<List<OrderModel>>()
     val messageError = MutableLiveData<String>()
     private val orderCallbackListener:IOrderCallbackListener

    init {
        orderCallbackListener = this
    }

    fun getOrderModelList():MutableLiveData<List<OrderModel>>{
        loadOrder(0)
        return orderModelList

    }


    fun loadOrder(status: Int) {
        /*  Lectura de datos una sola vez => addListenerForSingleValueEvent
         En algunos casos, puede resultar útil invocar una devolución de llamada una sola vez y luego quitarla de inmediato.
     * */
        val tempList:MutableList<OrderModel> = ArrayList()
        val orderRef = FirebaseDatabase.getInstance()
            .getReference(Common.ORDER_REF)
            .orderByChild("orderStatus")
            .equalTo(status.toDouble())
        orderRef.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
               orderCallbackListener.onOrderLoadFailed(error.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (itemSnapShot in p0.children){
                    val orderModel = itemSnapShot.getValue(OrderModel::class.java)
                    orderModel!!.key = itemSnapShot.key
                    tempList.add(orderModel)

                }
                orderCallbackListener.onOrderLoadSuccess(tempList)
            }

        })

    }

}