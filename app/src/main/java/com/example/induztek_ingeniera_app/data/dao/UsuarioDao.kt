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

    @Query("SELECT * FROM usuarios WHERE LOWER(correo) = LOWER(:correo) AND contrasena = :contrasena LIMIT 1")
    suspend fun iniciarSesion(correo: String, contrasena: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id_usuario = :id LIMIT 1")
    fun buscarUsuarioPorId(id: Long): Flow<UsuarioEntity?>

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun obtenerCantidadUsuarios(): Int


    @Query("UPDATE usuarios SET nombre = :nombre, correo = :correo, foto_perfil = :fotoPerfil WHERE id_usuario = :idUsuario")
    suspend fun actualizarPerfil(idUsuario: Long, nombre: String, correo: String, fotoPerfil: ByteArray?)


    @Query("UPDATE usuarios SET contrasena = :nueva WHERE LOWER(correo) = LOWER(:correo)")
    suspend fun actualizarContrasena(correo: String, nueva: String): Int
}