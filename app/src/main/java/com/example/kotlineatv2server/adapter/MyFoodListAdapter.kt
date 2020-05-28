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
import com.example.kotlineatv2server.common.Common
import com.example.kotlineatv2server.model.FoodModel
import java.lang.StringBuilder

class MyFoodListAdapter(var context: Context,var foodList:List<FoodModel>):
    RecyclerView.Adapter<MyFoodListAdapter.MyViewHolder>() {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
      return MyViewHolder(LayoutInflater.from(context!!).inflate(R.layout.layout_food_item,parent,false))
    }

    override fun getItemCount(): Int {
      return foodList.size
    }

   

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        Glide.with(context).load(foodList.get(position).image).into(holder.img_food_image!!)
        holder.txt_food_name!!.setText(foodList.get(position).name)
        holder.txt_food_price!!.setText(StringBuilder("$ ").append(foodList.get(position).price.toString()))
        //Event
        holder.setListener(object : IRecyclerItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                Common.foodModelSelected = foodList.get(position)
                Common.foodModelSelected!!.key = position.toString()


            }

        })

    }

    fun getItemAtPosition(pos: Int): FoodModel {
        return foodList.get(pos)

    }


    inner class MyViewHolder(itemView:View):RecyclerView.ViewHolder(itemView),View.OnClickListener{

        override fun onClick(v: View?) {
            listener!!.onItemClick(v!!,adapterPosition)
        }


        var txt_food_name : TextView? = null
        var txt_food_price:TextView? = null
        var img_food_image: ImageView? = null


        internal var listener: IRecyclerItemClickListener?=null

        fun setListener(iReclycler: IRecyclerItemClickListener){

            this.listener = iReclycler
        }


        init {
            txt_food_name = itemView.findViewById(R.id.txt_food_name) as TextView
            txt_food_price = itemView.findViewById(R.id.txt_food_price) as TextView
            img_food_image = itemView.findViewById(R.id.img_food_image) as ImageView

            itemView.setOnClickListener(this)

        }

    }

}