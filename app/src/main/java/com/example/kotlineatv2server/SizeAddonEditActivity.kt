package com.example.kotlineatv2server

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kotlineatv2server.adapter.MyAdapterSize
import com.example.kotlineatv2server.common.Common
import com.example.kotlineatv2server.eventbus.AddonSizeEditEvent
import com.example.kotlineatv2server.eventbus.SelectSizeModel
import com.example.kotlineatv2server.eventbus.ToasEvent
import com.example.kotlineatv2server.eventbus.UpdateSizeModel
import com.example.kotlineatv2server.model.SizeModel
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_size_addon_edit.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.StringBuilder

class SizeAddonEditActivity : AppCompatActivity() {

    //variables miembros
    var adapter:MyAdapterSize?=null
    var foodEditPosition = -1
    private var needSave = false
    private var isAddon = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_size_addon_edit)
       init()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_size_addon,menu)
        return true;
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId){
            R.id.action_save -> saveData()
            android.R.id.home ->{
                if (needSave){
                    val builder = AlertDialog.Builder(this)
                        .setTitle("Cancelar?")
                        .setMessage("Estas seguro(a) de salir sin guardar")
                        .setNegativeButton("CANCEL"){dialogInterface,_->dialogInterface.dismiss()}
                        .setPositiveButton("OK"){dialogInterface,i->
                            needSave = false
                            closeActivity()
                        }

                    val dialog = builder.create()
                    dialog.show()
                }else{
                    closeActivity()
                }
            }
        }


        return true
    }

    private fun saveData() {
        if (foodEditPosition != -1){

            Common.category_selected!!.foods!!.set(foodEditPosition,Common.foodModelSelected!!)
            val updateData : MutableMap<String,Any> = HashMap()
            updateData["foods"] = Common.category_selected!!.foods!!
            FirebaseDatabase.getInstance().getReference("Category")
                .child(Common.category_selected!!.menu_id!!)
                .updateChildren(updateData)
                .addOnFailureListener{
                    e:Exception ->
                    Toast.makeText(this,e.message,Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener{
                    task->
                    if (task.isSuccessful){
                        Toast.makeText(this,"Registro satisfactorio",Toast.LENGTH_SHORT).show()
                        needSave=false
                        edt_name.setText("")
                        edt_price.setText("0")
                    }
                }


        }


    }

    private fun closeActivity() {
        edt_name.setText("")
        edt_price.setText("0")
        needSave = false
        finish()

    }

    private fun init() {
        setSupportActionBar(tool_bar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        recycler_addon_size.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        recycler_addon_size!!.layoutManager = layoutManager
        recycler_addon_size.addItemDecoration(DividerItemDecoration(this,layoutManager.orientation))


        btn_create.setOnClickListener{

            if (!isAddon){ //SIZE

                if (adapter != null){
                    val sizeModel = SizeModel()
                    sizeModel.name = edt_name.text.toString()
                    sizeModel.price = edt_price.text.toString().toLong()
                    adapter!!.addNewSize(sizeModel)

                }

            }else{// ADDON

            }

        }

        btn_edit.setOnClickListener{
            if (!isAddon)//size
            {

                if (adapter != null){
                    val sizeModel = SizeModel()
                    sizeModel.name = edt_name.text.toString()
                    sizeModel.price = edt_price.text.toString().toLong()
                    adapter!!.editSize(sizeModel)
                }

            }else{//addon

            }

        }



    }

    override fun onStart() {
        super.onStart()
        if(!EventBus.getDefault().isRegistered(this))
             EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().removeStickyEvent(UpdateSizeModel::class.java)
        super.onStop()
    }

    //ESCUCHAMOS EL EVENTO ENVIADO DESDE FRAGMENT LIST
    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onAddonSizeEditRecieve(event:AddonSizeEditEvent){
        if (!event.isAddon) //size
        {
            if(Common.foodModelSelected!!.size != null){
                adapter = MyAdapterSize(this,Common.foodModelSelected!!.size.toMutableList())
                foodEditPosition = event.pos
                recycler_addon_size!!.adapter = adapter



            }
        }

    }

    //ESCUCHAMOS UNO DE LOS DOS EVENTOS ENVIADO DESDE EL ADAPTADOR CUANDO SE ACTUALIZA EL SIZE
    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onSizeModelUpdate(event:UpdateSizeModel){

        if (event.sizeModelList != null){ //size
            needSave = true
            Common.foodModelSelected!!.size = event.sizeModelList!! //update

        }

    }


    //ESCUCHAMOS UNO DE LOS DOS EVENTOS ENVIADO DESDE EL ADAPTADOR CUANDO SE SELECCIONA UN ITEM DEL RECYCLER
    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onSelectSize(event:SelectSizeModel){

        if (event.sizeModel != null){
            edt_name.setText(event.sizeModel.name)
            edt_price.setText(event.sizeModel.price.toString())
            btn_edit.isEnabled = true
        }else
            btn_edit.isEnabled = false

    }

}
