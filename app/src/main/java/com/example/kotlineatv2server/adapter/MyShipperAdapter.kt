package com.example.kotlineatv2server.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlineatv2server.R
import com.example.kotlineatv2server.eventbus.UpdateActiveEvent
import com.example.kotlineatv2server.model.ShipperModel
import org.greenrobot.eventbus.EventBus

class MyShipperAdapter (internal var context:Context,
                        internal var shipperList:List<ShipperModel>):RecyclerView.Adapter<MyShipperAdapter.MyViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_shipper,parent,false))
    }

    override fun getItemCount(): Int {
        return shipperList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.txt_name!!.setText(shipperList[position].name)
        holder.txt_phone!!.setText(shipperList[position].phone)
        holder.btn_enable!!.isChecked = shipperList[position].isActive

        //event
        holder.btn_enable!!.setOnCheckedChangeListener{
            compoundButton,b->
            EventBus.getDefault().postSticky(UpdateActiveEvent(shipperList[position],b))

        }

    }


    inner class MyViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){

        var txt_name:TextView?=null
        var txt_phone:TextView?=null
        var btn_enable: SwitchCompat?=null

        init {
            txt_name = itemView.findViewById<View>(R.id.txt_name) as TextView
            txt_phone = itemView.findViewById<View>(R.id.txt_phone) as TextView
            btn_enable = itemView.findViewById<View>(R.id.btn_enable) as SwitchCompat
        }



    }
}