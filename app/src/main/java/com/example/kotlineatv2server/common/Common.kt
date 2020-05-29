package com.example.kotlineatv2server.common

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.TextView
import com.example.kotlineatv2server.model.CategoryModel
import com.example.kotlineatv2server.model.FoodModel
import com.example.kotlineatv2server.model.ServerUserModel

object Common {
    val ORDER_REF:String ="Order"
    var foodModelSelected: FoodModel?=null
    var category_selected: CategoryModel?=null
    const val SERVER_REF = "Server"
    var currentServerUser:ServerUserModel?=null
    const val CATEGORY_REFERENCE = "Category"
    val DEFAULT_COLUMN_COUNT:Int = 0
    val FULL_WIDTH_COLUMN:Int = 1

    fun setSpanString(welcome: String, name: String?, txtUser: TextView?) {

        val builder = SpannableStringBuilder()
        builder.append(welcome)
        val txtSpannable = SpannableString(name)
        val boldSpan = StyleSpan(Typeface.BOLD)
        txtSpannable.setSpan(boldSpan,0,name!!.length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.append(txtSpannable)
        txtUser!!.setText(builder,TextView.BufferType.SPANNABLE)



    }

    fun setSpanStringColor(welcome: String, name: String?, txtUser: TextView?,color:Int){
        val builder = SpannableStringBuilder()
        builder.append(welcome)
        val txtSpannable = SpannableString(name)
        val boldSpan = StyleSpan(Typeface.BOLD)
        txtSpannable.setSpan(boldSpan,0,name!!.length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        txtSpannable.setSpan(ForegroundColorSpan(color),0,name!!.length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.append(txtSpannable)
        txtUser!!.setText(builder,TextView.BufferType.SPANNABLE)

    }

    fun getDateOfWeek(get: Int): String {
        when(get){
            1 -> return "Lunes"
            2 -> return "Martes"
            3 -> return "Miercoles"
            4 -> return "Jueves"
            5 -> return "Viernes"
            6 -> return "Sabado"
            7 -> return "Domingo"
            else -> return "Unknow"


        }

    }

    fun convertStatusToText(orderStatus: Int): String {
        when(orderStatus){

            0->return "Placed"
            1->return "Shipping"
            2->return "Shipped"
            -1->return "Cancelled"
            else -> return "Unknow"

        }

    }

    fun convertStatusToString(orderStatus: Int): String? =
        when(orderStatus){
            0 -> "Placed"
            1 -> "Shipping"
            2-> "Shipped"
            -1 -> "Canceled"
            else -> "Error"
        }




}