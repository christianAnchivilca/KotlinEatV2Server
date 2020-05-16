package com.example.kotlineatv2server.callback

import com.example.kotlineatv2server.model.CategoryModel

interface ICategoryCallabackListener {
    fun onCategoryLoadSuccess(categoryModelList:List<CategoryModel>)
    fun onCategoryLoadFailed(message:String)


}