package com.example.kotlineatv2server.ui.order

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlineatv2server.R
import com.example.kotlineatv2server.SizeAddonEditActivity
import com.example.kotlineatv2server.adapter.MyFoodListAdapter
import com.example.kotlineatv2server.adapter.MyOrderAdapter
import com.example.kotlineatv2server.common.BottomSheetOrderFragment
import com.example.kotlineatv2server.common.Common
import com.example.kotlineatv2server.common.SwipeToDeleteCallback
import com.example.kotlineatv2server.eventbus.AddonSizeEditEvent
import com.example.kotlineatv2server.eventbus.ChangeMenuClick
import com.example.kotlineatv2server.eventbus.LoadOrderEvent
import com.example.kotlineatv2server.model.FoodModel
import com.example.kotlineatv2server.model.OrderModel
import com.example.kotlineatv2server.ui.foodlist.FoodListViewModel
import com.google.firebase.database.FirebaseDatabase
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.StringBuilder
import java.util.jar.Manifest

class OrderFragment:Fragment() {

    private lateinit var orderViewModel: OrderViewModel
    lateinit var recycler_order:RecyclerView
    var ordersModels:List<OrderModel> = ArrayList<OrderModel>()
    lateinit var layoutAnimationController:LayoutAnimationController
    private var adapter : MyOrderAdapter?=null
    private var txt_order_filter:TextView?=null

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
                ordersModels = orderList
                adapter = MyOrderAdapter(context!!,orderList.toMutableList())
                recycler_order.adapter = adapter
                recycler_order.layoutAnimation = layoutAnimationController
                txt_order_filter!!.setText(StringBuilder("Ordenes (").append(orderList.size).append(")"))
            }
        })

        return root
    }

    private fun initView(root: View) {
        setHasOptionsMenu(true)
        recycler_order = root.findViewById(R.id.recycler_order) as RecyclerView
        recycler_order.setHasFixedSize(true)
        recycler_order.layoutManager = LinearLayoutManager(context)
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(context,R.anim.layout_item_from_left)
        txt_order_filter = root.findViewById(R.id.txt_order_filter) as TextView


        val swipeHandler = @SuppressLint("UseRequireInsteadOfGet")
        object : SwipeToDeleteCallback(context!!) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = recycler_order!!.adapter as MyOrderAdapter
                dialogQuestion(viewHolder.adapterPosition)
                //Common.foodModelSelected = ordersModels[viewHolder.adapterPosition]
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recycler_order)


    }

    @SuppressLint("UseRequireInsteadOfGet")
    fun dialogQuestion(pos:Int){
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Elegir una opcion")
        builder.setCancelable(false)
        val itemview = LayoutInflater.from(context).inflate(R.layout.layout_dialog_order_questions,null)
        val rdb_editar = itemview.findViewById<View>(R.id.rdb_editar_orden)as RadioButton
        val rdb_eliminar = itemview.findViewById<View>(R.id.rdb_eliminar_orden)as RadioButton
        val rdb_call_customer = itemview.findViewById<View>(R.id.rdb_call_customer) as RadioButton


        builder.setNegativeButton("CANCELAR"){dialogInterface,_->dialogInterface.dismiss()
            //orderViewModel.getOrderModelList().value = Common.category_selected!!.foods
        }


        builder.setPositiveButton("ACEPTAR"){dialogInterface,_->
            //dialogInterface.dismiss()
            if(rdb_call_customer.isChecked){

               Dexter.withContext(this@OrderFragment.context)
                 .withPermission(android.Manifest.permission.CALL_PHONE)
                .withListener(object: PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    val orderModel = adapter!!.getItemAtPosition(pos)
                    val intent = Intent()
                    intent.setAction(Intent.ACTION_DIAL)
                        intent.setData(Uri.parse(StringBuilder("tel: ").append(orderModel.userPhone).toString()))
                    startActivity(intent)
                  }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: PermissionRequest?,
                        p1: PermissionToken?
                    ) {

                    }

                    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {

                    }

                }).check()

            }else if(rdb_eliminar.isChecked){
                val orderModel = adapter!!.getItemAtPosition(pos)
                val alerta = AlertDialog.Builder(this@OrderFragment.context!!)
                    .setTitle("Eliminar")
                    .setMessage("¿Seguro de eliminar la orden?")
                    .setPositiveButton("SI"){dialogInterface,i->

                        FirebaseDatabase.getInstance()
                            .getReference(Common.ORDER_REF)
                            .child(orderModel.key!!)
                            .removeValue()
                            .addOnFailureListener{
                                Toast.makeText(context,""+it.message,Toast.LENGTH_LONG).show()
                            }
                            .addOnSuccessListener{
                                adapter!!.removeItem(pos)
                                adapter!!.notifyItemRemoved(pos)
                                txt_order_filter!!.setText(StringBuilder("Ordenes (").append(adapter!!.itemCount).append(")"))
                                Toast.makeText(context,"Eliminado correctamente",Toast.LENGTH_LONG).show()
                                dialogInterface.dismiss()
                            }

                    }
                    .setNegativeButton("NO"){dialogInterface,i->dialogInterface.dismiss()}
                val mostrar = alerta.create()
                mostrar.show()

                val btn_negative = mostrar.getButton(DialogInterface.BUTTON_NEGATIVE)
                btn_negative.setTextColor(Color.LTGRAY)
                val btn_positive = mostrar.getButton(DialogInterface.BUTTON_POSITIVE)
                btn_positive.setTextColor(Color.RED)

            }else{
                Toast.makeText(context,"Permiso Denegado",Toast.LENGTH_LONG).show()
            }



        }

        builder.setView(itemview)
        val dialog = builder.create()
        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.order_list_menu,menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.action_filter){

            val bottomSheet = BottomSheetOrderFragment.instance
            bottomSheet!!.show(requireActivity().supportFragmentManager,"OrderList")
        }
        return true
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onStop() {
        if (EventBus.getDefault().hasSubscriberForEvent(LoadOrderEvent::class.java))
             EventBus.getDefault().removeStickyEvent(LoadOrderEvent::class.java)
        if (EventBus.getDefault().isRegistered(this))
             EventBus.getDefault().unregister(this)
        super.onStop()
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(ChangeMenuClick(true))
        super.onDestroy()
    }


    //ESCUCHAMOS EL EVENTO
    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onLoadOrder(event:LoadOrderEvent){

        orderViewModel.loadOrder(event.status)


    }

}