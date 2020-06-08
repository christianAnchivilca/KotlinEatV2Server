package com.example.kotlineatv2server.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlineatv2server.R
import com.example.kotlineatv2server.callback.IRecyclerItemClickListener
import com.example.kotlineatv2server.model.ShipperModel


class MyShipperSelectedAdapter (internal var context: Context,
                        internal var shipperList:List<ShipperModel>): RecyclerView.Adapter<MyShipperSelectedAdapter.MyViewHolder>(){

    var lasCheckedImageView:ImageView?= null
    var selectedShipper:ShipperModel?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_shipper_selected,parent,false))
    }

    override fun getItemCount(): Int {
        return shipperList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.txt_name!!.setText(shipperList[position].name)
        holder.txt_phone!!.setText(shipperList[position].phone)
        holder.setListener(object :IRecyclerItemClickListener{
            override fun onItemClick(view: View, pos: Int) {
                if (lasCheckedImageView != null)
                    lasCheckedImageView!!.setImageResource(0)
                holder.img_checked!!.setImageResource(R.drawable.ic_check_black_24dp)
                lasCheckedImageView = holder.img_checked
                selectedShipper = shipperList[pos]


            }

        })

    }


    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView),
        View.OnClickListener {


        override fun onClick(v: View?) {
            iRecyclerItemClickListener!!.onItemClick(v!!,adapterPosition)
        }

        var txt_name: TextView?=null
        var txt_phone: TextView?=null
        var img_checked: ImageView?=null
        var iRecyclerItemClickListener:IRecyclerItemClickListener?=null

        fun setListener(iRecyclerItemClickListener:IRecyclerItemClickListener){
            this.iRecyclerItemClickListener = iRecyclerItemClickListener
        }

        init {
            txt_name = itemView.findViewById<View>(R.id.txt_name) as TextView
            txt_phone = itemView.findViewById<View>(R.id.txt_phone) as TextView
            img_checked = itemView.findViewById<View>(R.id.img_checked) as ImageView
            itemView.setOnClickListener(this)
        }



    }
}