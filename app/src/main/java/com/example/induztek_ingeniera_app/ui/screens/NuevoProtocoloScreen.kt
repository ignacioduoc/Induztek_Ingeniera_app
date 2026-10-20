package com.example.induztek_ingeniera_app.viewmodel

import androidx.lifecycle.ViewModel
import com.example.induztek_ingeniera_app.model.Protocolo
import com.example.induztek_ingeniera_app.model.ProtocoloUiState
import com.example.induztek_ingeniera_app.model.Resultado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// =====================================================================
//  NuevoProtocoloViewModel.kt  —  LÓGICA DEL FORMULARIO DEL PROTOCOLO
//  ---------------------------------------------------------------------
//  Igual que el FormularioViewModel del profe: guarda lo que se escribe,
//  valida cada campo y, si todo está bien, ARMA el Protocolo y lo devuelve.
//  No toca la BD: la pantalla le entrega el Protocolo al ViewModel
//  compartido, que lo guarda (como el profe hace con registrar(usuario)).
// =====================================================================

class NuevoProtocoloViewModel : ViewModel() {

    // Privado y mutable: solo el ViewModel lo modifica
    private val _uiState = MutableStateFlow(ProtocoloUiState())

    // Público y de solo lectura: la pantalla lo observa
    val uiState: StateFlow<ProtocoloUiState> = _uiState.asStateFlow()

    fun onTipoPruebaSeleccionado(tipo: String) {
        _uiState.update { it.copy(tipoPrueba = tipo) }
    }

    // MEDICIÓN: una función por medición. Cada letra borra el error de ese campo.
    fun onAtBtCambia(valor: String) {
        _uiState.update { it.copy(atBt = valor, errorAtBt = null) }
    }

    fun onAtTierraCambia(valor: String) {
        _uiState.update { it.copy(atTierra = valor, errorAtTierra = null) }
    }

    fun onBtTierraCambia(valor: String) {
        _uiState.update { it.copy(btTierra = valor, errorBtTierra = null) }
    }

    fun onIndicePolarizacionCambia(valor: String) {
        _uiState.update { it.copy(indicePolarizacion = valor, errorIndicePolarizacion = null) }
    }

    fun onResultadoSeleccionado(resultado: Resultado) {
        _uiState.update { it.copy(resultado = resultado, errorObservaciones = null) }
    }

    fun onObservacionesCambia(valor: String) {
        _uiState.update { it.copy(observaciones = valor, errorObservaciones = null) }
    }

    // Botón "Guardar protocolo": valida cada campo. Si todo está bien, arma el
    // Protocolo con los ids que recibe y lo devuelve; si hay errores, devuelve null.
    fun onGuardar(equipoId: Long, tecnicoId: Long): Protocolo? {
        val estado = _uiState.value

        val errorAtBt = validarMedicion(estado.atBt)                            // MEDICIÓN
        val errorAtTierra = validarMedicion(estado.atTierra)                    // MEDICIÓN
        val errorBtTierra = validarMedicion(estado.btTierra)                    // MEDICIÓN
        val errorIndice = validarMedicion(estado.indicePolarizacion)            // MEDICIÓN
        // Si el resultado NO es "Aprobado", hay que explicar por qué
        val errorObservaciones =
            if (estado.resultado != Resultado.APROBADO && estado.observaciones.isBlank())
                "Explica la observación o el motivo del rechazo" else null

        val hayErrores = listOf(errorAtBt, errorAtTierra, errorBtTierra, errorIndice, errorObservaciones)
            .any { it != null }

        _uiState.update {
            it.copy(
                errorAtBt = errorAtBt,
                errorAtTierra = errorAtTierra,
                errorBtTierra = errorBtTierra,
                errorIndicePolarizacion = errorIndice,
                errorObservaciones = errorObservaciones,
                guardado = !hayErrores
            )
        }

        if (hayErrores) return null

        return Protocolo(
            equipoId = equipoId,
            tecnicoId = tecnicoId,
            fechaHora = estado.fechaHora,
            tipoPrueba = estado.tipoPrueba,
            atBt = aNumero(estado.atBt),                                        // MEDICIÓN
            atTierra = aNumero(estado.atTierra),                                // MEDICIÓN
            btTierra = aNumero(estado.btTierra),                                // MEDICIÓN
            indicePolarizacion = aNumero(estado.indicePolarizacion),            // MEDICIÓN
            resultado = estado.resultado,
            observaciones = estado.observaciones.trim()
        )
    }

    // Regla para TODAS las mediciones: obligatoria, número y mayor que 0.
    // Acepta coma o punto: "12,4" y "12.4" sirven.
    private fun validarMedicion(texto: String): String? {
        val numero = texto.replace(',', '.').toDoubleOrNull()
        return when {
            texto.isBlank() -> "Ingresa la medición"
            numero == null -> "Ingresa un número, por ejemplo 12,4"
            numero <= 0 -> "Debe ser mayor que 0"
            else -> null
        }
    }

    // "12,4" -> 12.4. Solo se llama después de validar, así que siempre es un número.
    private fun aNumero(texto: String): Double = texto.replace(',', '.').toDouble()
}