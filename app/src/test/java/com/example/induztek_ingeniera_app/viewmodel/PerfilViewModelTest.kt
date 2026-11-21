package com.example.induztek_ingeniera_app.viewmodel

import com.example.induztek_ingeniera_app.data.FakeEquipoDao
import com.example.induztek_ingeniera_app.data.FakeProtocoloDao
import com.example.induztek_ingeniera_app.data.FakeUsuarioDao
import com.example.induztek_ingeniera_app.data.ProtocoloRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

// =====================================================================
//  NUEVO (Pruebas unitarias) — PerfilViewModelTest.kt
// =====================================================================

class PerfilViewModelTest {

    private lateinit var viewModel: PerfilViewModel

    @Before
    fun preparar() {
        val repositorio = ProtocoloRepository(FakeUsuarioDao(), FakeProtocoloDao(), FakeEquipoDao())
        viewModel = PerfilViewModel(repositorio)
    }

    @Test
    fun onGuardar_conNombreConNumeros_muestraError() {
        viewModel.onNombreCambia("M4tias")

        viewModel.onGuardar(null)

        assertEquals("Usa solo letras (mínimo 2)", viewModel.formulario.value.errorNombre)
    }

    @Test
    fun onGuardar_conTelefonoCorto_muestraError() {
        viewModel.onTelefonoCambia("91234")

        viewModel.onGuardar(null)

        assertEquals("Ingresa 9 dígitos, parte con 9 (Ej: 912345678)", viewModel.formulario.value.errorTelefono)
    }
}