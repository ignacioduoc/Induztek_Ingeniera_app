package com.example.induztek_ingeniera_app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.induztek_ingeniera_app.data.entities.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuario(usuario: UsuarioEntity): Long

    // Cambiado de Flow a suspend fun con LOWER() para asegurar coincidencia exacta
    @Query("SELECT * FROM usuarios WHERE LOWER(correo) = LOWER(:correo) AND contrasena = :contrasena LIMIT 1")
    suspend fun iniciarSesion(correo: String, contrasena: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id_usuario = :id LIMIT 1")
    fun buscarUsuarioPorId(id: Long): Flow<UsuarioEntity?>

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun obtenerCantidadUsuarios(): Int
}