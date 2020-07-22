package com.example.kotlineatv2server.ui.best_deal

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlineatv2server.callback.IBestDealCallabackListener
import com.example.kotlineatv2server.callback.ICategoryCallabackListener
import com.example.kotlineatv2server.common.Common
import com.example.kotlineatv2server.model.BestDealModel
import com.example.kotlineatv2server.model.CategoryModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BestDealViewModel : ViewModel(), IBestDealCallabackListener {
    private var bestDealListMutable : MutableLiveData<List<BestDealModel>>?=null
    private var messageError: MutableLiveData<String> = MutableLiveData()
    private val bestDealCallbackListener : IBestDealCallabackListener

    init {
        bestDealCallbackListener = this
    }

    fun getBestDealList():MutableLiveData<List<BestDealModel>>{
        if (bestDealListMutable == null)
        {
            bestDealListMutable = MutableLiveData()
            loadBestDeal()

        }


        return bestDealListMutable!!

    }

     fun loadBestDeal() {
        val tempList = ArrayList<BestDealModel>()
        val bestDealRef = FirebaseDatabase.getInstance().getReference(Common.BEST_DEALS_REFERENCE)

        bestDealRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                bestDealCallbackListener.onListBestDealLoadFailed(p0.message!!)
            }

            override fun onDataChange(data: DataSnapshot) {
                for (item in data!!.children){
                    val model = item.getValue<BestDealModel>(BestDealModel::class.java)
                    model!!.key=item.key!!
                    tempList.add(model!!)
                }

                bestDealCallbackListener.onListBestDealLoadSuccess(tempList)
            }

        })
    }


    fun getMessageError ():MutableLiveData<String>{
        return messageError
    }

    override fun onListBestDealLoadSuccess(bestDealModelList: List<BestDealModel>) {
        bestDealListMutable!!.value= bestDealModelList

    }

    override fun onListBestDealLoadFailed(message: String) {
        messageError.value = message
    }

}