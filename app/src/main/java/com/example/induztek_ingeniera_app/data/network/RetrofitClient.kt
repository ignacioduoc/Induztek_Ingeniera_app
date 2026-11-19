package com.example.induztek_ingeniera_app.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // 10.0.2.2 apunta al localhost de tu PC desde el emulador Android
    private const val BASE_URL_EQUIPOS = "http://10.0.2.2:8083/"
    private const val BASE_URL_PROTOCOLOS = "http://10.0.2.2:8084/"

    val equiposApi: EquiposApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_EQUIPOS)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EquiposApi::class.java)
    }

    val protocolosApi: ProtocolosApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_PROTOCOLOS)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProtocolosApi::class.java)
    }
}