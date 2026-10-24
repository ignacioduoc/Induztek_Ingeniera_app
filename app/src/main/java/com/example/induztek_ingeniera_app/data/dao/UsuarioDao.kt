package com.example.induztek_ingeniera_app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.induztek_ingeniera_app.data.entities.UsuarioEntity

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios WHERE correo = :correo AND contrasena = :contrasena LIMIT 1")
    suspend fun iniciarSesion(correo: String, contrasena: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id_usuario = :id LIMIT 1")
    suspend fun buscarUsuarioPorId(id: Long): UsuarioEntity?
}
