package com.example.kotlineatv2server

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.navigation.NavController
import com.example.kotlineatv2server.common.Common

import com.example.kotlineatv2server.eventbus.CategoryClick
import com.example.kotlineatv2server.eventbus.ChangeMenuClick
import com.example.kotlineatv2server.eventbus.ToasEvent
import com.example.kotlineatv2server.model.FCMResponse
import com.example.kotlineatv2server.model.FCMSendData
import com.example.kotlineatv2server.remote.IFCMService
import com.example.kotlineatv2server.remote.RetrofitFCMClient
import com.google.android.gms.auth.api.signin.internal.Storage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.HashMap

class HomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController
    private lateinit var navView:NavigationView
    private var menuClick:Int = -1

    private var img_upload:ImageView?=null
    private val compositeDisposable = CompositeDisposable()
    private lateinit var ifcmService:IFCMService
    private var imgUri:Uri?=null
    private lateinit var storage:FirebaseStorage
    private var storageRef:StorageReference?=null
    private val PICK_IMAGE_REQUEST = 1212

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        ifcmService=RetrofitFCMClient.getInstance().create(IFCMService::class.java)
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference
        subscribeToTopic(Common.getNewOrderTopic())
        updateToken()
        drawerLayout = findViewById(R.id.drawer_layout)
        navView= findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_category,R.id.nav_food_list,R.id.nav_order,R.id.nav_shipper
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.setNavigationItemSelectedListener(object :NavigationView.OnNavigationItemSelectedListener{
            override fun onNavigationItemSelected(menu: MenuItem): Boolean {
                menu.isChecked = true
                drawerLayout!!.closeDrawers()
                if (menu.itemId == R.id.nav_sign_out){
                    signOut()
                }
                else if(menu.itemId == R.id.nav_category)
                {
                    if (menuClick != menu.itemId)
                    {
                        navController.popBackStack()//clear back stack
                        navController.navigate(R.id.nav_category)
                    }

                }
                else if(menu.itemId == R.id.nav_order){

                    if (menuClick != menu.itemId)
                    {
                        navController.popBackStack()//clear back stack
                        navController.navigate(R.id.nav_order)
                    }


                }
                else if(menu.itemId == R.id.nav_shipper){

                    if (menuClick != menu.itemId)
                    {
                        navController.popBackStack()//clear back stack
                        navController.navigate(R.id.nav_shipper)
                    }


                }
                else if(menu.itemId == R.id.nav_best_deal){

                    if (menuClick != menu.itemId)
                    {
                        navController.popBackStack()//clear back stack
                        navController.navigate(R.id.nav_best_deal)
                    }


                }
                else if(menu.itemId == R.id.nav_most_popular){

                    if (menuClick != menu.itemId)
                    {
                        navController.popBackStack()//clear back stack
                        navController.navigate(R.id.nav_most_popular)
                    }


                }

                else if(menu.itemId == R.id.nav_news){
                    showSendNewDialog()
                }

                menuClick = menu.itemId


                return true
            }

        })

        //view
        val headerView = navView.getHeaderView(0)
        val txt_user = headerView.findViewById<View>(R.id.txt_user) as TextView
        Common.setSpanString("Hey ",Common.currentServerUser!!.name!!.toString(),txt_user)

        menuClick = R.id.nav_category  // default
        //recibir el intent que llega desde MainActivity <- MyFCMServices
        checkOpenOrderFragment()

    }

    private fun showSendNewDialog() {
        val builder = AlertDialog.Builder(this)
            builder.setTitle("Nuevo Envio")
                .setMessage("Enviar nueva notificacion a todos los clientes")
         val itemView=LayoutInflater.from(this).inflate(R.layout.layout_news_system,null)
        builder.setView(itemView)
        val edt_title=itemView.findViewById<View>(R.id.edt_title) as EditText
        val edt_content=itemView.findViewById<View>(R.id.edt_content) as EditText
        val edt_link=itemView.findViewById<View>(R.id.edt_link) as EditText
         img_upload=itemView.findViewById<View>(R.id.img_upload) as ImageView
        val rdi_none =itemView.findViewById<View>(R.id.rdi_none) as RadioButton
        val rdi_link =itemView.findViewById<View>(R.id.rdi_link) as RadioButton
        val rdi_upload =itemView.findViewById<View>(R.id.rdi_image) as RadioButton

        rdi_none.setOnClickListener {
            edt_link.visibility=View.GONE
            img_upload!!.visibility = View.GONE
        }
        rdi_link.setOnClickListener {
            edt_link.visibility=View.VISIBLE
            img_upload!!.visibility = View.GONE
        }
        rdi_upload.setOnClickListener {
            edt_link.visibility=View.GONE
            img_upload!!.visibility = View.VISIBLE
        }
        img_upload!!.setOnClickListener{
             val intent = Intent()
             intent.type="image/*"
             intent.action = Intent.ACTION_GET_CONTENT
             startActivityForResult(Intent.createChooser(intent,"Seleccione una imagen"),PICK_IMAGE_REQUEST)

        }
        builder.setNegativeButton("CANCELAR"){dialog: DialogInterface, which: Int ->dialog.dismiss()}
        builder.setPositiveButton("OK"){dialog: DialogInterface, which: Int ->

            if (rdi_none.isChecked)
                sendNews(edt_title.text.toString(),edt_content.text.toString())
            else if (rdi_link.isChecked)
                sendNews(edt_title.text.toString(),edt_content.text.toString(),edt_link.text.toString())
            else if (rdi_upload.isChecked)
            {
                if (imgUri != null)
                {
                    val dialog= AlertDialog.Builder(this).setMessage("Cargando..").create()
                    dialog.show()
                    val file_name = UUID.randomUUID().toString()
                    val newsImage = storageRef!!.child("news/$file_name")
                    newsImage.putFile(imgUri!!)
                        .addOnFailureListener { e->dialog.dismiss()
                            Toast.makeText(this,""+e.message,Toast.LENGTH_LONG).show()
                        }.addOnSuccessListener {
                            dialog.dismiss()
                            newsImage.downloadUrl.addOnSuccessListener { uri->
                                sendNews(edt_title.text.toString(),edt_content.text.toString(),uri.toString())
                            }
                        }.addOnProgressListener {taskSnapshot->
                            val progress = Math.round(100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toDouble()
                            dialog.setMessage(StringBuilder("Cargando $progress %"))
                        }


                }else{
                    Toast.makeText(this,"Seleccione una imagen",Toast.LENGTH_LONG).show()
                }

            }
        }


        val dialog= builder.create()
        dialog.show()

    }

    private fun sendNews(title: String, content: String,url:String) {
        val notificationData:MutableMap<String,String> = HashMap()
        notificationData[Common.NOTI_TITLE]=title
        notificationData[Common.NOTI_CONTENT]=content
        notificationData[Common.IS_SEND_IMAGE]="true"
        notificationData[Common.IMAGE_URL] = url

        val fcmSendData= FCMSendData(Common.getNewsTopic(),notificationData)
        val dialog = AlertDialog.Builder(this)
            .setMessage("Esperando..").create()
        dialog.show()
        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe({t:FCMResponse->
                dialog.dismiss()
                if (t!!.message_id != 0L){
                    Toast.makeText(this,"Enviado con exito",Toast.LENGTH_LONG).show()
                    imgUri = null
                }

                else{
                    Toast.makeText(this,"Fallo en el envio",Toast.LENGTH_LONG).show()
                }

            },{
                    it->
                dialog.dismiss()
                Toast.makeText(this,"[FCM-ERROR]"+it.message,Toast.LENGTH_LONG).show()
            }))


    }
    private fun sendNews(title: String, content: String) {
        val notificationData:MutableMap<String,String> = HashMap()
        notificationData[Common.NOTI_TITLE]=title
        notificationData[Common.NOTI_CONTENT]=content
        notificationData[Common.IS_SEND_IMAGE]="false"
        val fcmSendData= FCMSendData(Common.getNewsTopic(),notificationData)
        val dialog = AlertDialog.Builder(this)
            .setMessage("Esperando..").create()
        dialog.show()
        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe({t:FCMResponse->
                dialog.dismiss()
                if (t!!.message_id != 0L)
                    Toast.makeText(this,"Enviado con exito",Toast.LENGTH_LONG).show()
                else
                    Toast.makeText(this,"Fallo en el envio",Toast.LENGTH_LONG).show()


            },{
                it->
                dialog.dismiss()
                Toast.makeText(this,"[FCM-ERROR]"+it.message,Toast.LENGTH_LONG).show()
            }))


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK)
        {
            if (data != null && data.data != null){
                imgUri = data.data
                img_upload!!.setImageURI(imgUri)
            }

        }

    }

    private fun checkOpenOrderFragment() {
        val isOpenNewOrder = intent.extras.getBoolean(Common.IS_OPEN_ACTIVITY_NEW_ORDER,false)
        if (isOpenNewOrder){
            navController.popBackStack();
            navController.navigate(R.id.nav_order)
            menuClick = R.id.nav_order
        }
    }

    private fun updateToken(){
        FirebaseInstanceId.getInstance().instanceId
            .addOnFailureListener{
                Toast.makeText(this,""+it.message,Toast.LENGTH_LONG).show()
            }
            .addOnSuccessListener{
                    result->
                Common.updateToken(this@HomeActivity,result.token,true,false)
            }
    }

    private fun subscribeToTopic(newOrderTopic: String) {

        FirebaseMessaging.getInstance()
            .subscribeToTopic(newOrderTopic)
            .addOnFailureListener{message ->
                Toast.makeText(this@HomeActivity,""+message.toString(),Toast.LENGTH_LONG).show()}
            .addOnCompleteListener {
                 task ->
                if (!task.isSuccessful){
                    Toast.makeText(this@HomeActivity,"Subscricion ha fallado",Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun signOut(){
        //ABRIMOS DIALOGO PARA PREGUNTAR SI DESEA CERRAR LA SESION
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Cerrar Sesion")
            .setMessage("¿Estas seguro de cerrar la sesion?")
            .setNegativeButton("cancelar",{dialogInterface,_ ->dialogInterface.dismiss()})
            .setPositiveButton("OK"){dialogInterface,_ ->
                Common.foodModelSelected = null
                Common.category_selected = null
                Common.currentServerUser = null
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this@HomeActivity,MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()

            }
        val dialog = builder.create()
        dialog.show()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onCategoryClick(event:CategoryClick){
        if (event.isSucces){

            if (menuClick != R.id.nav_food_list)
            {
                navController.navigate(R.id.nav_food_list)
                menuClick = R.id.nav_food_list

            }

        }

    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onChangeMenuEvent(event:ChangeMenuClick){
        if (!event.isFromFoodList){

            //clear
            navController.popBackStack(R.id.nav_category,true)
            navController.navigate(R.id.nav_category)
        }
        menuClick = -1

    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onToastEvent(event:ToasEvent){
        if (event.isUpdate){

            Toast.makeText(this@HomeActivity,"Actualizado con exito",Toast.LENGTH_SHORT).show()

        }else{
            Toast.makeText(this@HomeActivity,"Eliminado con exito",Toast.LENGTH_SHORT).show()
        }
        EventBus.getDefault().postSticky(ChangeMenuClick(event.isBackFromFoodList))


    }



}
