package com.example.kotlineatv2server.ui.shipper

import android.widget.Button
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlineatv2server.callback.IShipperLoadCallbackListener
import com.example.kotlineatv2server.common.Common
import com.example.kotlineatv2server.model.CategoryModel
import com.example.kotlineatv2server.model.OrderModel
import com.example.kotlineatv2server.model.ShipperModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ShipperViewModel:ViewModel(), IShipperLoadCallbackListener {
    override fun onShipperLoadSuccess(
        pos: Int,
        orderModel: OrderModel?,
        shipperList: List<ShipperModel>?,
        dialog: AlertDialog?,
        ok: Button?,
        cancel: Button?,
        rdi_shipping: RadioButton?,
        rdi_shipped: RadioButton?,
        rdi_cancelled: RadioButton?,
        rdi_delete: RadioButton?,
        rdi_restore_placed: RadioButton?
    ) {
        //do nothing
    }

    private var shipperListMutable:MutableLiveData<List<ShipperModel>>?=null
    private var messageError:MutableLiveData<String> = MutableLiveData()
    private val shipperCallbackListener:IShipperLoadCallbackListener
    init {
        shipperCallbackListener = this
    }

    fun getShipperList():MutableLiveData<List<ShipperModel>>{
        if (shipperListMutable == null)
        {
            shipperListMutable = MutableLiveData()
            loadShippers()

        }

        return shipperListMutable!!

    }

     fun loadShippers() {
        //get data of firebase
        val listaShippers = ArrayList<ShipperModel>()
        val shipperRef = FirebaseDatabase.getInstance().getReference(Common.SHIPPER_REF)
            shipperRef.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    shipperCallbackListener.onShipperLoadFailed(p0.message)
                }

                override fun onDataChange(data: DataSnapshot) {
                    if (data.exists()){
                        for (item in data.children){
                            val shipper = item.getValue<ShipperModel>(ShipperModel::class.java)
                            shipper!!.key = item.key
                            listaShippers.add(shipper!!)
                        }
                        shipperCallbackListener.onShipperLoadSuccess(listaShippers)
                    }

                }

            })

    }


    fun getMessageError ():MutableLiveData<String>{
        return messageError
    }

    override fun onShipperLoadSuccess(shipperModelList: List<ShipperModel>) {
        shipperListMutable!!.value = shipperModelList

    }

    override fun onShipperLoadFailed(message: String) {
        messageError!!.value = message

    }






}