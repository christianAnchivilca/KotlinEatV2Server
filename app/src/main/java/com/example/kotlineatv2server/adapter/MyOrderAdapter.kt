package com.example.kotlineatv2server.adapter

import android.content.Context
import android.graphics.Color
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kotlineatv2server.R
import com.example.kotlineatv2server.common.Common
import com.example.kotlineatv2server.model.OrderModel
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*

class MyOrderAdapter(internal var context: Context,internal var orderList:MutableList<OrderModel>):
    RecyclerView.Adapter<MyOrderAdapter.MyViewHolder>() {

    internal var calendar: Calendar
    internal var simpleDateFormat: SimpleDateFormat
    init {
        calendar = Calendar.getInstance()
        simpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
         return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_order_item,parent,false))
    }

    override fun getItemCount(): Int {
       return orderList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        Glide.with(context!!).load(orderList[position].cartItemList!![0].foodImage).into(holder.img_order!!)
        calendar.timeInMillis =orderList[position].createDate

        holder.txt_order_number!!.setText(orderList[position].key)
        Common.setSpanStringColor("Order date ",simpleDateFormat.format(orderList[position].createDate),
            holder.txt_order_time, Color.parseColor("#333639"))
        Common.setSpanStringColor("Order status: ",Common.convertStatusToString(orderList[position].orderStatus),
            holder.txt_order_status, Color.parseColor("#005758"))

        Common.setSpanStringColor("Num of items: ",if (orderList[position].cartItemList == null) "0" else
            orderList[position].cartItemList!!.size.toString(),
            holder.txt_num_item, Color.parseColor("#005754"))

        Common.setSpanStringColor("Name: ",orderList[position].userName,
            holder.txt_name, Color.parseColor("#006061"))




    }

    fun getItemAtPosition(pos: Int): OrderModel {

        return orderList[pos]

    }

    fun removeItem(pos: Int) {
        orderList.removeAt(pos)

    }


    inner class MyViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){

        var txt_order_number : TextView? = null
        var txt_order_time: TextView? = null
        var txt_name: TextView? = null
        var txt_order_status:TextView? = null
        var txt_num_item:TextView? =null
        var img_order:ImageView? = null


        init {
            txt_order_number = itemView.findViewById(R.id.txt_order_number) as TextView
            txt_order_time = itemView.findViewById(R.id.txt_order_time) as TextView
            txt_name = itemView.findViewById(R.id.txt_name) as TextView
            txt_order_status = itemView.findViewById(R.id.txt_order_status) as TextView
            txt_num_item = itemView.findViewById(R.id.txt_num_item) as TextView
            img_order = itemView.findViewById(R.id.img_order) as ImageView



        }


    }

}