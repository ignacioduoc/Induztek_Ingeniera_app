package com.example.induztek_ingeniera_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.induztek_ingeniera_app.data.ProtocoloRepository
import com.example.induztek_ingeniera_app.data.entities.EmpresaEntity
import com.example.induztek_ingeniera_app.data.entities.EquipoConDetalles
import com.example.induztek_ingeniera_app.data.entities.EquipoEntity
import com.example.induztek_ingeniera_app.data.entities.ProtocoloConDetalles
import com.example.induztek_ingeniera_app.data.entities.ProtocoloEntity
import com.example.induztek_ingeniera_app.data.entities.UsuarioEntity
import com.example.induztek_ingeniera_app.model.Equipo
import com.example.induztek_ingeniera_app.model.EstadoProtocolo
import com.example.induztek_ingeniera_app.model.Protocolo
import com.example.induztek_ingeniera_app.model.Resultado
import com.example.induztek_ingeniera_app.model.Rol
import com.example.induztek_ingeniera_app.model.Usuario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class ProtocolosViewModel(private val repository: ProtocoloRepository) : ViewModel() {

    // Usuario con sesión iniciada
    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()


    @OptIn(ExperimentalCoroutinesApi::class)
    val equipos: StateFlow<List<Equipo>> = _usuarioActual
        .flatMapLatest { usuario ->
            when (usuario?.rolId) {
                1L -> repository.obtenerTodosLosEquipos().map { lista ->
                    lista.map { it.toDomainModel() }
                }
                2L -> repository.obtenerEquiposPorCliente(usuario.id).map { lista ->
                    lista.map { it.toDomainModel() }
                }
                else -> flowOf<List<Equipo>>(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val protocolos: StateFlow<List<Protocolo>> = _usuarioActual
        .flatMapLatest { usuario ->
            if (usuario != null && usuario.rolId == 2L) { // 2L = Rol Cliente
                repository.listarProtocolosDeCliente(usuario.id).map { listaEntities ->
                    listaEntities.map { it.toDomainModel() }
                }
            } else {
                flowOf<List<Protocolo>>(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    // Tarea 4: datos para el formulario "Agregar equipo".
    // Empiezan a cargar después del login (como "equipos"), cuando los datos de ejemplo ya existen.
    @OptIn(ExperimentalCoroutinesApi::class)
    val empresas: StateFlow<List<EmpresaEntity>> = _usuarioActual
        .flatMapLatest { usuario ->
            if (usuario == null) flowOf<List<EmpresaEntity>>(emptyList())
            else repository.obtenerEmpresas()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Solo los usuarios con rol Cliente (los posibles dueños de un equipo)
    @OptIn(ExperimentalCoroutinesApi::class)
    val clientes: StateFlow<List<UsuarioEntity>> = _usuarioActual
        .flatMapLatest { usuario ->
            if (usuario == null) flowOf<List<UsuarioEntity>>(emptyList())
            else repository.listarUsuarios().map { lista -> lista.filter { it.idRol == Rol.ID_CLIENTE } }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    fun registrarIngreso(usuario: Usuario) {
        _usuarioActual.value = usuario
    }


    fun actualizarPerfil(nombre: String, correo: String, fotoPerfil: ByteArray?) {
        val usuario = _usuarioActual.value ?: return
        viewModelScope.launch {
            repository.actualizarPerfilUsuario(usuario.id, nombre, correo, fotoPerfil)
            _usuarioActual.value = usuario.copy(
                nombre = nombre,
                correo = correo,
                fotoPerfil = fotoPerfil
            )
        }
    }


    // Tarea 4: el técnico agrega un equipo. onResultado(false) = el código ya existía.
    fun agregarEquipo(
        codigo: String,
        tipo: String,
        instalacion: String,
        idEmpresa: Long,
        idCliente: Long,
        direccion: String,
        latitud: Double,
        longitud: Double,
        onResultado: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            if (repository.existeCodigoEquipo(codigo)) {
                onResultado(false)
                return@launch
            }
            val nuevoId = repository.crearEquipo(
                EquipoEntity(
                    codigo = codigo.trim(),
                    tipo = tipo.trim(),
                    instalacion = instalacion.trim(),
                    direccion = direccion,
                    latitud = latitud,
                    longitud = longitud,
                    id_cliente = idCliente,
                    id_empresa = idEmpresa
                )
            )
            onResultado(nuevoId != -1L)
        }
    }


    fun actualizarUbicacionEquipo(idEquipo: Long, direccion: String, latitud: Double, longitud: Double) {
        viewModelScope.launch {
            repository.actualizarUbicacionEquipo(idEquipo, direccion, latitud, longitud)
        }
    }


    fun guardarProtocolo(protocolo: Protocolo) {
        viewModelScope.launch {
            val entity = ProtocoloEntity(
                idProtocolo = protocolo.id,
                idEquipo = protocolo.equipoId,
                idTecnico = protocolo.tecnicoId,
                fechaHora = protocolo.fechaHora,
                tipoPrueba = protocolo.tipoPrueba,
                atBt = protocolo.atBt,
                atTierra = protocolo.atTierra,
                btTierra = protocolo.btTierra,
                indicePolarizacion = protocolo.indicePolarizacion,
                resultado = protocolo.resultado.name,
                observaciones = protocolo.observaciones,
                estado = protocolo.estado.name,
                foto = protocolo.foto
            )
            repository.insertarProtocolo(entity)
        }
    }


    fun formalizar(id: Long) {
        viewModelScope.launch {
            repository.formalizarProtocolo(id)
        }
    }


    fun cerrarSesion() {
        _usuarioActual.value = null
    }


    fun obtenerProtocolo(id: Long, onResultado: (Protocolo?) -> Unit) {
        viewModelScope.launch {
            val entity = repository.buscarProtocoloPorId(id)
            onResultado(entity?.toDomainModel())
        }
    }


    fun historialDe(equipoId: Long): StateFlow<List<Protocolo>> {
        return repository.listarProtocolosDeEquipo(equipoId)
            .map { lista -> lista.map { it.toDomainModel() } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun obtenerEquipo(id: Long): Equipo? {
        return equipos.value.find { it.id == id }
    }


    private fun EquipoConDetalles.toDomainModel(): Equipo {
        return Equipo(
            id = this.equipo.id_equipo,
            codigo = this.equipo.codigo,
            tipo = this.equipo.tipo,
            instalacion = this.equipo.instalacion,
            direccion = this.equipo.direccion,
            latitud = this.equipo.latitud,
            longitud = this.equipo.longitud,
            clienteId = this.equipo.id_cliente,
            idEmpresa = this.equipo.id_empresa ?: 0L,
            empresaCliente = this.nombreEmpresa,
            direccionEmpresa = this.direccionEmpresa,
            nombreCliente = this.nombreCliente
        )
    }

    private fun ProtocoloConDetalles.toDomainModel(): Protocolo {
        return Protocolo(
            id = this.protocolo.idProtocolo,
            equipoId = this.protocolo.idEquipo,
            tecnicoId = this.protocolo.idTecnico,
            fechaHora = this.protocolo.fechaHora,
            tipoPrueba = this.protocolo.tipoPrueba,
            atBt = this.protocolo.atBt,
            atTierra = this.protocolo.atTierra,
            btTierra = this.protocolo.btTierra,
            indicePolarizacion = this.protocolo.indicePolarizacion,
            resultado = Resultado.valueOf(this.protocolo.resultado),
            observaciones = this.protocolo.observaciones,
            estado = EstadoProtocolo.valueOf(this.protocolo.estado),
            foto = this.protocolo.foto,
            codigoEquipo = this.codigoEquipo,
            tipoEquipo = this.tipoEquipo,
            instalacion = this.instalacion,
            nombreTecnico = this.nombreTecnico
        )
    }
}