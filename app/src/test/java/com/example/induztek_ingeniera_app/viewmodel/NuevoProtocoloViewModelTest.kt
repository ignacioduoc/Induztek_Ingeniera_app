package com.example.induztek_ingeniera_app.viewmodel

import com.example.induztek_ingeniera_app.model.Resultado
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

// =====================================================================
//  NUEVO (Pruebas unitarias) — NuevoProtocoloViewModelTest.kt
//  ---------------------------------------------------------------------
//  Truco: llenarProtocoloValido() deja TODO correcto y cada prueba
//  "rompe" solo UNA cosa.
// =====================================================================

class NuevoProtocoloViewModelTest {

    private lateinit var viewModel: NuevoProtocoloViewModel

    @Before
    fun preparar() {
        viewModel = NuevoProtocoloViewModel()
    }

    // Función de apoyo (no es una prueba: no tiene @Test)
    private fun llenarProtocoloValido() {
        viewModel.onAtBtCambia("12,4")
        viewModel.onAtTierraCambia("8,5")
        viewModel.onBtTierraCambia("6,2")
        viewModel.onIndicePolarizacionCambia("2,5")
        viewModel.onResultadoSeleccionado(Resultado.APROBADO)
    }

    @Test
    fun onGuardar_conDatosValidos_devuelveProtocolo() {
        llenarProtocoloValido()

        val protocolo = viewModel.onGuardar(1, 1)

        assertNotNull(protocolo)
    }

    @Test
    fun onGuardar_conMedicionVacia_devuelveNull() {
        llenarProtocoloValido()
        viewModel.onAtBtCambia("")   // rompemos SOLO una medición

        val protocolo = viewModel.onGuardar(1, 1)

        assertNull(protocolo)
        assertEquals("Ingresa la medición", viewModel.uiState.value.errorAtBt)
    }

    @Test
    fun onGuardar_rechazadoSinObservaciones_devuelveNull() {
        llenarProtocoloValido()
        viewModel.onResultadoSeleccionado(Resultado.RECHAZADO)   // rompemos SOLO el resultado

        val protocolo = viewModel.onGuardar(1, 1)

        assertNull(protocolo)
    }
}