package com.example.induztek_ingeniera_app.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Endpoints del microservicio empresas
interface EmpresaApi {

    @GET("api/empresas")
    suspend fun listar(): List<EmpresaResponse>

    @GET("api/empresas/{id}")
    suspend fun obtener(@Path("id") id: Long): EmpresaResponse

    @POST("api/empresas")
    suspend fun crear(@Body datos: EmpresaRequest): EmpresaResponse

    @PUT("api/empresas/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body datos: EmpresaRequest): EmpresaResponse
}