package com.example.induztek_ingeniera_app.data

import com.example.induztek_ingeniera_app.data.dao.EquipoDao
import com.example.induztek_ingeniera_app.data.dao.ProtocoloDao
import com.example.induztek_ingeniera_app.data.dao.UsuarioDao
import com.example.induztek_ingeniera_app.data.entities.EquipoEntity
import com.example.induztek_ingeniera_app.data.entities.ProtocoloConDetalles
import com.example.induztek_ingeniera_app.data.entities.ProtocoloEntity
import com.example.induztek_ingeniera_app.data.entities.UsuarioEntity
import com.example.induztek_ingeniera_app.model.Rol
import com.example.induztek_ingeniera_app.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

/**
 * REPOSITORIO
 *
 * Es la única fuente de datos de la app. Hoy los datos vienen de Room / SQLite;
 * más adelante podrían venir de un servicio REST sin que el ViewModel ni la vista tengan que cambiar.
 */
class ProtocoloRepository(
    private val usuarioDao: UsuarioDao,
    private val protocoloDao: ProtocoloDao,
    private val equipoDao: EquipoDao // Inyección de EquipoDao
) {
    // Autenticación de sesiones de usuario (Consulta directa suspend)
    suspend fun iniciarSesion(correo: String, contrasena: String): Usuario? {
        val entity = usuarioDao.iniciarSesion(correo, contrasena)
        return entity?.toDomainModel()
    }

    suspend fun buscarUsuarioPorId(id: Long): Usuario? {
        val entity = usuarioDao.buscarUsuarioPorId(id).firstOrNull()
        return entity?.toDomainModel()
    }

    // --- MÓDULO PERFIL DE USUARIO ---

    // Actualiza el nombre, correo y foto de perfil en la base de datos
    suspend fun actualizarPerfilUsuario(idUsuario: Long, nombre: String, correo: String, fotoPerfil: ByteArray?) {
        usuarioDao.actualizarPerfil(idUsuario, nombre, correo, fotoPerfil)
    }

    // --- MÓDULO EQUIPOS ---

    // Obtener todos los equipos para el perfil del Técnico
    fun obtenerTodosLosEquipos(): Flow<List<EquipoEntity>> {
        return equipoDao.obtenerTodos()
    }

    // Obtener los equipos pertenecientes a un cliente específico
    fun obtenerEquiposPorCliente(idCliente: Long): Flow<List<EquipoEntity>> {
        return equipoDao.obtenerEquiposPorCliente(idCliente)
    }

    // Buscar un equipo por su ID
    fun buscarEquipoPorId(idEquipo: Long): Flow<EquipoEntity?> {
        return equipoDao.buscarEquipoPorId(idEquipo)
    }

    // Actualización de ubicación y coordenadas de equipos
    suspend fun actualizarUbicacionEquipo(idEquipo: Long, direccion: String, latitud: Double, longitud: Double) {
        equipoDao.actualizarUbicacion(idEquipo, direccion, latitud, longitud)
    }

    // --- MÓDULO PROTOCOLOS ---

    // Escritura de Órdenes de Trabajo
    suspend fun insertarProtocolo(protocolo: ProtocoloEntity): Long {
        return protocoloDao.insertarProtocolo(protocolo)
    }

    suspend fun formalizarProtocolo(id: Long): Boolean {
        return protocoloDao.formalizarProtocolo(id) > 0
    }

    // Lecturas Reactivas en Tiempo Real para tus componentes Compose
    fun listarProtocolosDeCliente(clienteId: Long): Flow<List<ProtocoloConDetalles>> {
        return protocoloDao.listarProtocolosDeCliente(clienteId)
    }

    fun listarProtocolosDeEquipo(equipoId: Long): Flow<List<ProtocoloConDetalles>> {
        return protocoloDao.listarProtocolosDeEquipo(equipoId)
    }

    suspend fun buscarProtocoloPorId(id: Long): ProtocoloConDetalles? {
        return protocoloDao.buscarProtocoloPorId(id).firstOrNull()
    }

    // --- FUNCIONES DE EXTENSIÓN (Mapeo de Entidad a Modelo de Dominio) ---
    private fun UsuarioEntity.toDomainModel(): Usuario {
        val rolNombre = when (this.idRol) {
            Rol.ID_TECNICO -> "Técnico"
            Rol.ID_CLIENTE -> "Cliente"
            else -> "Usuario"
        }
        return Usuario(
            id = this.idUsuario,
            nombre = this.nombre,
            correo = this.correo,
            empresa = this.empresa,
            rolId = this.idRol,
            rolNombre = rolNombre,
            fotoPerfil = this.fotoPerfil
        )
    }
}