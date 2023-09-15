package com.applligent.formdatarequest.network

import com.applligent.formdatarequest.utilities.Constant.BASE_URL
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient {
    private var retrofit: Retrofit? = null

    fun getClient(): Retrofit? {
        if (retrofit == null) {

            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.apply {
                httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .protocols(listOf(Protocol.HTTP_1_1))
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(httpLoggingInterceptor)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create()).build()
        }
        return retrofit
    }
}