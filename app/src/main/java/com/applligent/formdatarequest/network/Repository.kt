package com.applligent.formdatarequest.network

import okhttp3.MultipartBody
import okhttp3.RequestBody

class Repository(private val apiInterface: ApiInterface) {

    fun addProduct(map: RequestBody, token:String) = apiInterface.addProduct(map,token)
}