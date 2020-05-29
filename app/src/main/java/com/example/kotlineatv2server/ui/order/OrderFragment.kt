package com.example.kotlineatv2server.ui.order

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlineatv2server.R
import com.example.kotlineatv2server.adapter.MyOrderAdapter
import com.example.kotlineatv2server.ui.foodlist.FoodListViewModel

class OrderFragment:Fragment() {

    private lateinit var orderViewModel: OrderViewModel
    lateinit var recycler_order:RecyclerView
    lateinit var layoutAnimationController:LayoutAnimationController
    private var adapter : MyOrderAdapter?=null

    @SuppressLint("FragmentLiveDataObserve", "UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        orderViewModel = ViewModelProviders.of(this).get(OrderViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_order, container, false)
        initView(root)

        orderViewModel!!.messageError.observe(this, Observer {error->
            Toast.makeText(context,""+error,Toast.LENGTH_LONG).show()
        })

        orderViewModel!!.getOrderModelList().observe(this, Observer {orderList->
            if (orderList != null){
                adapter = MyOrderAdapter(context!!,orderList)
                recycler_order.adapter = adapter
                recycler_order.layoutAnimation = layoutAnimationController
            }


        })

        return root
    }

    private fun initView(root: View) {
        recycler_order = root.findViewById(R.id.recycler_order) as RecyclerView
        recycler_order.setHasFixedSize(true)
        recycler_order.layoutManager = LinearLayoutManager(context)
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(context,R.anim.layout_item_from_left)

    }

}