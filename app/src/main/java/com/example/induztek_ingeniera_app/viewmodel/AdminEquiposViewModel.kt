package com.example.induztek_ingeniera_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.induztek_ingeniera_app.data.ProtocoloRepository
import com.example.induztek_ingeniera_app.data.entities.EmpresaEntity
import com.example.induztek_ingeniera_app.data.entities.EquipoEntity
import com.example.induztek_ingeniera_app.data.entities.UsuarioEntity
import com.example.induztek_ingeniera_app.model.Equipo
import com.example.induztek_ingeniera_app.model.FormularioEquipoState
import com.example.induztek_ingeniera_app.model.Rol
import com.example.induztek_ingeniera_app.ui.utils.Validaciones
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AdminEquiposViewModel(private val repository: ProtocoloRepository) : ViewModel() {

    // ---------- Lista ----------

    // Cada equipo ya viene con su empresa, la dirección de la empresa y el nombre del cliente dueño
    val equipos: StateFlow<List<Equipo>> = repository.obtenerTodosLosEquipos()
        .map { lista ->
            lista.map { d ->
                Equipo(
                    id = d.equipo.id_equipo,
                    codigo = d.equipo.codigo,
                    tipo = d.equipo.tipo,
                    instalacion = d.equipo.instalacion,
                    direccion = d.equipo.direccion,
                    latitud = d.equipo.latitud,
                    longitud = d.equipo.longitud,
                    clienteId = d.equipo.id_cliente,
                    idEmpresa = d.equipo.id_empresa ?: 0L,
                    empresaCliente = d.nombreEmpresa,
                    direccionEmpresa = d.direccionEmpresa,
                    nombreCliente = d.nombreCliente
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Para las listas desplegables del formulario
    val empresas: StateFlow<List<EmpresaEntity>> = repository.obtenerEmpresas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val clientes: StateFlow<List<UsuarioEntity>> = repository.listarUsuarios()
        .map { usuarios -> usuarios.filter { it.idRol == Rol.ID_CLIENTE } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // ---------- Formulario (crear / editar) ----------

    private val _formulario = MutableStateFlow(FormularioEquipoState())
    val formulario: StateFlow<FormularioEquipoState> = _formulario.asStateFlow()

    // Para separar "Transformador 500 kVA" o "Motor 75 kW" en nombre, valor y unidad
    private val formatoTipoGuardado = Regex("^(.+) (\\d+) (kVA|kW|kV|A)$")

    // idEquipo = 0 -> formulario vacío para crear. Otro id -> carga ese equipo para editarlo.
    fun abrirFormulario(idEquipo: Long) {
        if (idEquipo == 0L) {
            _formulario.value = FormularioEquipoState()
            return
        }
        _formulario.value = FormularioEquipoState(idEquipo = idEquipo, cargando = true)
        viewModelScope.launch {
            val e = repository.buscarEquipoPorId(idEquipo).firstOrNull()
            if (e == null) {
                _formulario.update { it.copy(cargando = false, errorGeneral = "No se encontró el equipo") }
                return@launch
            }

            val partes = formatoTipoGuardado.matchEntire(e.tipo)
            val nombre = partes?.groupValues?.get(1) ?: e.tipo
            val valor = partes?.groupValues?.get(2) ?: ""
            val unidad = partes?.groupValues?.get(3) ?: "kVA"
            val esTransformador = nombre == Validaciones.TIPO_TRANSFORMADOR && unidad == "kVA"

            _formulario.value = FormularioEquipoState(
                idEquipo = e.id_equipo,
                codigo = e.codigo.removePrefix(Validaciones.PREFIJO_CODIGO),
                tipoElegido = if (esTransformador) Validaciones.TIPO_TRANSFORMADOR else Validaciones.TIPO_OTRO,
                nombreTipo = if (esTransformador) "" else nombre,
                valorTipo = valor,
                unidadTipo = unidad,
                instalacion = e.instalacion,
                idEmpresa = e.id_empresa ?: 0L,
                idCliente = e.id_cliente,
                direccionOriginal = e.direccion,
                latitudOriginal = e.latitud,
                longitudOriginal = e.longitud,
                idEmpresaOriginal = e.id_empresa ?: 0L
            )
        }
    }

    // Cada vez que se escribe en un campo, se borra su error
    fun onCodigoCambia(v: String) =
        _formulario.update { it.copy(codigo = v.filter { c -> c.isDigit() }.take(5), errorCodigo = null) }

    fun onTipoCambia(v: String) =
        _formulario.update { it.copy(tipoElegido = v, errorTipo = null, errorNombreTipo = null, errorValorTipo = null) }

    fun onNombreTipoCambia(v: String) = _formulario.update { it.copy(nombreTipo = v, errorNombreTipo = null) }

    fun onValorTipoCambia(v: String) =
        _formulario.update { it.copy(valorTipo = v.filter { c -> c.isDigit() }.take(5), errorValorTipo = null) }

    fun onUnidadCambia(v: String) = _formulario.update { it.copy(unidadTipo = v) }
    fun onInstalacionCambia(v: String) = _formulario.update { it.copy(instalacion = v, errorInstalacion = null) }

    // Al cambiar la empresa, el cliente se vuelve a elegir (cada cliente pertenece a una empresa)
    fun onEmpresaCambia(v: Long) =
        _formulario.update { it.copy(idEmpresa = v, idCliente = 0, errorEmpresa = null, errorCliente = null) }

    fun onClienteCambia(v: Long) = _formulario.update { it.copy(idCliente = v, errorCliente = null) }

    fun limpiarGuardado() = _formulario.update { it.copy(guardado = false) }

    /**
     * buscarCoordenadas: la pantalla pasa la búsqueda con el Geocoder (necesita Context).
     * Se usa solo si el equipo es nuevo o cambió de empresa, para ubicarlo en el mapa.
     */
    fun onGuardar(buscarCoordenadas: (String) -> Pair<Double, Double>?) {
        val f = _formulario.value

        // 1) Validaciones que no necesitan la base de datos
        val errorCodigo = when {
            f.codigo.isBlank() -> "Ingresa el código del equipo"
            !Validaciones.codigoEquipoValido(f.codigo) -> "Escribe solo números, de 3 a 5 dígitos (Ej: 0142)"
            else -> null
        }
        val errorTipo = if (f.tipoElegido.isBlank()) "Elige el tipo de equipo" else null
        val esOtro = f.tipoElegido == Validaciones.TIPO_OTRO
        val errorNombreTipo = when {
            !esOtro -> null
            f.nombreTipo.isBlank() -> "Ingresa el nombre del tipo"
            !Validaciones.nombreTipoValido(f.nombreTipo) -> "Usa solo letras (mínimo 3)"
            else -> null
        }
        val errorValorTipo =
            if (f.tipoElegido.isNotBlank() && !Validaciones.valorTipoValido(f.valorTipo)) "Ingresa un número de 1 a 99999" else null
        val errorInstalacion = if (!Validaciones.instalacionValida(f.instalacion)) "Ingresa la instalación (3 a 60 caracteres)" else null
        val errorEmpresa = if (f.idEmpresa == 0L) "Elige la empresa" else null
        val empresaSinClientes = f.idEmpresa != 0L && clientes.value.none { it.idEmpresa == f.idEmpresa }
        val errorCliente = when {
            f.idEmpresa == 0L -> null
            empresaSinClientes -> "Esta empresa no tiene clientes registrados"
            f.idCliente == 0L -> "Elige el cliente dueño"
            else -> null
        }

        val hayError = listOf(
            errorCodigo, errorTipo, errorNombreTipo, errorValorTipo, errorInstalacion, errorEmpresa, errorCliente
        ).any { it != null }

        if (hayError) {
            _formulario.update {
                it.copy(
                    errorCodigo = errorCodigo, errorTipo = errorTipo, errorNombreTipo = errorNombreTipo,
                    errorValorTipo = errorValorTipo, errorInstalacion = errorInstalacion,
                    errorEmpresa = errorEmpresa, errorCliente = errorCliente
                )
            }
            return
        }

        // 2) Código repetido (consulta a la base). Al editar se excluye al propio equipo.
        _formulario.update { it.copy(guardando = true, errorGeneral = null) }
        viewModelScope.launch {
            if (repository.existeCodigoEquipo(f.codigoCompleto, f.idEquipo)) {
                _formulario.update { it.copy(guardando = false, errorCodigo = "Ya existe un equipo con ese código") }
                return@launch
            }

            // 3) Ubicación: la dirección del equipo es la de su empresa
            val direccionEmpresa = empresas.value.firstOrNull { it.idEmpresa == f.idEmpresa }?.direccion.orEmpty()
            val conservarUbicacion = f.esEdicion && f.idEmpresa == f.idEmpresaOriginal

            val direccion: String
            val latitud: Double
            val longitud: Double
            when {
                conservarUbicacion -> {
                    direccion = f.direccionOriginal
                    latitud = f.latitudOriginal
                    longitud = f.longitudOriginal
                }
                direccionEmpresa.isBlank() -> {
                    direccion = ""; latitud = 0.0; longitud = 0.0
                }
                else -> {
                    // El Geocoder se queda esperando la red: se hace fuera del hilo principal
                    val punto = withContext(Dispatchers.IO) { buscarCoordenadas(direccionEmpresa) }
                    direccion = direccionEmpresa
                    latitud = punto?.first ?: 0.0
                    longitud = punto?.second ?: 0.0
                }
            }

            // 4) Guardar
            val entidad = EquipoEntity(
                id_equipo = f.idEquipo,
                codigo = f.codigoCompleto,
                tipo = f.tipoFinal,
                instalacion = f.instalacion.trim(),
                direccion = direccion,
                latitud = latitud,
                longitud = longitud,
                id_cliente = f.idCliente,
                id_empresa = f.idEmpresa
            )
            val exito = if (f.esEdicion) repository.actualizarEquipo(entidad) else repository.crearEquipo(entidad) > 0
            _formulario.update {
                it.copy(
                    guardando = false,
                    guardado = exito,
                    errorGeneral = if (exito) null else "No se pudo guardar el equipo"
                )
            }
        }
    }
}