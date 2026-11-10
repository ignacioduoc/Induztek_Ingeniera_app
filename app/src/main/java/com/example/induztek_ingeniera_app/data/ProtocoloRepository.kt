package com.example.induztek_ingeniera_app.data

import com.example.induztek_ingeniera_app.data.dao.EquipoDao
import com.example.induztek_ingeniera_app.data.dao.ProtocoloDao
import com.example.induztek_ingeniera_app.data.dao.UsuarioDao
import com.example.induztek_ingeniera_app.data.entities.EmpresaEntity
import com.example.induztek_ingeniera_app.data.entities.EquipoConDetalles
import com.example.induztek_ingeniera_app.data.entities.EquipoEntity
import com.example.induztek_ingeniera_app.data.entities.ProtocoloConDetalles
import com.example.induztek_ingeniera_app.data.entities.ProtocoloEntity
import com.example.induztek_ingeniera_app.data.entities.UsuarioEntity
import com.example.induztek_ingeniera_app.model.Rol
import com.example.induztek_ingeniera_app.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

// La respuesta de seguridad se guarda y se compara siempre así: sin espacios sobrantes y en minúsculas.
// Úsala también al crear o editar un usuario, antes de guardar la respuesta.
fun normalizarRespuestaSeguridad(texto: String): String = texto.trim().lowercase()

class ProtocoloRepository(
    private val usuarioDao: UsuarioDao,
    private val protocoloDao: ProtocoloDao,
    private val equipoDao: EquipoDao
) {
    suspend fun iniciarSesion(correo: String, contrasena: String): Usuario? {
        val entity = usuarioDao.iniciarSesion(correo, contrasena)
        return entity?.toDomainModel()
    }

    suspend fun buscarUsuarioPorId(id: Long): Usuario? {
        val entity = usuarioDao.buscarUsuarioPorId(id).firstOrNull()
        return entity?.toDomainModel()
    }

    suspend fun cambiarContrasena(correo: String, nueva: String): Boolean {
        return usuarioDao.actualizarContrasena(correo, nueva) > 0
    }

    suspend fun actualizarPerfilUsuario(idUsuario: Long, nombre: String, correo: String, fotoPerfil: ByteArray?) {
        usuarioDao.actualizarPerfil(idUsuario, nombre, correo, fotoPerfil)
    }


    // ---------- Usuarios (Tareas 2, 3 y 6) ----------

    fun listarUsuarios(): Flow<List<UsuarioEntity>> {
        return usuarioDao.listarUsuarios()
    }

    // Devuelve el id nuevo, o -1 si el correo o el RUT ya existen
    suspend fun crearUsuario(usuario: UsuarioEntity): Long {
        return usuarioDao.insertarUsuario(usuario)
    }

    suspend fun actualizarUsuario(usuario: UsuarioEntity): Boolean {
        return usuarioDao.actualizarUsuario(usuario) > 0
    }

    // idExcluir = 0 al crear; al editar, el id del usuario (para que no choque consigo mismo)
    suspend fun existeCorreo(correo: String, idExcluir: Long = 0): Boolean {
        return usuarioDao.contarPorCorreo(correo.trim(), idExcluir) > 0
    }

    suspend fun existeRut(rut: String, idExcluir: Long = 0): Boolean {
        return usuarioDao.contarPorRut(rut.trim(), idExcluir) > 0
    }

    // null = no existe ese correo, o ese usuario no tiene pregunta de seguridad guardada
    suspend fun obtenerPreguntaSeguridad(correo: String): String? {
        return usuarioDao.obtenerPreguntaSeguridad(correo.trim())?.takeIf { it.isNotBlank() }
    }

    suspend fun verificarRespuestaSeguridad(correo: String, respuesta: String): Boolean {
        return usuarioDao.contarRespuestaSeguridad(correo.trim(), normalizarRespuestaSeguridad(respuesta)) > 0
    }


    // ---------- Empresas (Tareas 3 y 5) ----------

    fun obtenerEmpresas(): Flow<List<EmpresaEntity>> {
        return equipoDao.obtenerEmpresas()
    }

    suspend fun buscarEmpresa(idEmpresa: Long): EmpresaEntity? {
        return equipoDao.buscarEmpresaPorId(idEmpresa)
    }

    // Devuelve el id nuevo, o -1 si ya existe una empresa con ese nombre
    suspend fun crearEmpresa(empresa: EmpresaEntity): Long {
        return equipoDao.insertarEmpresa(empresa)
    }

    suspend fun actualizarEmpresa(empresa: EmpresaEntity): Boolean {
        return equipoDao.actualizarEmpresa(empresa) > 0
    }

    suspend fun existeNombreEmpresa(nombre: String, idExcluir: Long = 0): Boolean {
        return equipoDao.contarEmpresasPorNombre(nombre.trim(), idExcluir) > 0
    }


    // ---------- Equipos (Tareas 3, 4, 5 y 7) ----------

    // Estas dos ya traen la empresa, su dirección y el nombre del cliente dueño
    fun obtenerTodosLosEquipos(): Flow<List<EquipoConDetalles>> {
        return equipoDao.obtenerTodos()
    }

    fun obtenerEquiposPorCliente(idCliente: Long): Flow<List<EquipoConDetalles>> {
        return equipoDao.obtenerEquiposPorCliente(idCliente)
    }

    fun buscarEquipoPorId(idEquipo: Long): Flow<EquipoEntity?> {
        return equipoDao.buscarEquipoPorId(idEquipo)
    }

    // Devuelve el id nuevo, o -1 si el código ya existe
    suspend fun crearEquipo(equipo: EquipoEntity): Long {
        return equipoDao.insertarEquipo(equipo)
    }

    suspend fun actualizarEquipo(equipo: EquipoEntity): Boolean {
        return equipoDao.actualizarEquipo(equipo) > 0
    }

    suspend fun existeCodigoEquipo(codigo: String, idExcluir: Long = 0): Boolean {
        return equipoDao.contarPorCodigo(codigo.trim(), idExcluir) > 0
    }

    suspend fun actualizarUbicacionEquipo(idEquipo: Long, direccion: String, latitud: Double, longitud: Double) {
        equipoDao.actualizarUbicacion(idEquipo, direccion, latitud, longitud)
    }


    // ---------- Protocolos ----------

    suspend fun insertarProtocolo(protocolo: ProtocoloEntity): Long {
        return protocoloDao.insertarProtocolo(protocolo)
    }

    suspend fun formalizarProtocolo(id: Long): Boolean {
        return protocoloDao.formalizarProtocolo(id) > 0
    }

    fun listarProtocolosDeCliente(clienteId: Long): Flow<List<ProtocoloConDetalles>> {
        return protocoloDao.listarProtocolosDeCliente(clienteId)
    }

    fun listarProtocolosDeEquipo(equipoId: Long): Flow<List<ProtocoloConDetalles>> {
        return protocoloDao.listarProtocolosDeEquipo(equipoId)
    }

    suspend fun buscarProtocoloPorId(id: Long): ProtocoloConDetalles? {
        return protocoloDao.buscarProtocoloPorId(id).firstOrNull()
    }

    // El nombre y la dirección de la empresa salen de la tabla "empresas" (por id_empresa).
    private suspend fun UsuarioEntity.toDomainModel(): Usuario {
        val rolNombre = when (this.idRol) {
            Rol.ID_TECNICO -> "Técnico"
            Rol.ID_CLIENTE -> "Cliente"
            Rol.ID_ADMINISTRADOR -> "Administrador"
            else -> "Usuario"
        }
        val empresaVinculada = this.idEmpresa?.let { equipoDao.buscarEmpresaPorId(it) }
        return Usuario(
            id = this.idUsuario,
            nombre = this.nombre,
            correo = this.correo,
            empresa = empresaVinculada?.nombre ?: "",
            rolId = this.idRol,
            rolNombre = rolNombre,
            fotoPerfil = this.fotoPerfil,
            apellido = this.apellido,
            rut = this.rut ?: "",
            telefono = this.telefono,
            idEmpresa = this.idEmpresa ?: 0L,
            direccionEmpresa = empresaVinculada?.direccion ?: ""
        )
    }
}