package com.example.induztek_ingeniera_app.data.network

import retrofit2.Response
import retrofit2.http.*

interface EquiposApi {
    @GET("api/equipos")
    suspend fun listarTodos(): List<EquipoApiDto>

    @GET("api/equipos/cliente/{idCliente}")
    suspend fun listarPorCliente(@Path("idCliente") idCliente: Long): List<EquipoApiDto>

    @POST("api/equipos")
    suspend fun crear(@Body equipo: EquipoApiDto): Response<EquipoApiDto>

    @PATCH("api/equipos/{id}/ubicacion")
    suspend fun actualizarUbicacion(
        @Path("id") id: Long,
        @Body req: UbicacionRequestDto
    ): Response<EquipoApiDto>
}

interface ProtocolosApi {
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