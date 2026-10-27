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

    // Inicio de sesión. LOWER() pasa a minúsculas los dos lados:
    // "Tecnico@Gmail.cl" y "tecnico@gmail.cl" se consideran el mismo correo.
    @Query("SELECT * FROM usuarios WHERE LOWER(correo) = LOWER(:correo) AND contrasena = :contrasena LIMIT 1")
    suspend fun iniciarSesion(correo: String, contrasena: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id_usuario = :id LIMIT 1")
    fun buscarUsuarioPorId(id: Long): Flow<UsuarioEntity?>

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun obtenerCantidadUsuarios(): Int

    // ACTUALIZACIÓN DE PERFIL (Mario): permite modificar nombre, correo y foto del usuario
    @Query("UPDATE usuarios SET nombre = :nombre, correo = :correo, foto_perfil = :fotoPerfil WHERE id_usuario = :idUsuario")
    suspend fun actualizarPerfil(idUsuario: Long, nombre: String, correo: String, fotoPerfil: ByteArray?)

    // NUEVO (Restablecer contraseña): UPDATE de UNA sola columna.
    // Busca al usuario por su correo (sin importar mayúsculas) y le pone la clave nueva.
    // Devuelve cuántas filas cambió: 1 = listo, 0 = no existe una cuenta con ese correo.
    // ¡Sin el WHERE se cambiaría la contraseña de TODOS los usuarios!
    @Query("UPDATE usuarios SET contrasena = :nueva WHERE LOWER(correo) = LOWER(:correo)")
    suspend fun actualizarContrasena(correo: String, nueva: String): Int
}