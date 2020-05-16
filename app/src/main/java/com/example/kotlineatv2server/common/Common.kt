package com.example.kotlineatv2server.common

import com.example.kotlineatv2server.model.CategoryModel
import com.example.kotlineatv2server.model.ServerUserModel

object Common {
    var category_selected: CategoryModel?=null
    const val SERVER_REF = "Server"
    var currentServerUser:ServerUserModel?=null
    const val CATEGORY_REFERENCE = "Category"
    val DEFAULT_COLUMN_COUNT:Int = 0
    val FULL_WIDTH_COLUMN:Int = 1
}