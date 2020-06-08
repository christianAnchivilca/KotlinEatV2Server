package com.example.kotlineatv2server.ui.shipper

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.SearchManager
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.kotlineatv2server.adapter.MyShipperAdapter
import com.example.kotlineatv2server.common.Common
import com.example.kotlineatv2server.eventbus.ChangeMenuClick
import com.example.kotlineatv2server.eventbus.UpdateActiveEvent
import com.example.kotlineatv2server.model.ShipperModel
import com.example.kotlineatv2server.ui.order.OrderViewModel
import com.google.firebase.database.FirebaseDatabase
import dmax.dialog.SpotsDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

import android.view.*
import android.widget.EditText
import android.widget.ImageView
import com.example.kotlineatv2server.R
import com.google.firebase.database.core.Context


class ShipperFragment:Fragment() {
    private lateinit var shipperViewModel: ShipperViewModel
    private var recycler_shipper:RecyclerView?=null
    private var shipperModelsList:List<ShipperModel> = ArrayList<ShipperModel>()
    private var adapter:MyShipperAdapter?=null
    private lateinit var dialog: AlertDialog
    private lateinit var layoutAnimationControler: LayoutAnimationController
     var saveBeforeSearchList:List<ShipperModel> = ArrayList<ShipperModel>()



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        shipperViewModel = ViewModelProviders.of(this).get(ShipperViewModel::class.java)
        val root = inflater.inflate(com.example.kotlineatv2server.R.layout.fragment_shipper, container, false)
        init(root)

        shipperViewModel.getMessageError().observe(this, Observer {
            Toast.makeText(context,""+it,Toast.LENGTH_LONG).show()
        })
        shipperViewModel.getShipperList().observe(this, Observer { lista->
            dialog.dismiss()
            shipperModelsList = lista
            saveBeforeSearchList = lista
            adapter = MyShipperAdapter(context!!,lista)
            recycler_shipper!!.adapter = adapter
            recycler_shipper!!.layoutAnimation = layoutAnimationControler
        })

        return root
    }

    private fun init(root: View) {
        setHasOptionsMenu(true)
        dialog = SpotsDialog.Builder().setContext(context).setCancelable(false).build()
        dialog.show()
        layoutAnimationControler = AnimationUtils.loadLayoutAnimation(context,R.anim.layout_item_from_left)
        recycler_shipper = root.findViewById(R.id.recycler_shipper) as RecyclerView
        recycler_shipper!!.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        recycler_shipper!!.layoutManager = layoutManager

    }


    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.food_list_menu,menu)
        //create search view
        val menuItem = menu.findItem(R.id.action_search)
        val searchManager = activity!!.getSystemService(android.content.Context.SEARCH_SERVICE) as SearchManager
        val searchView = menuItem.actionView as androidx.appcompat.widget.SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName!!))

        searchView.setOnQueryTextListener(object:androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(search: String?): Boolean {
                startSearchShipper(search)
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })
        //clear text when click to clear button
        val closeButton = searchView.findViewById<View>(R.id.search_close_btn) as ImageView
        closeButton.setOnClickListener{
            val ed = searchView.findViewById<View>(R.id.search_src_text) as EditText
            //clear text
            ed.setText("")
            //clear query
            searchView.setQuery("",false)
            //collapse action view
            searchView.onActionViewCollapsed()
            //collapse the search widget
            menuItem.collapseActionView()
            //restore result to original
            shipperViewModel.loadShippers()
        }


    }

    private fun startSearchShipper(search: String?) {
        val resultShipper: MutableList<ShipperModel> = ArrayList()
        for (i in shipperModelsList.indices){

            val shipperModel = shipperModelsList!![i]
            if (shipperModel.phone!!.toLowerCase().contains(search!!.toLowerCase())){

                resultShipper.add(shipperModel)
            }
        }

        //update search result
        shipperViewModel!!.getShipperList().value = resultShipper

    }



    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }



    override fun onStop() {

        if (EventBus.getDefault().hasSubscriberForEvent(UpdateActiveEvent::class.java))
             EventBus.getDefault().removeStickyEvent(UpdateActiveEvent::class.java)

        if (EventBus.getDefault().isRegistered(this))
             EventBus.getDefault().unregister(this)

        super.onStop()
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(ChangeMenuClick(true))
        super.onDestroy()
    }

    //ESCUCHAMOS EL EVENTO ENVIADO DESDE EL ADAPTADOR MyShipperAdapter
    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onUpdateActiveEvent(event:UpdateActiveEvent){

        val updateData = HashMap<String,Any>()
        updateData.put("active",event.active)
        FirebaseDatabase.getInstance().getReference(Common.SHIPPER_REF)
            .child(event.shipperModel.key!!)
            .updateChildren(updateData)
            .addOnFailureListener{e->Toast.makeText(getContext(),""+e.message,Toast.LENGTH_LONG).show()}
            .addOnSuccessListener {
                Toast.makeText(getContext(),""+Common.getStatusShipper(event.active),Toast.LENGTH_LONG).show()
            }

    }
}