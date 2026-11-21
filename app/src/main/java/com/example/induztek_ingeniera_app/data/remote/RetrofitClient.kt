package com.example.induztek_ingeniera_app.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// =====================================================================
//  RetrofitClient.kt
//  Crea UNA sola vez los clientes de cada microservicio ("object").
//  10.0.2.2 = el "localhost" del PC visto desde el EMULADOR.
//  La URL base DEBE terminar en "/".
// =====================================================================

object RetrofitClient {

    // Gateway: una sola puerta para auth, usuarios y empresas
    private const val URL_GATEWAY = "http://10.0.2.2:8090/"

    // Servicios de tu compañero (todavía directo, sin gateway)
    private const val URL_EQUIPOS = "http://10.0.2.2:8083/"
    private const val URL_PROTOCOLOS = "http://10.0.2.2:8084/"

    // Arma un Retrofit para una URL base. Gson traduce JSON <-> data class.
    private fun crear(urlBase: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(urlBase)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // by lazy = se crea la primera vez que se usa, no al abrir la app
    val authApi: AuthApi by lazy { crear(URL_GATEWAY).create(AuthApi::class.java) }

    val usuarioApi: UsuarioApi by lazy { crear(URL_GATEWAY).create(UsuarioApi::class.java) }

    val empresaApi: EmpresaApi by lazy { crear(URL_GATEWAY).create(EmpresaApi::class.java) }

    val equipoApi: EquipoApi by lazy { crear(URL_EQUIPOS).create(EquipoApi::class.java) }

    val protocoloApi: ProtocoloApi by lazy { crear(URL_PROTOCOLOS).create(ProtocoloApi::class.java) }
}