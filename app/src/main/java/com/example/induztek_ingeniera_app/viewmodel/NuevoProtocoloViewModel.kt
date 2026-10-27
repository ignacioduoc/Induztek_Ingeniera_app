package com.example.induztek_ingeniera_app.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.example.induztek_ingeniera_app.data.ConvertidorImagen
import com.example.induztek_ingeniera_app.model.Protocolo
import com.example.induztek_ingeniera_app.model.ProtocoloUiState
import com.example.induztek_ingeniera_app.model.Resultado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class NuevoProtocoloViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProtocoloUiState())

    val uiState: StateFlow<ProtocoloUiState> = _uiState.asStateFlow()

    fun onTipoPruebaSeleccionado(tipo: String) {
        _uiState.update { it.copy(tipoPrueba = tipo) }
    }

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


    fun onFotoTomada(foto: Bitmap) {
        _uiState.update { it.copy(foto = ConvertidorImagen.aBytes(foto)) }
    }


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
            observaciones = estado.observaciones.trim(),
            foto = estado.foto                                                  // null si no hay foto
        )
    }


    private fun validarMedicion(texto: String): String? {
        val numero = texto.replace(',', '.').toDoubleOrNull()
        return when {
            texto.isBlank() -> "Ingresa la medición"
            numero == null -> "Ingresa un número, por ejemplo 12,4"
            numero <= 0 -> "Debe ser mayor que 0"
            else -> null
        }
    }


    private fun aNumero(texto: String): Double = texto.replace(',', '.').toDouble()
}