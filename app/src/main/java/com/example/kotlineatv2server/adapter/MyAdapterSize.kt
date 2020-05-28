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
import com.example.kotlineatv2server.eventbus.SelectSizeModel
import com.example.kotlineatv2server.eventbus.UpdateSizeModel
import com.example.kotlineatv2server.model.SizeModel
import org.greenrobot.eventbus.EventBus
import org.w3c.dom.Text

class MyAdapterSize(var context: Context,var sizeModelList:MutableList<SizeModel>):RecyclerView.Adapter<MyAdapterSize.MyViewHolder>() {

    var editPos:Int
    var updateSizeModel:UpdateSizeModel
    init {
        editPos =1
        updateSizeModel= UpdateSizeModel()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {


        return MyViewHolder(LayoutInflater.from(context!!).inflate(R.layout.layout_addon_size_item,parent,false))
    }

    override fun getItemCount(): Int {
        return sizeModelList!!.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.txt_name!!.text = sizeModelList[position].name
        holder.txt_price!!.text = sizeModelList[position].price.toString()
        holder.img_delete!!.setOnClickListener{
            sizeModelList.removeAt(position)
            notifyItemRemoved(position)
            updateSizeModel.sizeModelList = sizeModelList
            EventBus.getDefault().postSticky(updateSizeModel)
        }

        //EVENTO PARA OBTENER LA INFORMACION DE LOS ITEM EN RECYCLERVIEW
        holder.setListener(object:IRecyclerItemClickListener{
            override fun onItemClick(view: View, pos: Int) {
                editPos = pos
                EventBus.getDefault().postSticky(SelectSizeModel(sizeModelList[pos]))
            }

        })


    }

    fun addNewSize(sizeModel: SizeModel) {
        sizeModelList.add(sizeModel)
        notifyItemInserted(sizeModelList.size - 1)
        updateSizeModel.sizeModelList = sizeModelList
        EventBus.getDefault().postSticky(updateSizeModel)

    }

    fun editSize(sizeModel: SizeModel) {
        sizeModelList.set(editPos,sizeModel)
        notifyItemChanged(editPos)
        updateSizeModel.sizeModelList = sizeModelList
        EventBus.getDefault().postSticky(updateSizeModel)

    }


    inner class MyViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {



        var txt_name:TextView?=null
        var txt_price:TextView?=null
        var img_delete:ImageView?=null
        internal var listener :IRecyclerItemClickListener?=null

        init {
            txt_name = itemView.findViewById(R.id.txt_name) as TextView
            txt_price = itemView.findViewById(R.id.txt_price) as TextView
            img_delete = itemView.findViewById(R.id.img_delete) as ImageView
            itemView.setOnClickListener{view -> listener!!.onItemClick(view,adapterPosition)}

        }

        fun setListener(listener:IRecyclerItemClickListener?){
            this.listener = listener
        }

    }
}