package com.example.kotlineatv2server.ui.most_popular

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlineatv2server.callback.IBestDealCallabackListener
import com.example.kotlineatv2server.callback.IMostPopularlCallbackListener
import com.example.kotlineatv2server.common.Common
import com.example.kotlineatv2server.model.BestDealModel
import com.example.kotlineatv2server.model.MostPopularModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MostPopularViewModel : ViewModel(), IMostPopularlCallbackListener {
    private var mostPopularListMutable : MutableLiveData<List<MostPopularModel>>?=null
    private var messageError: MutableLiveData<String> = MutableLiveData()
    private val mostPopularCallbackListener : IMostPopularlCallbackListener

    init {
        mostPopularCallbackListener = this
    }


    fun getMostPopularList():MutableLiveData<List<MostPopularModel>>{
        if (mostPopularListMutable == null)
        {
            mostPopularListMutable = MutableLiveData()
            loadMostPopular()

        }


        return mostPopularListMutable!!

    }

    fun loadMostPopular() {
        val tempList = ArrayList<MostPopularModel>()
        val mostPopularRef = FirebaseDatabase.getInstance().getReference(Common.MOST_POPULAR_REFERENCE)

        mostPopularRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                mostPopularCallbackListener.onListMostPopularLoadFailed(p0.message!!)
            }

            override fun onDataChange(data: DataSnapshot) {
                for (item in data!!.children){
                    val model = item.getValue<MostPopularModel>(MostPopularModel::class.java)
                    model!!.key=item.key!!
                    tempList.add(model!!)
                }

                mostPopularCallbackListener.onListMostPopularLoadSuccess(tempList)
            }

        })
    }


    fun getMessageError ():MutableLiveData<String>{
        return messageError
    }




    override fun onListMostPopularLoadSuccess(mostPopularModelList: List<MostPopularModel>) {
        mostPopularListMutable!!.value=mostPopularModelList
    }

    override fun onListMostPopularLoadFailed(message: String) {
        messageError.value=message
    }
}