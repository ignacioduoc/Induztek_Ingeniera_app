package com.example.induztek_ingeniera_app.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.example.induztek_ingeniera_app.data.ConvertidorImagen
import com.example.induztek_ingeniera_app.model.Protocolo
import com.example.induztek_ingeniera_app.model.ProtocoloUiState
import com.example.induztek_ingeniera_app.model.Resultado
import com.example.induztek_ingeniera_app.ui.utils.eliminarImagenSiExiste
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

    fun onFotoTomada(context: Context, foto: Bitmap) {
        val rutaAnterior = _uiState.value.foto
        val nuevaRuta = ConvertidorImagen.guardarBitmapEnCarpeta(
            context = context,
            bitmap = foto,
            prefijo = "proto"
        )
        if (nuevaRuta != null) {
            eliminarImagenSiExiste(rutaAnterior)
            _uiState.update { it.copy(foto = nuevaRuta) }
        }
    }

    fun onGuardar(equipoId: Long, tecnicoId: Long): Protocolo? {
        val estado = _uiState.value

        val errorAtBt = validarMedicion(estado.atBt)
        val errorAtTierra = validarMedicion(estado.atTierra)
        val errorBtTierra = validarMedicion(estado.btTierra)
        val errorIndice = validarMedicion(estado.indicePolarizacion)
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
            atBt = aNumero(estado.atBt),
            atTierra = aNumero(estado.atTierra),
            btTierra = aNumero(estado.btTierra),
            indicePolarizacion = aNumero(estado.indicePolarizacion),
            resultado = estado.resultado,
            observaciones = estado.observaciones.trim(),
            foto = estado.foto // Ruta String? en /files/imagenes/ (o null si no hay foto)
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