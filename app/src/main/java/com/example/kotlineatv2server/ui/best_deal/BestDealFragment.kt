package com.example.kotlineatv2server.ui.best_deal

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kotlineatv2server.R
import com.example.kotlineatv2server.adapter.MyBestDealAdapter
import com.example.kotlineatv2server.adapter.MyCategoriesAdapter
import com.example.kotlineatv2server.callback.IMyButtonCallback
import com.example.kotlineatv2server.common.Common
import com.example.kotlineatv2server.common.MySwipeHelper
import com.example.kotlineatv2server.eventbus.ToasEvent
import com.example.kotlineatv2server.model.BestDealModel
import com.example.kotlineatv2server.model.CategoryModel
import com.example.kotlineatv2server.ui.category.CategoryFragment
import com.example.kotlineatv2server.ui.category.CategoryViewModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dmax.dialog.SpotsDialog
import org.greenrobot.eventbus.EventBus
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class BestDealFragment : Fragment() {


    private lateinit var bestDealViewModel: BestDealViewModel
    private lateinit var dialog: AlertDialog
    private lateinit var layoutAnimationControler: LayoutAnimationController
    private var bestDealAdapter: MyBestDealAdapter?=null
    private var recycler_best_deal: RecyclerView?=null
    internal var bestDealModels:List<BestDealModel> = ArrayList<BestDealModel>()

    internal lateinit var storage: FirebaseStorage
    internal lateinit var storageRef: StorageReference
    internal lateinit var img_best_deal: ImageView

    private var imageUri: Uri?=null


    companion object{
        val PICK_IMAGE_REQUEST = 1212
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bestDealViewModel =
            ViewModelProviders.of(this).get(BestDealViewModel::class.java)
        val root = inflater.inflate(com.example.kotlineatv2server.R.layout.best_deal_fragment, container, false)

        initView(root)
        bestDealViewModel.getMessageError().observe(viewLifecycleOwner, Observer {

        })

        bestDealViewModel.getBestDealList().observe(viewLifecycleOwner, Observer {
            dialog.dismiss()
            bestDealModels = it
            val contexto = context
            bestDealAdapter = MyBestDealAdapter(contexto!!,it)
            recycler_best_deal!!.adapter = bestDealAdapter
            recycler_best_deal!!.layoutAnimation = layoutAnimationControler
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
        recycler_best_deal = root!!.findViewById(R.id.recycler_best_deal) as RecyclerView
        recycler_best_deal!!.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(context)
        recycler_best_deal!!.layoutManager = layoutManager
        recycler_best_deal!!.addItemDecoration(DividerItemDecoration(context,layoutManager.orientation))

        val swipe = @SuppressLint("UseRequireInsteadOfGet")
        object: MySwipeHelper(context!!,recycler_best_deal!!,200)
        {
            override fun instantiateMyButton(
                viewHolder: RecyclerView.ViewHolder,
                buffer: MutableList<MyButton>
            ) {

                buffer.add(MyButton(context!!,
                    "Eliminar",
                    30,
                    0,
                    Color.parseColor("#9C9A9E"),
                    object : IMyButtonCallback {
                        override fun onClick(pos: Int) {
                            Common.bestDealSelected = bestDealModels[pos]
                            showDeleteDialog()
                        }
                    }))

                buffer.add(MyButton(context!!,
                    "Editar",
                    30,
                    0,
                    Color.parseColor("#560027"),
                    object : IMyButtonCallback {
                        override fun onClick(pos: Int) {
                            Common.bestDealSelected = bestDealModels[pos]
                            showUpdateDialog()
                        }
                    }))
            }
        }

    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun showDeleteDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(context!!)
        builder.setTitle("Eliminar")
        builder.setMessage("Seguro(a) de eliminar")
        builder.setNegativeButton("CANCEL"){dialogInterface,_->dialogInterface.dismiss()}
        builder.setPositiveButton("ELIMINAR"){dialogInterface,_-> //CODING FOR DELETE ITEM BEST DEAL
            deleteBestDeal()

        }
        val dialog = builder.create()
        dialog.show()

    }

    private fun deleteBestDeal() {
        FirebaseDatabase.getInstance()
            .getReference(Common.BEST_DEALS_REFERENCE)
            .child(Common.bestDealSelected!!.key!!)
            .removeValue()
            .addOnFailureListener{e->Toast.makeText(context,""+e.message,Toast.LENGTH_SHORT).show()}
            .addOnCompleteListener{task->
                bestDealViewModel!!.loadBestDeal()
                EventBus.getDefault().postSticky(ToasEvent(false,true))
                imageUri = null
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
        img_best_deal = itemview.findViewById<View>(R.id.img_category)as ImageView
        edt_category_name.setText(Common.bestDealSelected!!.name)
        Glide.with(context!!).load(Common.bestDealSelected!!.image).into(img_best_deal)
        img_best_deal.setOnClickListener{
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent,"Seleccione imagen"), PICK_IMAGE_REQUEST
            )
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
                        Toast.makeText(context,""+e.message, Toast.LENGTH_SHORT).show()
                    }
                    .addOnProgressListener{
                            taskSnapshot->
                        val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                        dialog.setMessage("Espere.. $progress")
                    }
                    .addOnCompleteListener{

                        imageFolder.downloadUrl.addOnSuccessListener { uri->
                            dialogInterface.dismiss()
                            dialog.dismiss()

                            updateData["image"] = uri.toString()
                            updateBestDeal(updateData)
                        }
                    }

            } else {
                updateBestDeal(updateData)
            }
        }

        builder.setView(itemview)
        val contructor = builder.create()
        contructor.show()

    }

    private fun updateBestDeal(updateData: HashMap<String, Any>) {

        FirebaseDatabase.getInstance()
            .getReference(Common.BEST_DEALS_REFERENCE)
            .child(Common.bestDealSelected!!.key!!)
            .updateChildren(updateData)
            .addOnFailureListener{e->Toast.makeText(context,""+e.message,Toast.LENGTH_SHORT).show()}
            .addOnCompleteListener{task->
                bestDealViewModel!!.loadBestDeal()
                EventBus.getDefault().postSticky(ToasEvent(true,true))
                imageUri = null
            }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST){
            if (resultCode == Activity.RESULT_OK){

                if (data != null && data.data != null){
                    imageUri = data.data
                    img_best_deal.setImageURI(imageUri)

                }
            }
        }
    }


}