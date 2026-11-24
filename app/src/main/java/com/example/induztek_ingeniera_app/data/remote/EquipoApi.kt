package com.example.induztek_ingeniera_app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface EquipoApi {

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

    @PATCH("api/equipos/{id}/tecnico")
    suspend fun asignarTecnico(
        @Path("id") id: Long,
        @Body req: AsignarTecnicoRequest
    ): Response<EquipoApiDto>
}