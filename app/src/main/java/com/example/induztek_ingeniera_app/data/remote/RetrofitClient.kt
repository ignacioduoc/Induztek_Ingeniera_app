package com.example.induztek_ingeniera_app.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {

    private const val URL_GATEWAY = "http://10.0.2.2:8090/"


    private fun crear(urlBase: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(urlBase)
            .addConverterFactory(GsonConverterFactory.create())
            .build()


    val authApi: AuthApi by lazy { crear(URL_GATEWAY).create(AuthApi::class.java) }

    val usuarioApi: UsuarioApi by lazy { crear(URL_GATEWAY).create(UsuarioApi::class.java) }

    val empresaApi: EmpresaApi by lazy { crear(URL_GATEWAY).create(EmpresaApi::class.java) }

    val equipoApi: EquipoApi by lazy { crear(URL_GATEWAY).create(EquipoApi::class.java) }

    val protocoloApi: ProtocoloApi by lazy { crear(URL_GATEWAY).create(ProtocoloApi::class.java) }
}