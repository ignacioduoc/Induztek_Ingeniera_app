package com.example.induztek_ingeniera_app.viewmodel

import com.example.induztek_ingeniera_app.data.FakeEquipoDao
import com.example.induztek_ingeniera_app.data.FakeProtocoloDao
import com.example.induztek_ingeniera_app.data.FakeUsuarioDao
import com.example.induztek_ingeniera_app.data.ProtocoloRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

// =====================================================================
//  NUEVO (Pruebas unitarias) — RestablecerContrasenaViewModelTest.kt
// =====================================================================

class RestablecerContrasenaViewModelTest {

    private lateinit var viewModel: RestablecerContrasenaViewModel

    @Before
    fun preparar() {
        val repositorio = ProtocoloRepository(FakeUsuarioDao(), FakeProtocoloDao(), FakeEquipoDao())
        viewModel = RestablecerContrasenaViewModel(repositorio)
    }

    @Test
    fun onContinuar_conCorreoVacio_muestraError() {
        viewModel.onContinuar()

        assertEquals("Ingresa tu correo", viewModel.uiState.value.errorCorreo)
    }

    @Test
    fun onContinuar_conCorreoInvalido_muestraError() {
        viewModel.onCorreoCambia("cliente")

        viewModel.onContinuar()

        assertEquals("Ingresa un correo válido", viewModel.uiState.value.errorCorreo)
    }
}