package com.example.kotlineatv2server.ui.category

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kotlineatv2server.R
import com.example.kotlineatv2server.adapter.MyCategoriesAdapter
import com.example.kotlineatv2server.callback.IMyButtonCallback
import com.example.kotlineatv2server.common.Common
import com.example.kotlineatv2server.common.MySwipeHelper
import com.example.kotlineatv2server.eventbus.ToasEvent
import com.example.kotlineatv2server.model.CategoryModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dmax.dialog.SpotsDialog
import org.greenrobot.eventbus.EventBus
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CategoryFragment : Fragment() {

    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var dialog:AlertDialog
    private lateinit var layoutAnimationControler:LayoutAnimationController
    private var categoriesAdapter:MyCategoriesAdapter?=null
    private var recycler_category:RecyclerView?=null

    internal var categoryModels:List<CategoryModel> = ArrayList<CategoryModel>()
    internal lateinit var storage:FirebaseStorage
    internal lateinit var storageRef:StorageReference
    internal lateinit var img_category:ImageView
    private var imageUri:Uri?=null

    companion object{
        val PICK_IMAGE_REQUEST = 1212
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        categoryViewModel =
            ViewModelProviders.of(this).get(CategoryViewModel::class.java)
        val root = inflater.inflate(com.example.kotlineatv2server.R.layout.fragment_category, container, false)
        initView(root)
        categoryViewModel.getMessageError().observe(this, Observer {

        })

        categoryViewModel.getCategoryList().observe(this, Observer {
            dialog.dismiss()
            categoryModels = it
            val contexto = context
            categoriesAdapter = MyCategoriesAdapter(contexto!!,it)
            recycler_category!!.adapter = categoriesAdapter
            recycler_category!!.layoutAnimation = layoutAnimationControler
        })


        return root
    }

    private fun initView(root: View?) {

        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        dialog = SpotsDialog.Builder().setContext(context)
            .setCancelable(false).build()

        dialog.show()
        layoutAnimationControler = AnimationUtils.loadLayoutAnimation(context,com.example.kotlineatv2server.R.anim.layout_item_from_left)

        recycler_category = root!!.findViewById(com.example.kotlineatv2server.R.id.recycler_menu) as RecyclerView
        recycler_category!!.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(context)

        recycler_category!!.layoutManager = layoutManager
        recycler_category!!.addItemDecoration(DividerItemDecoration(context,layoutManager.orientation))

        val swipe = @SuppressLint("UseRequireInsteadOfGet")
        object:MySwipeHelper(context!!,recycler_category!!,200)
        {
            override fun instantiateMyButton(
                viewHolder: RecyclerView.ViewHolder,
                buffer: MutableList<MyButton>
            ) {
                buffer.add(MyButton(context!!,
                    "Editar",
                    30,
                    0,
                    Color.parseColor("#560027"),
                    object :IMyButtonCallback{
                        override fun onClick(pos: Int) {
                            Common.category_selected = categoryModels[pos]
                            showUpdateDialog()
                        }
                    }))
            }
        }

    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun showUpdateDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(context!!)
        builder.setTitle("Actualizacion")
        builder.setMessage("\n" +
                "por favor complete la información")
        val itemview = LayoutInflater.from(context).inflate(R.layout.layout_update_category,null)
        val edt_category_name = itemview.findViewById<View>(R.id.edt_category_name) as EditText
        img_category = itemview.findViewById<View>(R.id.img_category)as ImageView
        edt_category_name.setText(Common.category_selected!!.name)
        Glide.with(context!!).load(Common.category_selected!!.image).into(img_category)
        img_category.setOnClickListener{
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent,"Seleccione imagen"),PICK_IMAGE_REQUEST)
        }
        builder.setNegativeButton("CANCEL"){dialogInterface,_->dialogInterface.dismiss()}
        builder.setPositiveButton("OK"){dialogInterface,_->

            val updateData = HashMap<String,Any>()
            updateData["name"]=edt_category_name.text.toString()
            if (imageUri != null){

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
                            updateData["image"] = uri.toString()
                            updateCategory(updateData)
                        }
                    }

            } else {
                updateCategory(updateData)
            }
        }

        builder.setView(itemview)
        val contructor = builder.create()
        contructor.show()

    }

    private fun updateCategory(updateData: HashMap<String, Any>) {
        FirebaseDatabase.getInstance()
            .getReference(Common.CATEGORY_REFERENCE)
            .child(Common.category_selected!!.menu_id!!)
            .updateChildren(updateData)
            .addOnFailureListener{e->Toast.makeText(context,""+e.message,Toast.LENGTH_SHORT).show()}
            .addOnCompleteListener{task->
                categoryViewModel!!.loadCategory()
                EventBus.getDefault().postSticky(ToasEvent(true,false))
                imageUri = null
            }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST){
            if (resultCode == Activity.RESULT_OK){

                if (data != null && data.data != null){
                    imageUri = data.data
                    img_category.setImageURI(imageUri)

                }
            }
        }
    }

}