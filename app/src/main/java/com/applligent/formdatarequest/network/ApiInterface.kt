package com.applligent.formdatarequest.network

import com.applligent.formdatarequest.model.ApiResponse
import com.applligent.formdatarequest.utilities.Constant.ADD_PRODUCT_API
import com.applligent.formdatarequest.utilities.Constant.AUTHORIZATION
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiInterface {
    @POST(ADD_PRODUCT_API)
    @JvmSuppressWildcards
    fun addProduct(@Body body: RequestBody, @Header(AUTHORIZATION) token: String): Call<ApiResponse>
}