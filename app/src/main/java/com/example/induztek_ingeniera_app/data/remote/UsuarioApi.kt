package com.example.induztek_ingeniera_app.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// Endpoints del microservicio usuarios
interface UsuarioApi {

    @GET("api/usuarios")
    suspend fun listar(@Query("buscar") buscar: String?): List<UsuarioResponse>

    @GET("api/usuarios/{id}")
    suspend fun obtener(@Path("id") id: Long): UsuarioResponse

    @POST("api/usuarios")
    suspend fun registrar(@Body datos: RegistroRequest): UsuarioResponse

    @PUT("api/usuarios/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body datos: ActualizarUsuarioRequest): UsuarioResponse

    // Sin tipo de retorno: el servidor responde 204 No Content
    @DELETE("api/usuarios/{id}")
    suspend fun eliminar(@Path("id") id: Long)
}