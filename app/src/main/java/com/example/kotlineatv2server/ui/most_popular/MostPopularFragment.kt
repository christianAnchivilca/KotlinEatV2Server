package com.example.kotlineatv2server.ui.most_popular

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
import com.example.kotlineatv2server.adapter.MyMostPopularAdapter
import com.example.kotlineatv2server.callback.IMyButtonCallback
import com.example.kotlineatv2server.common.Common
import com.example.kotlineatv2server.common.MySwipeHelper
import com.example.kotlineatv2server.eventbus.ToasEvent
import com.example.kotlineatv2server.model.BestDealModel
import com.example.kotlineatv2server.model.MostPopularModel
import com.example.kotlineatv2server.ui.best_deal.BestDealFragment
import com.example.kotlineatv2server.ui.best_deal.BestDealViewModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dmax.dialog.SpotsDialog
import org.greenrobot.eventbus.EventBus
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MostPopularFragment : Fragment() {


    private lateinit var dialog: AlertDialog
    private lateinit var layoutAnimationControler: LayoutAnimationController
    private var mostPopularAdapter: MyMostPopularAdapter?=null
    private var recycler_most_popular: RecyclerView?=null
    internal var mostPopularModels:List<MostPopularModel> = ArrayList<MostPopularModel>()

    internal lateinit var storage: FirebaseStorage
    internal lateinit var storageRef: StorageReference
    internal lateinit var img_most_popular: ImageView

    private var imageUri: Uri?=null
    private lateinit var mostPopularViewModel: MostPopularViewModel


    companion object{
        val PICK_IMAGE_REQUEST = 1212
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mostPopularViewModel =
            ViewModelProviders.of(this).get(MostPopularViewModel::class.java)
        val root = inflater.inflate(com.example.kotlineatv2server.R.layout.fragment_most_popular
            , container, false)

        initView(root)
        mostPopularViewModel.getMessageError().observe(viewLifecycleOwner, Observer {
           Toast.makeText(requireContext(),it.toString(),Toast.LENGTH_LONG).show()
        })

        mostPopularViewModel.getMostPopularList().observe(viewLifecycleOwner, Observer {
            dialog.dismiss()
            mostPopularModels = it
            val contexto = context
            mostPopularAdapter = MyMostPopularAdapter(contexto!!,it)
            recycler_most_popular!!.adapter = mostPopularAdapter
            recycler_most_popular!!.layoutAnimation = layoutAnimationControler
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
        recycler_most_popular = root!!.findViewById(R.id.recycler_most_popular) as RecyclerView
        recycler_most_popular!!.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(context)
        recycler_most_popular!!.layoutManager = layoutManager
        recycler_most_popular!!.addItemDecoration(DividerItemDecoration(context,layoutManager.orientation))

        val swipe = @SuppressLint("UseRequireInsteadOfGet")
        object: MySwipeHelper(context!!,recycler_most_popular!!,200)
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
                            Common.mostPopularSelected = mostPopularModels[pos]
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
                            Common.mostPopularSelected = mostPopularModels[pos]
                            showUpdateDialog()
                        }
                    }))
            }
        }

    }

    private fun showDeleteDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Eliminar")
        builder.setMessage("Seguro(a) de eliminar")
        builder.setNegativeButton("CANCEL"){dialogInterface,_->dialogInterface.dismiss()}
        builder.setPositiveButton("ELIMINAR"){dialogInterface,_-> //CODING FOR DELETE ITEM BEST DEAL
            deleteMostPopular()

        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun deleteMostPopular() {
        FirebaseDatabase.getInstance()
            .getReference(Common.MOST_POPULAR_REFERENCE)
            .child(Common.mostPopularSelected!!.key!!)
            .removeValue()
            .addOnFailureListener{e->Toast.makeText(context,""+e.message,Toast.LENGTH_SHORT).show()}
            .addOnCompleteListener{task->
                mostPopularViewModel!!.loadMostPopular()
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
        img_most_popular = itemview.findViewById<View>(R.id.img_category)as ImageView
        edt_category_name.setText(Common.mostPopularSelected!!.name)

        Glide.with(context!!).load(Common.mostPopularSelected!!.image).into(img_most_popular)

        img_most_popular.setOnClickListener{
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent,"Seleccione imagen"),
                PICK_IMAGE_REQUEST
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
                            updateMostPopular(updateData)
                        }
                    }

            } else {
                updateMostPopular(updateData)
            }
        }

        builder.setView(itemview)
        val contructor = builder.create()
        contructor.show()

    }

    private fun updateMostPopular(updateData: HashMap<String, Any>) {
        FirebaseDatabase.getInstance()
            .getReference(Common.MOST_POPULAR_REFERENCE)
            .child(Common.mostPopularSelected!!.key!!)
            .updateChildren(updateData)
            .addOnFailureListener{e->Toast.makeText(context,""+e.message,Toast.LENGTH_SHORT).show()}
            .addOnCompleteListener{task->
                mostPopularViewModel!!.loadMostPopular()
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
                    img_most_popular.setImageURI(imageUri)

                }
            }
        }
    }


}