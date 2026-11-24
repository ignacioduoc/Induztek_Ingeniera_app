package com.example.induztek_ingeniera_app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ProtocoloApi {

    @GET("api/protocolos")
    suspend fun listarTodos(): List<ProtocoloDetalleApiDto>

    @GET("api/protocolos/{id}")
    suspend fun obtenerPorId(@Path("id") id: Long): Response<ProtocoloDetalleApiDto>

    @GET("api/protocolos/equipo/{idEquipo}")
    suspend fun listarPorEquipo(@Path("idEquipo") idEquipo: Long): List<ProtocoloDetalleApiDto>

    @GET("api/protocolos/cliente/{idCliente}")
    suspend fun listarPorCliente(@Path("idCliente") idCliente: Long): List<ProtocoloDetalleApiDto>

    @POST("api/protocolos")
    suspend fun crear(@Body protocolo: ProtocoloApiDto): Response<ProtocoloApiDto>

    @PATCH("api/protocolos/{id}/formalizar")
    suspend fun formalizar(@Path("id") id: Long): Response<ProtocoloApiDto>
}