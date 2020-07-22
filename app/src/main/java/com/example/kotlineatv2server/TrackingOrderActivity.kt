package com.example.kotlineatv2server

import android.animation.ValueAnimator
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.animation.LinearInterpolator
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
import com.google.firebase.database.*
import com.google.firebase.database.collection.LLRBNode
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject

class TrackingOrderActivity : AppCompatActivity(), OnMapReadyCallback,
    ISingleShippingOrderCallbackListener, ValueEventListener {

    private lateinit var mMap: GoogleMap
    private var isInit:Boolean=false
    private var shipperMarker: Marker?=null
    private var iSingleShippingOrderCallbackListener:ISingleShippingOrderCallbackListener?=null

    private var currentShippingOrder:ShippingOrderModel?=null
    //referenceia a la base de datos de firebase
    private lateinit var shippingRef:DatabaseReference
    private var handler: Handler?=null
    private var index=0
    private var next:Int=0
    private var v=0f
    private var lat=0.0
    private var lng=0.0
    private var startPosition=LatLng(0.0,0.0)
    private var endPosition = LatLng(0.0,0.0)

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
        currentShippingOrder = shippingOrderModel
        subscribeShipperMove(currentShippingOrder!!)

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

        //val hasta = LatLng(shippingOrderModel.orderModel!!.lat,shippingOrderModel.orderModel!!.lng)
        //val desde = LatLng(shippingOrderModel.currentLat,shippingOrderModel.currentLng)

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

    private fun subscribeShipperMove(currentShippingOrder: ShippingOrderModel) {
        shippingRef = FirebaseDatabase.getInstance().getReference(Common.SHIPPING_ORDER_REF)
            .child(currentShippingOrder!!.key!!)
        shippingRef.addValueEventListener(this)


    }

    override fun onCancelled(p0: DatabaseError) {
        Toast.makeText(this,""+p0.message, Toast.LENGTH_SHORT).show()
    }

    override fun onDataChange(p0: DataSnapshot) {
        if (p0!!.exists()){
            //save old position
            val from = java.lang.StringBuilder()
                .append(currentShippingOrder!!.currentLat)
                .append(",")
                .append(currentShippingOrder!!.currentLng)
                .toString()
            currentShippingOrder = p0.getValue(ShippingOrderModel::class.java)
            val to = java.lang.StringBuilder()
                .append(currentShippingOrder!!.currentLat)
                .append(",")
                .append(currentShippingOrder!!.currentLng)
                .toString()

            if(isInit)moveMakerAnimation(shipperMarker,from,to) else isInit = true


        }


    }

    private fun moveMakerAnimation(marker: Marker?, from: String, to: String) {
        compositeDisposable.add(iGoogleApi!!.getDirections("driving",
            "less_driving",
            from.toString(),
            to.toString(),
            getString(R.string.google_maps_key))!!.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {s->
                    Log.d("DEBUG",s)
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
                        polylineOptions!!.color(Color.BLACK)
                        polylineOptions!!.width(5.0f)
                        polylineOptions!!.startCap(SquareCap())
                        polylineOptions!!.endCap(SquareCap())
                        polylineOptions!!.jointType(JointType.ROUND)
                        polylineOptions!!.addAll(polylineList)
                        graykPolyline= mMap!!.addPolyline(polylineOptions)

                        blackPolylineOptions = PolylineOptions()
                        blackPolylineOptions!!.color(Color.GRAY)
                        blackPolylineOptions!!.width(5.0f)
                        blackPolylineOptions!!.startCap(SquareCap())
                        blackPolylineOptions!!.endCap(SquareCap())
                        blackPolylineOptions!!.jointType(JointType.ROUND)
                        blackPolylineOptions!!.addAll(polylineList)
                        blackPolyline = mMap.addPolyline(blackPolylineOptions)

                        //Animator
                        val polylineAnimator = ValueAnimator.ofInt(0,100)
                        polylineAnimator.setDuration(2000)
                        polylineAnimator.setInterpolator(LinearInterpolator())
                        polylineAnimator.addUpdateListener {
                                valueAnimator: ValueAnimator ->
                            val points=graykPolyline!!.points
                            val porcentValue =Integer.parseInt(valueAnimator.animatedValue.toString())
                            val size = points.size
                            val newPoints = (size *(porcentValue /100.0f)).toInt()
                            val p = points.subList(0,newPoints)
                            blackPolyline!!.points = p

                        }

                        polylineAnimator.start()
                        //cart moving
                        index = -1
                        next = -1

                        val r = object:Runnable {
                            override fun run() {
                                if (index < polylineList.size - 1)
                                {
                                    index++
                                    next=index+1
                                    startPosition = polylineList[index]
                                }

                                val valueAnimator = ValueAnimator.ofInt(0,1)
                                valueAnimator.setDuration(1500)
                                valueAnimator.setInterpolator(LinearInterpolator())
                                valueAnimator.addUpdateListener{ valueAnimator->
                                    v=valueAnimator.animatedFraction
                                    lat = v * endPosition!!.latitude + (1-v)*startPosition!!.latitude
                                    lng = v * endPosition!!.longitude +(1-v) * startPosition!!.longitude
                                    val newPos = LatLng(lat,lng)
                                    marker!!.position = newPos
                                    marker!!.setAnchor(0.5f,0.5f)
                                    marker!!.rotation = Common.getBearing(startPosition!!,newPos)

                                    mMap!!.moveCamera(CameraUpdateFactory.newLatLng(marker.position))

                                }
                                valueAnimator.start()
                                if (index < polylineList.size - 2)
                                    handler!!.postDelayed(this,1500)
                            }


                        }

                        handler = Handler()
                        handler!!.postDelayed(r,1500)




                    }catch (e:Exception){
                        Log.d("DEBUG",e.message)

                    }

                },{
                    Toast.makeText(this,""+it.message,Toast.LENGTH_SHORT).show()

                }))



    }

    override fun onDestroy() {
        shippingRef.removeEventListener(this)
        isInit = false
        super.onDestroy()
    }


}


