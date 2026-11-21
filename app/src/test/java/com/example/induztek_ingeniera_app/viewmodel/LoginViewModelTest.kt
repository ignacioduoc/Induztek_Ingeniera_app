package com.example.induztek_ingeniera_app.viewmodel

import com.example.induztek_ingeniera_app.data.FakeEquipoDao
import com.example.induztek_ingeniera_app.data.FakeProtocoloDao
import com.example.induztek_ingeniera_app.data.FakeUsuarioDao
import com.example.induztek_ingeniera_app.data.ProtocoloRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

// =====================================================================
//  NUEVO (Pruebas unitarias) — LoginViewModelTest.kt
//  ---------------------------------------------------------------------
//  Una prueba unitaria revisa UNA pieza pequeña del código (aquí, el
//  LoginViewModel) sin abrir la app, sin emulador y sin servidor.
//
//  Cada prueba sigue el patrón AAA:
//   1. Preparar  -> dejar el ViewModel en el estado que necesito
//   2. Ejecutar  -> llamar a la función que quiero probar
//   3. Verificar -> comprobar que el resultado es el esperado
// =====================================================================

class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel

    // @Before se ejecuta ANTES de CADA @Test
    @Before
    fun preparar() {
        val repositorio = ProtocoloRepository(FakeUsuarioDao(), FakeProtocoloDao(), FakeEquipoDao())
        viewModel = LoginViewModel(repositorio)
    }

    @Test
    fun onIngresar_conCamposVacios_muestraErrores() {
        // Ejecutar (el formulario parte vacío)
        viewModel.onIngresar()

        // Verificar
        assertEquals("Ingresa tu correo", viewModel.uiState.value.errorCorreo)
        assertEquals("Ingresa tu contraseña", viewModel.uiState.value.errorContrasena)
    }

    @Test
    fun onIngresar_conContrasenaCorta_muestraError() {
        // Preparar
        viewModel.onCorreoCambia("tecnico@gmail.com")
        viewModel.onContrasenaCambia("123")

        // Ejecutar
        viewModel.onIngresar()

        // Verificar
        assertEquals("Mínimo 6 caracteres", viewModel.uiState.value.errorContrasena)
    }

    @Test
    fun escribirDespuesDeUnError_borraElMensajeDeError() {
        // Preparar: provocamos el error
        viewModel.onIngresar()

        // Ejecutar: el usuario corrige el correo
        viewModel.onCorreoCambia("t")

        // Verificar: el error del correo desaparece
        assertNull(viewModel.uiState.value.errorCorreo)
    }
}