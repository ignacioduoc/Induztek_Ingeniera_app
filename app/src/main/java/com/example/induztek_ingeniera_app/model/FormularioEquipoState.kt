package com.example.induztek_ingeniera_app.model

import com.example.induztek_ingeniera_app.ui.utils.Validaciones


// Todo lo que muestra el formulario de equipo: lo escrito, los errores por campo y el estado de guardado
data class FormularioEquipoState(
    val idEquipo: Long = 0,                     // 0 = equipo nuevo; distinto de 0 = editando
    val codigo: String = "",                    // solo el número; el "TR-" va fijo
    val tipoElegido: String = "",               // "" / Transformador / OTRO
    val nombreTipo: String = "",                // solo si el tipo es OTRO
    val valorTipo: String = "",                 // potencia o valor (solo número)
    val unidadTipo: String = "kVA",             // solo si el tipo es OTRO
    val instalacion: String = "",
    val idEmpresa: Long = 0,
    val idCliente: Long = 0,

    // Datos originales del equipo (solo al editar): si no cambia la empresa se conserva su ubicación
    val direccionOriginal: String = "",
    val latitudOriginal: Double = 0.0,
    val longitudOriginal: Double = 0.0,
    val idEmpresaOriginal: Long = 0,

    val errorCodigo: String? = null,
    val errorTipo: String? = null,
    val errorNombreTipo: String? = null,
    val errorValorTipo: String? = null,
    val errorInstalacion: String? = null,
    val errorEmpresa: String? = null,
    val errorCliente: String? = null,
    val errorGeneral: String? = null,

    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val guardado: Boolean = false
) {
    val esEdicion: Boolean get() = idEquipo != 0L

    val codigoCompleto: String get() = Validaciones.PREFIJO_CODIGO + codigo.trim()

    // Lo que se guarda como "tipo": "Transformador 500 kVA" o "Motor 75 kW". Vacío si todavía no está completo.
    val tipoFinal: String
        get() {
            val valorOk = Validaciones.valorTipoValido(valorTipo)
            return when {
                tipoElegido == Validaciones.TIPO_TRANSFORMADOR && valorOk ->
                    "${Validaciones.TIPO_TRANSFORMADOR} ${valorTipo.toInt()} kVA"
                tipoElegido == Validaciones.TIPO_OTRO && Validaciones.nombreTipoValido(nombreTipo) && valorOk ->
                    "${Validaciones.conMayusculaInicial(nombreTipo)} ${valorTipo.toInt()} $unidadTipo"
                else -> ""
            }
        }
}