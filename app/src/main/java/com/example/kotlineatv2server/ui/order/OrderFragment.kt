package com.example.kotlineatv2server.ui.order

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlineatv2server.PruebaActivity
import com.example.kotlineatv2server.R
import com.example.kotlineatv2server.SizeAddonEditActivity
import com.example.kotlineatv2server.TrackingOrderActivity
import com.example.kotlineatv2server.adapter.MyFoodListAdapter
import com.example.kotlineatv2server.adapter.MyOrderAdapter
import com.example.kotlineatv2server.adapter.MyShipperSelectedAdapter
import com.example.kotlineatv2server.callback.IShipperLoadCallbackListener
import com.example.kotlineatv2server.common.BottomSheetOrderFragment
import com.example.kotlineatv2server.common.Common
import com.example.kotlineatv2server.common.SwipeToDeleteCallback
import com.example.kotlineatv2server.eventbus.AddonSizeEditEvent
import com.example.kotlineatv2server.eventbus.ChangeMenuClick
import com.example.kotlineatv2server.eventbus.LoadOrderEvent
import com.example.kotlineatv2server.model.*
import com.example.kotlineatv2server.remote.IFCMService
import com.example.kotlineatv2server.remote.RetrofitFCMClient
import com.example.kotlineatv2server.ui.foodlist.FoodListViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import dmax.dialog.SpotsDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.create
import java.lang.StringBuilder
import java.util.jar.Manifest

class OrderFragment:Fragment(), IShipperLoadCallbackListener {
    override fun onShipperLoadSuccess(shipperModelList: List<ShipperModel>) {
           //do nothing
    }

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

        if (recycler_shipper != null){
            recycler_shipper!!.setHasFixedSize(true)
            val layoutManager = LinearLayoutManager(context!!)
            recycler_shipper!!.layoutManager = layoutManager
            recycler_shipper!!.addItemDecoration(DividerItemDecoration(context!!,layoutManager.orientation))
            myShipperSelectedAdapter = MyShipperSelectedAdapter(context!!,shipperList!!)
            recycler_shipper!!.adapter = myShipperSelectedAdapter
        }
        showDialog(pos,orderModel!!,dialog!!,ok!!,cancel!!,rdi_shipping,rdi_shipped,rdi_cancelled,rdi_delete,rdi_restore_placed)


    }

    override fun onShipperLoadFailed(message: String) {
        Toast.makeText(context,""+message,Toast.LENGTH_LONG).show()

    }


    private val compositeDisposable = CompositeDisposable()
    lateinit var ifcService:IFCMService
    private lateinit var orderViewModel: OrderViewModel
    lateinit var recycler_order:RecyclerView
    var ordersModels:List<OrderModel> = ArrayList<OrderModel>()
    lateinit var layoutAnimationController:LayoutAnimationController
    private var adapter : MyOrderAdapter?=null
    private var txt_order_filter:TextView?=null

    var myShipperSelectedAdapter:MyShipperSelectedAdapter?=null
    lateinit var iShipperLoadCallbackListener:IShipperLoadCallbackListener
    var recycler_shipper:RecyclerView?=null



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
                updateTextCounter()
            }
        })

        return root
    }

    private fun initView(root: View) {

        iShipperLoadCallbackListener = this
        ifcService = RetrofitFCMClient.getInstance().create(IFCMService::class.java)
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
        val rdb_direccion_tracking = itemview.findViewById<View>(R.id.rdb_directions_trackings) as RadioButton


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
                                updateTextCounter()
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

            }else if (rdb_editar.isChecked){

                showEditDialog(adapter!!.getItemAtPosition(pos),pos)

            }else if(rdb_direccion_tracking.isChecked){
                val orderModel = (recycler_order.adapter as MyOrderAdapter).getItemAtPosition(pos)
                if (orderModel.orderStatus == 1)//shipping , en rutas
                {
                    Common.currentOrderSelected = orderModel
                    startActivity(Intent(context!!,TrackingOrderActivity::class.java))

                }else{
                    Toast.makeText(context,StringBuilder("Su orden ha sido ")
                        .append(Common.convertStatusToString(orderModel.orderStatus))
                        .append("a si que no puede hacer seguimiento"),Toast.LENGTH_LONG).show()
                }

            }else{
                Toast.makeText(context,"Permiso Denegado",Toast.LENGTH_LONG).show()
            }



        }

        builder.setView(itemview)
        val dialog = builder.create()
        dialog.show()
    }

    private fun showEditDialog(orderModel: OrderModel, pos: Int) {
        var layout_dialog :View?=null
        var builder:AlertDialog.Builder?=null

         var rdi_restore_placed:RadioButton?=null
         var rdi_delete:RadioButton?=null
         var rdi_shipped:RadioButton?=null
         var rdi_shipping:RadioButton?=null
         var rdi_cancelled:RadioButton?=null


        if (orderModel.orderStatus == -1)//cancelled
        {
            layout_dialog = LayoutInflater.from(context!!).inflate(R.layout.layout_dialog_cancelled,null)



            builder = AlertDialog.Builder(context!!)
                .setView(layout_dialog)

            rdi_delete = layout_dialog.findViewById<View>(R.id.rdi_delete) as RadioButton
            rdi_restore_placed = layout_dialog.findViewById<View>(R.id.rdi_restore_placed) as RadioButton


        }else if (orderModel.orderStatus == 0)//placed
        {
            layout_dialog = LayoutInflater.from(context!!).inflate(R.layout.layout_dialog_shipping,null)
            recycler_shipper = layout_dialog.findViewById(R.id.recycler_shipper) as RecyclerView
            builder = AlertDialog.Builder(context!!,
                android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
                .setView(layout_dialog)

            rdi_shipping = layout_dialog.findViewById<View>(R.id.rdi_shipping) as RadioButton
            rdi_cancelled = layout_dialog.findViewById<View>(R.id.rdi_cancelled) as RadioButton

        }
        else
        {
            layout_dialog = LayoutInflater.from(context!!).inflate(R.layout.layout_dialog_shipped,null)
            builder = AlertDialog.Builder(context!!)
                .setView(layout_dialog)
            rdi_shipped = layout_dialog.findViewById<View>(R.id.rdi_shipped) as RadioButton
            rdi_cancelled = layout_dialog.findViewById<View>(R.id.rdi_cancelled) as RadioButton

        }

        val btn_ok = layout_dialog.findViewById<View>(R.id.btn_ok) as Button
        val btn_cancel = layout_dialog.findViewById<View>(R.id.btn_cancel) as Button
        val txt_status = layout_dialog.findViewById<View>(R.id.txt_status) as TextView


        txt_status.setText(StringBuilder("Orden Status(")
            .append(Common.convertStatusToString(orderModel.orderStatus)).append(")"))

        val dialog = builder.create()
        if (orderModel.orderStatus == 0)//shipping
            loadShipperList(pos,orderModel,dialog,btn_ok,
                btn_cancel,rdi_shipping,rdi_shipped,rdi_cancelled,rdi_delete,rdi_restore_placed)
        else
            showDialog(pos,orderModel,dialog,btn_ok,
                btn_cancel,rdi_shipping,rdi_shipped,rdi_cancelled,rdi_delete,rdi_restore_placed)
    }

    private fun loadShipperList(
        pos: Int,
        orderModel: OrderModel,
        dialog: AlertDialog,
        btn_ok: Button,
        btn_cancel: Button,
        rdi_shipping: RadioButton?,
        rdi_shipped: RadioButton?,
        rdi_cancelled: RadioButton?,
        rdi_delete: RadioButton?,
        rdi_restore_placed: RadioButton?
    ) {

        val tempList : MutableList<ShipperModel> = ArrayList<ShipperModel>()
        val shipperRef = FirebaseDatabase.getInstance().getReference(Common.SHIPPER_REF)
        val shipperActive = shipperRef.orderByChild("active").equalTo(true)
        shipperActive.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                iShipperLoadCallbackListener.onShipperLoadFailed(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (item in p0.children){
                    val shipperModel = item.getValue<ShipperModel>(ShipperModel::class.java)
                    shipperModel!!.key = item.key
                    tempList.add(shipperModel)

                }
                iShipperLoadCallbackListener.onShipperLoadSuccess(pos,orderModel,
                    tempList,dialog,btn_ok,btn_cancel,rdi_shipping,rdi_shipped,rdi_cancelled,rdi_delete,rdi_restore_placed)


            }

        })

    }

    private fun showDialog(
        pos: Int,
        orderModel: OrderModel,
        dialog: AlertDialog,
        btn_ok: Button,
        btn_cancel: Button,
        rdi_shipping: RadioButton?,
        rdi_shipped: RadioButton?,
        rdi_cancelled: RadioButton?,
        rdi_delete: RadioButton?,
        rdi_restore_placed: RadioButton?
    ) {
        dialog.show()
        btn_cancel.setOnClickListener{dialog.dismiss()}
        btn_ok.setOnClickListener {

            if (rdi_cancelled != null && rdi_cancelled!!.isChecked)
            {
                updateOrder(pos,orderModel,-1)
                dialog.dismiss()
            }
            else if(rdi_shipping != null && rdi_shipping!!.isChecked)
            {
                //updateOrder(pos,orderModel,1)
                var shipperModel:ShipperModel? = null

                if (myShipperSelectedAdapter != null){
                    shipperModel = myShipperSelectedAdapter!!.selectedShipper
                    if (shipperModel != null){

                        createShippingOrder(pos,shipperModel,orderModel,dialog)

                    }else
                        Toast.makeText(context!!,"Seleccione un repartidor",Toast.LENGTH_LONG).show()
                }
            }
            else if(rdi_shipped != null && rdi_shipped!!.isChecked)
            {
                updateOrder(pos,orderModel,2)
                dialog.dismiss()
            }
            else if(rdi_restore_placed != null && rdi_restore_placed!!.isChecked)
            {
                updateOrder(pos,orderModel,0)
                dialog.dismiss()

            }
            else if(rdi_delete != null && rdi_delete!!.isChecked)
            {
                deleteOrder(orderModel,pos)
                dialog.dismiss()
            }
        }
    }

    private fun createShippingOrder(pos:Int,shipperModel: ShipperModel, orderModel: OrderModel, dialog: AlertDialog) {

        val shippingOrder = ShippingOrderModel()
        shippingOrder.shipperName = shipperModel.name
        shippingOrder.shipperPhone = shipperModel.phone
        shippingOrder.orderModel = orderModel
        shippingOrder.isStartTrip = false
        shippingOrder.currentLat = -1.0
        shippingOrder.currentLng = -1.0
        //create shipping order in firebase
        FirebaseDatabase.getInstance()
            .getReference(Common.SHIPPING_ORDER_REF)
            .child(orderModel.key!!) //se cambio push() por key
            .setValue(shippingOrder)
            .addOnFailureListener{e->
                dialog.dismiss()
                Toast.makeText(context,""+e.message,Toast.LENGTH_LONG).show()}
            .addOnCompleteListener {task ->
                if (task.isSuccessful){
                    dialog.dismiss()

                    //load token
                    FirebaseDatabase.getInstance()
                        .getReference(Common.TOKEN_REF)
                        .child(shipperModel.key!!)
                        .addListenerForSingleValueEvent(object:ValueEventListener{
                            override fun onCancelled(p0: DatabaseError) {
                                dialog.dismiss()
                                Toast.makeText(context!!,""+p0.message,Toast.LENGTH_LONG).show()
                            }

                            override fun onDataChange(dataSnapShot: DataSnapshot) {
                                if (dataSnapShot.exists()){

                                    val tokenModel = dataSnapShot.getValue(TokenModel::class.java)
                                    val notiData = HashMap<String,String>()

                                    notiData.put(Common.NOTI_TITLE,"Tienes nueva orden")
                                    notiData.put(Common.NOTI_CONTENT,StringBuilder("tienes un nuevo pedido que necesitas enviar")
                                        .append(orderModel.userPhone).toString())



                                    val sendData = FCMSendData(tokenModel!!.token!!,notiData)
                                    compositeDisposable.add(
                                        ifcService.sendNotification(sendData)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({
                                                    fcmResponse->
                                                dialog.dismiss()
                                                if (fcmResponse.success == 1){
                                                    updateOrder(pos,orderModel,1)
                                                    //Toast.makeText(context,"Orden Actualizado con exito",Toast.LENGTH_LONG).show()
                                                }else{
                                                    Toast.makeText(context!!,"Fallo el envio de notificacion",Toast.LENGTH_LONG).show()
                                                }

                                            }, {
                                                dialog.dismiss()
                                                Toast.makeText(context!!,""+it.message,Toast.LENGTH_LONG).show()

                                            })
                                    )

                                }else {
                                    dialog.dismiss()
                                    Toast.makeText(context!!,"Token no encontrado",Toast.LENGTH_LONG).show()
                                }

                            }

                        })

                }
            }

    }


    private fun updateOrder(pos: Int, orderModel: OrderModel, status: Int) {
        if (!TextUtils.isEmpty(orderModel.key)){
            val update_data= HashMap<String,Any>()
            update_data.put("orderStatus",status)

            FirebaseDatabase.getInstance()
                .getReference(Common.ORDER_REF)
                .child(orderModel.key!!)
                .updateChildren(update_data)
                .addOnFailureListener{
                    Toast.makeText(context,""+it.message,Toast.LENGTH_LONG).show()
                }
                .addOnSuccessListener {

                    //send message to client
                    val dialog = SpotsDialog.Builder().setContext(context!!).setCancelable(false).build()
                    dialog.show()
                    //load token
                    FirebaseDatabase.getInstance()
                        .getReference(Common.TOKEN_REF)
                        .child(orderModel.userId!!)
                        .addListenerForSingleValueEvent(object:ValueEventListener{
                            override fun onCancelled(p0: DatabaseError) {
                                dialog.dismiss()
                                Toast.makeText(context!!,""+p0.message,Toast.LENGTH_LONG).show()
                            }

                            override fun onDataChange(dataSnapShot: DataSnapshot) {
                                if (dataSnapShot.exists()){

                                    val tokenModel = dataSnapShot.getValue(TokenModel::class.java)
                                    val notiData = HashMap<String,String>()

                                    notiData.put(Common.NOTI_TITLE,"Tu orden ha sido actualizada")
                                    notiData.put(Common.NOTI_CONTENT,StringBuilder("Orden")
                                        .append(orderModel.key).append(" fue actualizado a")
                                        .append(Common.convertStatusToString(status)).toString())

                                    val sendData = FCMSendData(tokenModel!!.token!!,notiData)
                                    compositeDisposable.add(
                                        ifcService.sendNotification(sendData)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({
                                                    fcmResponse->
                                                dialog.dismiss()
                                                if (fcmResponse.success == 1){
                                                    Toast.makeText(context,"Orden Actualizado con exito",Toast.LENGTH_LONG).show()
                                                }else{
                                                    Toast.makeText(context!!,"Fallo el envio de notificacion",Toast.LENGTH_LONG).show()
                                                }

                                            }, {
                                                dialog.dismiss()
                                                Toast.makeText(context!!,""+it.message,Toast.LENGTH_LONG).show()

                                            })
                                    )

                                }else {
                                    dialog.dismiss()
                                    Toast.makeText(context!!,"Token no encontrado",Toast.LENGTH_LONG).show()
                                }

                            }

                        })

                    adapter!!.removeItem(pos)
                    adapter!!.notifyItemRemoved(pos)
                    updateTextCounter()

                }
        }else{
            Toast.makeText(context,"Numero de Orden no puede estar vacia o nula",Toast.LENGTH_LONG).show()
        }
    }

    private fun updateTextCounter() {
        txt_order_filter!!.setText(StringBuilder("Ordenes (")
            .append(adapter!!.itemCount).append(")"))

    }

    private fun deleteOrder(orderModel: OrderModel, pos: Int) {

        if (!TextUtils.isEmpty(orderModel.key)){
            FirebaseDatabase.getInstance()
                .getReference(Common.ORDER_REF)
                .child(orderModel.key!!)
                .removeValue()
                .addOnFailureListener{
                    Toast.makeText(context,""+it.message,Toast.LENGTH_LONG).show()
                }
                .addOnSuccessListener {
                    adapter!!.removeItem(pos)
                    adapter!!.notifyItemRemoved(pos)
                    updateTextCounter()
                    Toast.makeText(context,"Eliminado con exito",Toast.LENGTH_LONG).show()
                }
        }else{
            Toast.makeText(context,"Numero de Orden no puede estar vacia o nula",Toast.LENGTH_LONG).show()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.order_list_menu,menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_filter){
            val bottomSheet = BottomSheetOrderFragment.instance
            bottomSheet!!.show(requireActivity().supportFragmentManager,"OrderList")
            return true
        }
        else
            return super.onOptionsItemSelected(item)
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

        compositeDisposable.clear()
        super.onStop()
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(ChangeMenuClick(true))
        super.onDestroy()
    }


    //ESCUCHAMOS EL EVENTO DESDE EL BottomSheetOrderFragment
    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onLoadOrder(event:LoadOrderEvent){
        orderViewModel.loadOrder(event.status)
        (activity as AppCompatActivity).supportActionBar!!.title = Common.convertStatusToString(event.status)


    }

}