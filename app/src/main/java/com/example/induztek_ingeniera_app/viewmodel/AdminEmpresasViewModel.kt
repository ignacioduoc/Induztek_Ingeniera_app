package com.example.induztek_ingeniera_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.induztek_ingeniera_app.data.ProtocoloRepository
import com.example.induztek_ingeniera_app.data.entities.EmpresaEntity
import com.example.induztek_ingeniera_app.model.FormularioEmpresaState
import com.example.induztek_ingeniera_app.ui.utils.Validaciones
import com.example.induztek_ingeniera_app.ui.utils.eliminarImagenSiExiste
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdminEmpresasViewModel(private val repository: ProtocoloRepository) : ViewModel() {

    // ---------- Lista ----------

    val empresas: StateFlow<List<EmpresaEntity>> = repository.obtenerEmpresas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------- Formulario (crear / editar) ----------

    private val _formulario = MutableStateFlow(FormularioEmpresaState())
    val formulario: StateFlow<FormularioEmpresaState> = _formulario.asStateFlow()

    private var logoOriginal: String? = null

    fun abrirFormulario(idEmpresa: Long) {
        if (idEmpresa == 0L) {
            logoOriginal = null
            _formulario.value = FormularioEmpresaState()
            return
        }
        _formulario.value = FormularioEmpresaState(idEmpresa = idEmpresa, cargando = true)
        viewModelScope.launch {
            val e = repository.buscarEmpresa(idEmpresa)
            if (e != null) {
                logoOriginal = e.logo
                _formulario.value = FormularioEmpresaState(
                    idEmpresa = e.idEmpresa,
                    nombre = e.nombre,
                    direccion = e.direccion,
                    logo = e.logo
                )
            } else {
                _formulario.update { it.copy(cargando = false, errorGeneral = "No se encontró la empresa") }
            }
        }
    }

    fun onNombreCambia(v: String) = _formulario.update { it.copy(nombre = v, errorNombre = null) }
    fun onDireccionCambia(v: String) = _formulario.update { it.copy(direccion = v, errorDireccion = null) }

    fun onLogoCambia(nuevaRuta: String?) {
        val anteriorEnFormulario = _formulario.value.logo
        // Si había una imagen temporal distinta a la original de BD, la limpiamos
        if (anteriorEnFormulario != null && anteriorEnFormulario != logoOriginal && anteriorEnFormulario != nuevaRuta) {
            eliminarImagenSiExiste(anteriorEnFormulario)
        }
        _formulario.update { it.copy(logo = nuevaRuta, errorGeneral = null) }
    }

    fun limpiarGuardado() = _formulario.update { it.copy(guardado = false) }

    fun onGuardar() {
        val f = _formulario.value
        val nombre = f.nombre.trim()
        val direccion = f.direccion.trim()

        val errorNombre = when {
            nombre.isBlank() -> "Ingresa el nombre de la empresa"
            !Validaciones.nombreEmpresaValido(nombre) -> "Entre 3 y 60 caracteres (letras, números y . , & -)"
            else -> null
        }
        val errorDireccion = when {
            direccion.isBlank() -> "Ingresa la dirección"
            !Validaciones.direccionValida(direccion) -> "Entre 5 y 100 caracteres"
            else -> null
        }
        if (errorNombre != null || errorDireccion != null) {
            _formulario.update { it.copy(errorNombre = errorNombre, errorDireccion = errorDireccion) }
            return
        }

        _formulario.update { it.copy(guardando = true, errorGeneral = null) }
        viewModelScope.launch {
            if (repository.existeNombreEmpresa(nombre, f.idEmpresa)) {
                _formulario.update {
                    it.copy(guardando = false, errorNombre = "Ya existe una empresa con ese nombre")
                }
                return@launch
            }

            // Si se reemplazó o quitó el logo original al editar, borramos el antiguo de disco
            if (logoOriginal != null && logoOriginal != f.logo) {
                eliminarImagenSiExiste(logoOriginal)
            }

            val entidad = EmpresaEntity(
                idEmpresa = f.idEmpresa,
                nombre = nombre,
                direccion = direccion,
                logo = f.logo
            )
            val exito = if (f.esEdicion) repository.actualizarEmpresa(entidad) else repository.crearEmpresa(entidad) > 0
            if (exito) {
                logoOriginal = f.logo
            }
            _formulario.update {
                it.copy(
                    guardando = false,
                    guardado = exito,
                    errorGeneral = if (exito) null else "No se pudo guardar la empresa"
                )
            }
        }
    }
}