package com.example.kotlineatv2server

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.example.kotlineatv2server.common.Common
import com.example.kotlineatv2server.model.ServerUserModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.auth.api.Auth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import dmax.dialog.SpotsDialog
import java.util.*

class MainActivity : AppCompatActivity() {

    //VARIABLES MIEMBROS
    private var firebaseAuth:FirebaseAuth?=null
    private var listener:FirebaseAuth.AuthStateListener? = null
    private var dialog:AlertDialog?=null
    private var serverRef:DatabaseReference?=null
    private var providers:List<AuthUI.IdpConfig>? = null

    companion object{
        private val APP_REQUEST_CODE = 1212
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth!!.addAuthStateListener(listener!!)
    }

    override fun onStop() {
        firebaseAuth!!.removeAuthStateListener(listener!!)
        super.onStop()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        init()
    }

    private fun init(){
        providers = Arrays.asList<AuthUI.IdpConfig>(AuthUI.IdpConfig.PhoneBuilder().build())
        serverRef = FirebaseDatabase.getInstance().getReference(Common.SERVER_REF)
        firebaseAuth = FirebaseAuth.getInstance()
        dialog = SpotsDialog.Builder().setContext(this).setCancelable(false).build()

        listener = object :FirebaseAuth.AuthStateListener{

            //onAuthStateChanged ->La manera recomendada de obtener el usuario actual es establecer un observador en el objeto Auth
            override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
                /*
                * También puedes usar la propiedad currentUser para obtener el usuario que accedió.
                * Si no accedió ningún usuario, el valor de currentUser es null:*/
                val user = firebaseAuth.currentUser

                if (user != null)
                {
                     checkServerUserFromFirebase(user)


                }else
                {
                  phoneLogin()
                }

            }

        }


    }

    private fun checkServerUserFromFirebase(user: FirebaseUser) {
        dialog!!.show()
        serverRef!!.child(user!!.uid)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    dialog!!.dismiss()
                    Toast.makeText(this@MainActivity,""+error.message,Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(dataSnapShot: DataSnapshot) {

                    if (dataSnapShot.exists()){
                        val userModel = dataSnapShot.getValue(ServerUserModel::class.java)

                        if (userModel!!.isActive){
                            dialog!!.dismiss()
                            goToHomeActivity(userModel)

                        }else{
                            dialog!!.dismiss()
                            Toast.makeText(this@MainActivity,"Admin aun no activa su cuenta",Toast.LENGTH_SHORT).show()
                        }

                    }else{
                        dialog!!.dismiss()
                        showRegisterDialog(user)
                    }

                }

            })

    }

    private fun showRegisterDialog(user: FirebaseUser) {
        var builder = AlertDialog.Builder(this)
        builder.setTitle("Registrarme")
        builder.setMessage("Porfavor complete la informacion \n El Admin aceptara su cuenta despues")

        val itemView = LayoutInflater.from(this).inflate(R.layout.layout_dialog_register,null,false)
        val edt_name = itemView.findViewById<View>(R.id.edt_name) as EditText
        val edt_phone = itemView.findViewById<View>(R.id.edt_phone) as EditText
        //set data
        edt_phone.setText(user!!.phoneNumber)
        builder.setNegativeButton("CANCEL"){dialogInterface,_-> dialogInterface.dismiss()}
        builder.setPositiveButton("REGISTRAR"){dialogInterface,_->
            if (TextUtils.isEmpty(edt_name.text)){

                Toast.makeText(this,"El nombre no puede estar vacio",Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            val serverUserModel = ServerUserModel()
            serverUserModel.uid = user.uid
            serverUserModel.name=edt_name.text.toString()
            serverUserModel.phone=edt_phone.text.toString()
            serverUserModel.isActive = false

            dialog!!.show()
            serverRef!!.child(serverUserModel.uid!!).setValue(serverUserModel)
                .addOnFailureListener{
                    dialog!!.dismiss()
                    Toast.makeText(this,""+it.message,Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener{
                    task ->
                    dialog!!.dismiss()
                    Toast.makeText(this,"Registro satisfactorio !!,\n Admin lo verificara y lo activara pronto",Toast.LENGTH_SHORT).show()


                }


        }

        builder.setView(itemView)
        val registerDialog = builder.create()
        registerDialog.show()



    }

    private fun goToHomeActivity(user:ServerUserModel) {

        Common.currentServerUser = user
        val myIntent = Intent(this@MainActivity,HomeActivity::class.java)
        var isOpenActivityNewOrder = false
       if (intent != null && intent.extras != null)
           isOpenActivityNewOrder =  intent.extras.getBoolean(Common.IS_OPEN_ACTIVITY_NEW_ORDER,false)
        myIntent.putExtra(Common.IS_OPEN_ACTIVITY_NEW_ORDER,isOpenActivityNewOrder)
        startActivity(myIntent)
        finish()
    }

    private fun phoneLogin() {
        startActivityForResult(AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers!!).build(),APP_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == APP_REQUEST_CODE){
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK){
                val user = FirebaseAuth.getInstance().currentUser
            }else{
                Toast.makeText(this,"Failed to sign in",Toast.LENGTH_SHORT).show()
            }



        }
    }



}
