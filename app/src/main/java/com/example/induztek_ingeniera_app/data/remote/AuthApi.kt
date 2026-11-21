package com.example.induztek_ingeniera_app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// Endpoints del microservicio auth (login, cuentas y contraseñas)
interface AuthApi {

    @POST("api/auth/login")
    suspend fun iniciarSesion(@Body datos: LoginRequest): LoginResponse

    // 201 sin cuerpo -> Response<Unit>
    @POST("api/auth/credenciales")
    suspend fun crearCredencial(@Body datos: CredencialRequest): Response<Unit>

    // 204 sin cuerpo
    @POST("api/auth/configurar")
    suspend fun configurar(@Body datos: CambiarContrasenaRequest): Response<Unit>

    @POST("api/auth/olvide")
    suspend fun olvide(@Body datos: OlvideRequest): Response<Unit>

    @POST("api/auth/restablecer")
    suspend fun restablecer(@Body datos: RestablecerRequest): Response<Unit>
}