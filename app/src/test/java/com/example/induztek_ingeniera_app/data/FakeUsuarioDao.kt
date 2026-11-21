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
import kotlinx.coroutines.flow.Flow

// =====================================================================
//  NUEVO (Pruebas unitarias) — FakeUsuarioDao.kt
//  ---------------------------------------------------------------------
//  Una prueba unitaria NO debe depender de la base de datos:
//  Room necesita un teléfono o emulador.
//
//  Solución: DAO "falsos". Implementan las MISMAS interfaces que Room,
//  pero no hacen nada. El Repository no nota la diferencia.
//
//  Atajo en Android Studio: escribir "class FakeUsuarioDao : UsuarioDao",
//  Alt+Enter -> Implement members -> seleccionar todos -> OK.
// =====================================================================

// Las que no usamos en las pruebas quedan con TODO() (si alguien las llama, la prueba falla)
class FakeUsuarioDao : UsuarioDao {
    override suspend fun insertarUsuario(usuario: UsuarioEntity): Long = TODO()
    override suspend fun actualizarUsuario(usuario: UsuarioEntity): Int = TODO()
    override suspend fun iniciarSesion(correo: String, contrasena: String): UsuarioEntity? = TODO()
    override fun buscarUsuarioPorId(id: Long): Flow<UsuarioEntity?> = TODO()
    override fun listarUsuarios(): Flow<List<UsuarioEntity>> = TODO()
    override suspend fun obtenerCantidadUsuarios(): Int = TODO()
    override suspend fun actualizarPerfil(idUsuario: Long, nombre: String, correo: String, fotoPerfil: ByteArray?) = TODO()
    override suspend fun actualizarContrasena(correo: String, nueva: String): Int = TODO()
    override suspend fun contarPorCorreo(correo: String, idExcluir: Long): Int = TODO()
    override suspend fun contarPorRut(rut: String, idExcluir: Long): Int = TODO()
    override suspend fun obtenerPreguntaSeguridad(correo: String): String? = TODO()
    override suspend fun obtenerRespuestaSeguridad(correo: String): String? = TODO()
    override suspend fun actualizarRespuestaSeguridad(correo: String, respuesta: String): Int = TODO()
}

class FakeProtocoloDao : ProtocoloDao {
    override suspend fun insertarProtocolo(protocolo: ProtocoloEntity): Long = TODO()
    override fun listarProtocolosDeCliente(clienteId: Long): Flow<List<ProtocoloConDetalles>> = TODO()
    override fun listarProtocolosDeEquipo(equipoId: Long): Flow<List<ProtocoloConDetalles>> = TODO()
    override fun buscarProtocoloPorId(id: Long): Flow<ProtocoloConDetalles?> = TODO()
    override suspend fun formalizarProtocolo(id: Long): Int = TODO()
}

class FakeEquipoDao : EquipoDao {
    override suspend fun insertarEquipos(equipos: List<EquipoEntity>) = TODO()
    override suspend fun insertarEquipo(equipo: EquipoEntity): Long = TODO()
    override suspend fun actualizarEquipo(equipo: EquipoEntity): Int = TODO()
    override fun obtenerTodos(): Flow<List<EquipoConDetalles>> = TODO()
    override fun obtenerEquiposPorCliente(idCliente: Long): Flow<List<EquipoConDetalles>> = TODO()
    override fun obtenerEquiposPorTecnico(idTecnico: Long): Flow<List<EquipoConDetalles>> = TODO()
    override fun buscarEquipoPorId(idEquipo: Long): Flow<EquipoEntity?> = TODO()
    override suspend fun actualizarUbicacion(idEquipo: Long, direccion: String, lat: Double, lng: Double) = TODO()
    override suspend fun asignarTecnico(idEquipo: Long, idTecnico: Long?) = TODO()
    override suspend fun contarPorCodigo(codigo: String, idExcluir: Long): Int = TODO()
    override suspend fun insertarEmpresa(empresa: EmpresaEntity): Long = TODO()
    override suspend fun actualizarEmpresa(empresa: EmpresaEntity): Int = TODO()
    override fun obtenerEmpresas(): Flow<List<EmpresaEntity>> = TODO()
    override suspend fun buscarEmpresaPorId(idEmpresa: Long): EmpresaEntity? = TODO()
    override suspend fun contarEmpresasPorNombre(nombre: String, idExcluir: Long): Int = TODO()
}