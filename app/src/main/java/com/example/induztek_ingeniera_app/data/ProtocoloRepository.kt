package com.example.induztek_ingeniera_app.data

import android.util.Base64
import com.example.induztek_ingeniera_app.data.dao.EquipoDao
import com.example.induztek_ingeniera_app.data.dao.ProtocoloDao
import com.example.induztek_ingeniera_app.data.dao.UsuarioDao
import com.example.induztek_ingeniera_app.data.entities.EmpresaEntity
import com.example.induztek_ingeniera_app.data.entities.EquipoConDetalles
import com.example.induztek_ingeniera_app.data.entities.EquipoEntity
import com.example.induztek_ingeniera_app.data.entities.ProtocoloConDetalles
import com.example.induztek_ingeniera_app.data.entities.ProtocoloEntity
import com.example.induztek_ingeniera_app.data.entities.UsuarioEntity
import com.example.induztek_ingeniera_app.data.network.EquipoApiDto
import com.example.induztek_ingeniera_app.data.network.ProtocoloApiDto
import com.example.induztek_ingeniera_app.data.network.RetrofitClient
import com.example.induztek_ingeniera_app.data.network.UbicacionRequestDto
import com.example.induztek_ingeniera_app.model.Rol
import com.example.induztek_ingeniera_app.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import java.security.MessageDigest
import java.security.SecureRandom

fun normalizarRespuestaSeguridad(texto: String): String = texto.trim().lowercase()

private const val PREFIJO_HASH = "sha256:"

private fun sha256Hex(texto: String): String =
    MessageDigest.getInstance("SHA-256").digest(texto.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

fun hashRespuestaSeguridad(respuesta: String): String {
    val sal = ByteArray(8).also { SecureRandom().nextBytes(it) }.joinToString("") { "%02x".format(it) }
    return "$PREFIJO_HASH$sal:${sha256Hex(sal + normalizarRespuestaSeguridad(respuesta))}"
}

private fun esHashRespuesta(guardada: String): Boolean =
    guardada.startsWith(PREFIJO_HASH) && guardada.count { it == ':' } == 2

private fun coincideConHash(guardada: String, respuesta: String): Boolean {
    val partes = guardada.removePrefix(PREFIJO_HASH).split(":")
    val calculado = sha256Hex(partes[0] + normalizarRespuestaSeguridad(respuesta))
    return MessageDigest.isEqual(calculado.toByteArray(), partes[1].toByteArray())
}

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

    suspend fun buscarUsuarioEntity(id: Long): UsuarioEntity? {
        return usuarioDao.buscarUsuarioPorId(id).firstOrNull()
    }

    suspend fun esContrasenaActual(correo: String, contrasena: String): Boolean {
        return usuarioDao.iniciarSesion(correo.trim(), contrasena) != null
    }

    suspend fun cambiarContrasena(correo: String, nueva: String): Boolean {
        return usuarioDao.actualizarContrasena(correo, nueva) > 0
    }

    suspend fun actualizarPerfilUsuario(idUsuario: Long, nombre: String, correo: String, fotoPerfil: ByteArray?) {
        usuarioDao.actualizarPerfil(idUsuario, nombre, correo, fotoPerfil)
    }


    // ---------- Usuarios ----------

    fun listarUsuarios(): Flow<List<UsuarioEntity>> {
        return usuarioDao.listarUsuarios()
    }

    suspend fun crearUsuario(usuario: UsuarioEntity): Long {
        return usuarioDao.insertarUsuario(usuario)
    }

    suspend fun actualizarUsuario(usuario: UsuarioEntity): Boolean {
        return usuarioDao.actualizarUsuario(usuario) > 0
    }

    suspend fun existeCorreo(correo: String, idExcluir: Long = 0): Boolean {
        return usuarioDao.contarPorCorreo(correo.trim(), idExcluir) > 0
    }

    suspend fun existeRut(rut: String, idExcluir: Long = 0): Boolean {
        return usuarioDao.contarPorRut(rut.trim(), idExcluir) > 0
    }

    suspend fun obtenerPreguntaSeguridad(correo: String): String? {
        return usuarioDao.obtenerPreguntaSeguridad(correo.trim())?.takeIf { it.isNotBlank() }
    }

    suspend fun verificarRespuestaSeguridad(correo: String, respuesta: String): Boolean {
        val normalizada = normalizarRespuestaSeguridad(respuesta)
        if (normalizada.length < 2) return false
        if (obtenerPreguntaSeguridad(correo) == null) return false

        val guardada = usuarioDao.obtenerRespuestaSeguridad(correo.trim()) ?: return false
        if (guardada.isBlank()) return false

        if (esHashRespuesta(guardada)) return coincideConHash(guardada, normalizada)

        if (normalizarRespuestaSeguridad(guardada) != normalizada) return false
        usuarioDao.actualizarRespuestaSeguridad(correo.trim(), hashRespuestaSeguridad(normalizada))
        return true
    }


    // ---------- Empresas ----------

    fun obtenerEmpresas(): Flow<List<EmpresaEntity>> {
        return equipoDao.obtenerEmpresas()
    }

    suspend fun buscarEmpresa(idEmpresa: Long): EmpresaEntity? {
        return equipoDao.buscarEmpresaPorId(idEmpresa)
    }

    suspend fun crearEmpresa(empresa: EmpresaEntity): Long {
        return equipoDao.insertarEmpresa(empresa)
    }

    suspend fun actualizarEmpresa(empresa: EmpresaEntity): Boolean {
        return equipoDao.actualizarEmpresa(empresa) > 0
    }

    suspend fun existeNombreEmpresa(nombre: String, idExcluir: Long = 0): Boolean {
        return equipoDao.contarEmpresasPorNombre(nombre.trim(), idExcluir) > 0
    }


    // ---------- Equipos (Sincronizado con ms-equipos:8083 + Room) ----------

    fun obtenerTodosLosEquipos(): Flow<List<EquipoConDetalles>> {
        return equipoDao.obtenerTodos().onStart {
            try {
                val remotos = RetrofitClient.equiposApi.listarTodos().map { it.toEntity() }
                if (remotos.isNotEmpty()) {
                    equipoDao.insertarEquipos(remotos)
                }
            } catch (_: Exception) {
                // Modo offline: Room emite lo que tiene localmente
            }
        }
    }

    fun obtenerEquiposPorCliente(idCliente: Long): Flow<List<EquipoConDetalles>> {
        return equipoDao.obtenerEquiposPorCliente(idCliente).onStart {
            try {
                val remotos = RetrofitClient.equiposApi.listarPorCliente(idCliente).map { it.toEntity() }
                if (remotos.isNotEmpty()) {
                    equipoDao.insertarEquipos(remotos)
                }
            } catch (_: Exception) { }
        }
    }

    fun buscarEquipoPorId(idEquipo: Long): Flow<EquipoEntity?> {
        return equipoDao.buscarEquipoPorId(idEquipo)
    }

    suspend fun crearEquipo(equipo: EquipoEntity): Long {
        val idLocal = equipoDao.insertarEquipo(equipo)
        if (idLocal > 0) {
            try {
                RetrofitClient.equiposApi.crear(
                    EquipoApiDto(
                        codigo = equipo.codigo,
                        tipo = equipo.tipo,
                        instalacion = equipo.instalacion,
                        direccion = equipo.direccion,
                        latitud = equipo.latitud,
                        longitud = equipo.longitud,
                        idCliente = equipo.id_cliente
                    )
                )
            } catch (_: Exception) { }
        }
        return idLocal
    }

    suspend fun actualizarEquipo(equipo: EquipoEntity): Boolean {
        return equipoDao.actualizarEquipo(equipo) > 0
    }

    suspend fun existeCodigoEquipo(codigo: String, idExcluir: Long = 0): Boolean {
        return equipoDao.contarPorCodigo(codigo.trim(), idExcluir) > 0
    }

    suspend fun actualizarUbicacionEquipo(idEquipo: Long, direccion: String, latitud: Double, longitud: Double) {
        equipoDao.actualizarUbicacion(idEquipo, direccion, latitud, longitud)
        try {
            RetrofitClient.equiposApi.actualizarUbicacion(
                id = idEquipo,
                req = UbicacionRequestDto(direccion, latitud, longitud)
            )
        } catch (_: Exception) { }
    }


    // ---------- Protocolos (Sincronizado con ms-protocolos:8084 + Room) ----------

    suspend fun insertarProtocolo(protocolo: ProtocoloEntity): Long {
        val dto = ProtocoloApiDto(
            idEquipo = protocolo.idEquipo,
            idTecnico = protocolo.idTecnico,
            fechaHora = protocolo.fechaHora,
            tipoPrueba = protocolo.tipoPrueba,
            atBt = protocolo.atBt,
            atTierra = protocolo.atTierra,
            btTierra = protocolo.btTierra,
            indicePolarizacion = protocolo.indicePolarizacion,
            resultado = protocolo.resultado,
            observaciones = protocolo.observaciones,
            estado = protocolo.estado,
            foto = protocolo.foto?.let { Base64.encodeToString(it, Base64.NO_WRAP) }
        )

        return try {
            val response = RetrofitClient.protocolosApi.crear(dto)
            if (response.isSuccessful && response.body() != null) {
                protocoloDao.insertarProtocolo(response.body()!!.toEntity())
            } else {
                protocoloDao.insertarProtocolo(protocolo.copy(sincronizado = false))
            }
        } catch (_: Exception) {
            protocoloDao.insertarProtocolo(protocolo.copy(sincronizado = false))
        }
    }

    suspend fun formalizarProtocolo(id: Long): Boolean {
        val localOk = protocoloDao.formalizarProtocolo(id) > 0
        try {
            RetrofitClient.protocolosApi.formalizar(id)
        } catch (_: Exception) { }
        return localOk
    }

    fun listarProtocolosDeCliente(clienteId: Long): Flow<List<ProtocoloConDetalles>> = flow {
        try {
            val remotos = RetrofitClient.protocolosApi.listarPorCliente(clienteId)
                .map { it.toConDetalles() }
            emit(remotos)
        } catch (_: Exception) {
            protocoloDao.listarProtocolosDeCliente(clienteId).collect { emit(it) }
        }
    }

    fun listarProtocolosDeEquipo(equipoId: Long): Flow<List<ProtocoloConDetalles>> = flow {
        try {
            val remotos = RetrofitClient.protocolosApi.listarPorEquipo(equipoId)
                .map { it.toConDetalles() }
            emit(remotos)
        } catch (_: Exception) {
            protocoloDao.listarProtocolosDeEquipo(equipoId).collect { emit(it) }
        }
    }

    suspend fun buscarProtocoloPorId(id: Long): ProtocoloConDetalles? {
        return try {
            val response = RetrofitClient.protocolosApi.obtenerPorId(id)
            if (response.isSuccessful) {
                response.body()?.toConDetalles()
            } else {
                protocoloDao.buscarProtocoloPorId(id).firstOrNull()
            }
        } catch (_: Exception) {
            protocoloDao.buscarProtocoloPorId(id).firstOrNull()
        }
    }

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
            direccionEmpresa = empresaVinculada?.direccion ?: "",
            tienePreguntaSeguridad = this.preguntaSeguridad.isNotBlank()
        )
    }
}