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
import com.example.kotlineatv2server.eventbus.CategoryClick
import com.example.kotlineatv2server.model.CategoryModel
import org.greenrobot.eventbus.EventBus

class MyCategoriesAdapter (internal var context: Context,
                           internal var listCategories:List<CategoryModel>) :
    RecyclerView.Adapter<MyCategoriesAdapter.MyViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_category_item,parent,false))
    }

    override fun getItemCount(): Int {
        return listCategories.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(listCategories.get(position).image).into(holder.category_image!!)
        holder.category_name!!.setText(listCategories.get(position).name)

        holder.setListener(object : IRecyclerItemClickListener{
            override fun onItemClick(view: View, pos: Int) {

                Common.category_selected = listCategories.get(pos)
                EventBus.getDefault().postSticky(CategoryClick(true,listCategories.get(pos)))
                //Toast.makeText(context,""+listCategories.get(position).name,Toast.LENGTH_LONG).show()
            }

        })
    }


    override fun getItemViewType(position: Int): Int {
        return if (listCategories.size == 1)
            Common.DEFAULT_COLUMN_COUNT
        else{
            if (listCategories.size % 2 == 0)
                Common.DEFAULT_COLUMN_COUNT
            else
                if (position > 1 && position == listCategories.size - 1)
                    Common.FULL_WIDTH_COLUMN
                else
                    Common.DEFAULT_COLUMN_COUNT
        }



        //return super.getItemViewType(position)
    }


    inner class MyViewHolder(itemView:View):RecyclerView.ViewHolder(itemView), View.OnClickListener {
        override fun onClick(view: View?) {

            listener!!.onItemClick(view!!,adapterPosition)
        }
        var category_name : TextView?=null
        var category_image: ImageView?=null
        internal var listener:IRecyclerItemClickListener?=null

        fun setListener(iReclycler:IRecyclerItemClickListener){

            this.listener = iReclycler
        }


        init {
            category_name = itemView.findViewById(R.id.category_name) as TextView
            category_image = itemView.findViewById(R.id.category_image) as ImageView
            itemView.setOnClickListener(this)
        }

    }
}