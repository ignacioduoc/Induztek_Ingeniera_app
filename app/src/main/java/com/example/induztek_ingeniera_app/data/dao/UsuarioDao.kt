package com.example.induztek_ingeniera_app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.induztek_ingeniera_app.data.entities.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    // IGNORE: si el correo o el RUT ya existen no pisa al otro usuario, devuelve -1
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarUsuario(usuario: UsuarioEntity): Long

    @Update
    suspend fun actualizarUsuario(usuario: UsuarioEntity): Int

    @Query("SELECT * FROM usuarios WHERE LOWER(correo) = LOWER(:correo) AND contrasena = :contrasena LIMIT 1")
    suspend fun iniciarSesion(correo: String, contrasena: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id_usuario = :id LIMIT 1")
    fun buscarUsuarioPorId(id: Long): Flow<UsuarioEntity?>

    @Query("SELECT * FROM usuarios ORDER BY nombre ASC, apellido ASC")
    fun listarUsuarios(): Flow<List<UsuarioEntity>>

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun obtenerCantidadUsuarios(): Int


    @Query("UPDATE usuarios SET nombre = :nombre, correo = :correo, foto_perfil = :fotoPerfil WHERE id_usuario = :idUsuario")
    suspend fun actualizarPerfil(idUsuario: Long, nombre: String, correo: String, fotoPerfil: ByteArray?)


    @Query("UPDATE usuarios SET contrasena = :nueva WHERE LOWER(correo) = LOWER(:correo)")
    suspend fun actualizarContrasena(correo: String, nueva: String): Int


    // Validar correo y RUT repetidos. Al crear se pasa idExcluir = 0; al editar, el id del usuario
    @Query("SELECT COUNT(*) FROM usuarios WHERE LOWER(correo) = LOWER(:correo) AND id_usuario != :idExcluir")
    suspend fun contarPorCorreo(correo: String, idExcluir: Long): Int

    @Query("SELECT COUNT(*) FROM usuarios WHERE rut = :rut AND id_usuario != :idExcluir")
    suspend fun contarPorRut(rut: String, idExcluir: Long): Int


    // Preguntas de seguridad (Tarea 2)
    @Query("SELECT pregunta_seguridad FROM usuarios WHERE LOWER(correo) = LOWER(:correo) LIMIT 1")
    suspend fun obtenerPreguntaSeguridad(correo: String): String?

    // Lo guardado de la respuesta: un hash ("sha256:sal:hash") o, en cuentas antiguas, el texto plano
    @Query("SELECT respuesta_seguridad FROM usuarios WHERE LOWER(correo) = LOWER(:correo) LIMIT 1")
    suspend fun obtenerRespuestaSeguridad(correo: String): String?

    @Query("UPDATE usuarios SET respuesta_seguridad = :respuesta WHERE LOWER(correo) = LOWER(:correo)")
    suspend fun actualizarRespuestaSeguridad(correo: String, respuesta: String): Int
}