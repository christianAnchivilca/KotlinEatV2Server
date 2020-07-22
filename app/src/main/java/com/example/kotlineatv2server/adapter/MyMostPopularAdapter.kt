package com.example.kotlineatv2server.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kotlineatv2server.R
import com.example.kotlineatv2server.callback.IRecyclerItemClickListener
import com.example.kotlineatv2server.model.BestDealModel
import com.example.kotlineatv2server.model.MostPopularModel

class MyMostPopularAdapter (internal var context: Context, internal var listMostPopular:List<MostPopularModel>):
    RecyclerView.Adapter<MyMostPopularAdapter.MyViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_category_item,parent,false))
    }

    override fun getItemCount(): Int {
        return listMostPopular.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(listMostPopular.get(position).image).into(holder.category_image!!)
        holder.category_name!!.setText(listMostPopular.get(position).name)

        holder.setListener(object : IRecyclerItemClickListener{
            override fun onItemClick(view: View, pos: Int) {

            }

        })

    }


    inner class MyViewHolder(itemView:View):RecyclerView.ViewHolder(itemView), View.OnClickListener {
        override fun onClick(view: View?) {

            listener!!.onItemClick(view!!,adapterPosition)
        }
        var category_name : TextView?=null
        var category_image: ImageView?=null
        internal var listener: IRecyclerItemClickListener?=null

        fun setListener(iReclycler: IRecyclerItemClickListener){

            this.listener = iReclycler
        }


        init {
            category_name = itemView.findViewById(R.id.category_name) as TextView
            category_image = itemView.findViewById(R.id.category_image) as ImageView
            itemView.setOnClickListener(this)
        }

    }

}