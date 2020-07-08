package com.example.kotlineatv2server

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.kotlineatv2server.callback.ISingleShippingOrderCallbackListener
import com.example.kotlineatv2server.common.Common
import com.example.kotlineatv2server.model.ShippingOrderModel
import com.example.kotlineatv2server.remote.IGoogleApi
import com.example.kotlineatv2server.remote.RetrofitGoogleClient

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.collection.LLRBNode
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject

class TrackingOrderActivity : AppCompatActivity(), OnMapReadyCallback,
    ISingleShippingOrderCallbackListener {

    private lateinit var mMap: GoogleMap
    private var shipperMarker: Marker?=null
    private var iSingleShippingOrderCallbackListener:ISingleShippingOrderCallbackListener?=null

    //GOOGLE  API
    private lateinit var iGoogleApi: IGoogleApi
    private var polylineOptions:PolylineOptions?=null
    private var blackPolylineOptions:PolylineOptions?=null
    private var blackPolyline:Polyline?=null
    private var graykPolyline:Polyline?=null
    private var redPolyline:Polyline?=null
    private val compositeDisposable = CompositeDisposable()
    private var polylineList:List<LatLng> = ArrayList<LatLng>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking_order)

        initView()
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun initView() {
        iGoogleApi = RetrofitGoogleClient.instance!!.create(IGoogleApi::class.java)
        iSingleShippingOrderCallbackListener = this

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap!!.uiSettings.isZoomControlsEnabled = true

        try {
            val success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.uber_light_with_label))
           if (!success)
               Log.d("ErrorStyle","Fallo la config del estilo de mapas")
        }catch (ex:Resources.NotFoundException){
            Log.d("ErrorStyle","No se encontro el json string para estilo de mapas")

        }
        checkOrderFromFirebase()
    }

    private fun checkOrderFromFirebase() {
        FirebaseDatabase.getInstance().getReference(Common.SHIPPING_ORDER_REF)
            .child(Common.currentOrderSelected!!.orderNumber!!)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                  Toast.makeText(this@TrackingOrderActivity,""+p0.message,Toast.LENGTH_LONG).show()
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                       // Toast.makeText(this@TrackingOrderActivity,"Hay una orden para hacer tracking",Toast.LENGTH_LONG).show()
                      val shippingOrderModel = p0.getValue(ShippingOrderModel::class.java)
                        shippingOrderModel!!.key = p0.key
                        iSingleShippingOrderCallbackListener!!.onSingleShippingOrderSuccess(shippingOrderModel)


                    }else{
                        Toast.makeText(this@TrackingOrderActivity,"Orden no encontrada",Toast.LENGTH_LONG).show()

                    }

                }

            })

    }

    override fun onSingleShippingOrderSuccess(shippingOrderModel: ShippingOrderModel) {

        val locationOrder = LatLng(shippingOrderModel!!.orderModel!!.lat,
            shippingOrderModel!!.orderModel!!.lng)

        val locationShipper = LatLng(shippingOrderModel!!.currentLat,
            shippingOrderModel!!.currentLng)

        //addbox
        mMap.addMarker(MarkerOptions()
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.box))
            .title(shippingOrderModel.orderModel!!.userName)
            .snippet(shippingOrderModel.orderModel!!.shippingAddress)
            .position(locationOrder))

        //addShipper
        if (shipperMarker == null)
        {
            val height=80
            val width=80
            val bitmapDrawable = ContextCompat.getDrawable(this,R.drawable.shippernew) as BitmapDrawable
            val resized = Bitmap.createScaledBitmap(bitmapDrawable.bitmap,width,height,false)

            shipperMarker = mMap.addMarker(MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(resized))
                .title(shippingOrderModel.shipperName)
                .snippet(shippingOrderModel!!.shipperPhone)
                .position(locationShipper))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationShipper,18f))

        }else
        {
            shipperMarker!!.position = locationShipper
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationShipper,18f))

        }


        //draw route
        val to = StringBuilder().append(shippingOrderModel.orderModel!!.lat)
            .append(",")
            .append(shippingOrderModel.orderModel!!.lng)
            .toString()

        val hasta = LatLng(shippingOrderModel.orderModel!!.lat,shippingOrderModel.orderModel!!.lng)
        val desde = LatLng(shippingOrderModel.currentLat,shippingOrderModel.currentLng)

        val from = StringBuilder().append(shippingOrderModel.currentLat)
            .append(",")
            .append(shippingOrderModel.currentLng)
            .toString()

       /* mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                hasta,18f
            )
        )
        mMap.addPolyline(PolylineOptions().add(hasta)
            .add(desde)
            .width(8f)
            .color(Color.RED))*/

        compositeDisposable.add(iGoogleApi!!.getDirections("driving","less_driving",
            from,to,getString(R.string.google_maps_key))!!
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {s->

                    try {
                        val jsonObject = JSONObject(s)
                        val jsonArray = jsonObject.getJSONArray("routes")
                        for(i in 0 until jsonArray.length())
                        {
                            val route = jsonArray.getJSONObject(i)
                            val poly = route.getJSONObject("overview_polyline")
                            val polyline = poly.getString("points")
                            polylineList = Common.decodePoly(polyline)
                        }

                        polylineOptions = PolylineOptions()
                        polylineOptions!!.color(Color.BLUE)
                        polylineOptions!!.width(8f)

                        polylineOptions!!.addAll(polylineList)
                        mMap.addPolyline(polylineOptions)


                    }catch (e: Exception){
                        Log.d("DEBUG",e.message)

                    }

                },{
                    Toast.makeText(this,""+it.message, Toast.LENGTH_SHORT).show()

                }))


    }

}


