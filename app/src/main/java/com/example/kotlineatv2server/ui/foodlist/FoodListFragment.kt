package com.example.kotlineatv2server.ui.foodlist

import android.annotation.SuppressLint
import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kotlineatv2server.R
import com.example.kotlineatv2server.SizeAddonEditActivity
import com.example.kotlineatv2server.adapter.MyFoodListAdapter
import com.example.kotlineatv2server.callback.IMyButtonCallback
import com.example.kotlineatv2server.common.Common
import com.example.kotlineatv2server.common.MySwipeHelper
import com.example.kotlineatv2server.common.SwipeToDeleteCallback
import com.example.kotlineatv2server.eventbus.AddonSizeEditEvent
import com.example.kotlineatv2server.eventbus.ChangeMenuClick
import com.example.kotlineatv2server.eventbus.ToasEvent
import com.example.kotlineatv2server.model.CategoryModel
import com.example.kotlineatv2server.model.FoodModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dmax.dialog.SpotsDialog
import org.greenrobot.eventbus.EventBus
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FoodListFragment : Fragment() {

    private lateinit var foodListViewModel: FoodListViewModel
    var recycler_food_list:RecyclerView?=null
    var foodModels:List<FoodModel> = ArrayList<FoodModel>()
    var layoutAnimationController:LayoutAnimationController?=null
    var adapter:MyFoodListAdapter?=null

    internal lateinit var storage: FirebaseStorage
    internal lateinit var storageRef: StorageReference
    internal lateinit var img_food:ImageView
    private var imageUri: Uri?=null
    private lateinit var dialog: android.app.AlertDialog

    companion object{
        val PICK_IMAGE_FOOD_REQUEST =4444
    }


    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.food_list_menu,menu)


        //create search view
        val menuItem = menu.findItem(R.id.action_search)
        val searchManager = activity!!.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menuItem.actionView as androidx.appcompat.widget.SearchView

        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName!!))
        searchView.setOnQueryTextListener(object:androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(search: String?): Boolean {
                startSearchFood(search)
               return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false

            }

        })
        //clear txt when click clear button
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
            foodListViewModel.getMutableFoodModelListData().value = Common.category_selected!!.foods

        }


    }

    private fun startSearchFood(search: String?) {
        val resultFood: MutableList<FoodModel> = ArrayList()
        for (i in Common.category_selected!!.foods!!.indices){

            val foodModel = Common.category_selected!!.foods!![i]

            if (foodModel.name!!.toLowerCase().contains(search!!.toLowerCase())){
                //aquí guardaremos el índice del elemento de resultado de búsqueda
                foodModel.positionList = i
                resultFood.add(foodModel)
            }
        }

        //update search result
        foodListViewModel!!.getMutableFoodModelListData().value = resultFood

    }




    @SuppressLint("UseRequireInsteadOfGet", "FragmentLiveDataObserve")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        foodListViewModel =
            ViewModelProviders.of(this).get(FoodListViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_food_list, container, false)
        initView(root)

        foodListViewModel.getMutableFoodModelListData().observe(this, Observer {
                foodModels = it
                adapter = MyFoodListAdapter(context!!,it)
                recycler_food_list!!.adapter = adapter
                //recycler_food_list!!.layoutAnimation = layoutAnimationController


        })

        return root
    }


    private fun initView(root: View?) {

        setHasOptionsMenu(true)

        dialog = SpotsDialog.Builder().setContext(context).setCancelable(false).build()

        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        recycler_food_list = root!!.findViewById(R.id.recycler_food_list) as RecyclerView
        recycler_food_list!!.setHasFixedSize(true)
        recycler_food_list!!.layoutManager = LinearLayoutManager(context)

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(context,R.anim.layout_item_from_left)

        (activity as AppCompatActivity).supportActionBar!!.title = Common.category_selected!!.name

        val swipeHandler = @SuppressLint("UseRequireInsteadOfGet")
        object : SwipeToDeleteCallback(context!!) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = recycler_food_list!!.adapter as MyFoodListAdapter
                dialogQuestion(viewHolder.adapterPosition)
                Common.foodModelSelected = foodModels[viewHolder.adapterPosition]
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recycler_food_list)
    }


    @SuppressLint("UseRequireInsteadOfGet")
    fun dialogQuestion(pos:Int){
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Elegir una opcion")
        builder.setCancelable(false)
        val itemview = LayoutInflater.from(context).inflate(R.layout.layout_dialog_question,null)
        val rdb_editar = itemview.findViewById<View>(R.id.rdb_editar_food)as RadioButton
        val rdb_eliminar = itemview.findViewById<View>(R.id.rdb_eliminar_food)as RadioButton
        val rdb_edit_size = itemview.findViewById<View>(R.id.rdb_editar_size) as RadioButton
        val rdb_edit_addon = itemview.findViewById<View>(R.id.rdb_editar_addon) as RadioButton

        builder.setNegativeButton("CANCELAR"){dialogInterface,_->dialogInterface.dismiss()
            foodListViewModel.getMutableFoodModelListData().value = Common.category_selected!!.foods
        }


        builder.setPositiveButton("ACEPTAR"){dialogInterface,_->
                //dialogInterface.dismiss()


             if (rdb_editar.isChecked){
                 val foodModel = adapter!!.getItemAtPosition(pos)
                 if (foodModel.positionList == -1)
                     showDialogEditar(pos,foodModel)
                 else
                     showDialogEditar(foodModel.positionList,foodModel)

             }else if(rdb_eliminar.isChecked){
                 val foodModel = adapter!!.getItemAtPosition(pos)
                 if (foodModel.positionList == -1)
                     Common.category_selected!!.foods!!.removeAt(pos)
                 else
                     Common.category_selected!!.foods!!.removeAt(foodModel.positionList)
                 updateFood(Common.category_selected!!.foods,true)

             }else if(rdb_edit_size.isChecked){

                 val foodModel = adapter!!.getItemAtPosition(pos)
                 if (foodModel.positionList == -1)
                     Common.foodModelSelected = foodModels!![pos]
                 else
                     Common.foodModelSelected = foodModel
                 startActivity(Intent(context!!,SizeAddonEditActivity::class.java))
                 if (foodModel.positionList == -1)
                     EventBus.getDefault().postSticky(AddonSizeEditEvent(false,pos))
                 else
                     EventBus.getDefault().postSticky(AddonSizeEditEvent(false,foodModel.positionList))


             }else if(rdb_edit_addon.isChecked){

                 val foodModel = adapter!!.getItemAtPosition(pos)
                 if (foodModel.positionList == -1)
                     Common.foodModelSelected = foodModels!![pos]
                 else
                     Common.foodModelSelected = foodModel
                 startActivity(Intent(context!!,SizeAddonEditActivity::class.java))
                 if (foodModel.positionList == -1)
                     EventBus.getDefault().postSticky(AddonSizeEditEvent(true,pos))
                 else
                     EventBus.getDefault().postSticky(AddonSizeEditEvent(true,foodModel.positionList))



             }else{
                 Toast.makeText(context,"Seleccione una opcion",Toast.LENGTH_SHORT).show()


             }

            }

        builder.setView(itemview)
        val dialog = builder.create()
        dialog.show()
    }


    @SuppressLint("UseRequireInsteadOfGet")
    private fun showDialogEditar(pos:Int,foodModel:FoodModel) {

        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Editar")
        builder.setMessage("Por favor completar la informacion")
        val itemView = LayoutInflater.from(context!!).inflate(R.layout.layout_update_food,null,false)
        val edt_food_name = itemView.findViewById<View>(R.id.edt_food_name) as EditText
        val edt_food_price = itemView.findViewById<View>(R.id.edt_food_price) as EditText
        val edt_food_desciption = itemView.findViewById<View>(R.id.edt_food_desciption) as EditText
        img_food = itemView.findViewById<View>(R.id.img_food) as ImageView

        edt_food_name.setText(foodModel.name)
        edt_food_price.setText(foodModel.price.toString())
        edt_food_desciption.setText(foodModel.description)
        Glide.with(context!!).load(foodModel.image).into(img_food)
        img_food.setOnClickListener{
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent,"Seleccione una imagen"),
                PICK_IMAGE_FOOD_REQUEST)
        }


        builder.setNegativeButton("CANCELAR"){dialogInterface,_->
            dialog.dismiss()
            dialogInterface.dismiss()
        }
        builder.setPositiveButton("ACEPTAR"){dialogInterface,_->
           val foodUpdate = foodModel
            foodUpdate.name = edt_food_name.text.toString()
            foodUpdate.price = edt_food_price.text.toString().toLong()
            foodUpdate.description = edt_food_desciption.text.toString()



            if (imageUri!=null){

                dialog.setMessage("Actualizando..")
                dialog.show()
                val imageName = UUID.randomUUID().toString()
                val imageFolder = storageRef.child("images/$imageName")
                imageFolder.putFile(imageUri!!)
                    .addOnFailureListener{
                            e->
                        dialog.dismiss()
                        Toast.makeText(context,""+e.message,Toast.LENGTH_SHORT).show()
                    }
                    .addOnProgressListener{
                            taskSnapshot->
                        val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                        dialog.setMessage("Uploades $progress")
                    }
                    .addOnCompleteListener{
                        dialogInterface.dismiss()
                        imageFolder.downloadUrl.addOnSuccessListener {
                                uri->
                            dialog.dismiss()
                            foodUpdate.image = uri.toString()
                            Common.category_selected!!.foods!![pos] = foodUpdate
                            updateFood(Common.category_selected!!.foods!!,false)

                        }
                    }



            }else{
                Common.category_selected!!.foods!![pos] = foodUpdate
                updateFood(Common.category_selected!!.foods!!,false)
            }


        }
        builder.setView(itemView)
        val dialog = builder.create()
        dialog.show()



    }



    @SuppressLint("UseRequireInsteadOfGet")
    private fun updateFood(foods: MutableList<FoodModel>?,isDelete:Boolean) {
        val updateData = HashMap<String,Any>()
        updateData["foods"] = foods!!
        FirebaseDatabase.getInstance().getReference(Common.CATEGORY_REFERENCE)
            .child(Common.category_selected!!.menu_id!!)
            .updateChildren(updateData)
            .addOnFailureListener{e->
                Toast.makeText(context,""+e.message,Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener{task->
                 if (task.isSuccessful){
                     foodListViewModel.getMutableFoodModelListData().observe(this, Observer {
                             foodModels = it
                             adapter = MyFoodListAdapter(context!!,it)
                             recycler_food_list!!.adapter = adapter
                            // recycler_food_list!!.layoutAnimation = layoutAnimationController
                     })
                     EventBus.getDefault().postSticky(ToasEvent(!isDelete,true))
                     imageUri = null
                 }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_FOOD_REQUEST){
            if (resultCode ==  Activity.RESULT_OK){
                if (data != null && data.data != null){
                    imageUri = data.data
                    img_food.setImageURI(imageUri)

                }

            }
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(ChangeMenuClick(true))
        super.onDestroy()
    }
}