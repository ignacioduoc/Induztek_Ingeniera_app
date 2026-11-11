package com.example.induztek_ingeniera_app.model


// Todo lo que muestra el formulario de empresa: lo escrito, los errores por campo y el estado de guardado
data class FormularioEmpresaState(
    val idEmpresa: Long = 0,                    // 0 = empresa nueva; distinto de 0 = editando
    val nombre: String = "",
    val direccion: String = "",
    val logo: ByteArray? = null,                // opcional

    val errorNombre: String? = null,
    val errorDireccion: String? = null,
    val errorGeneral: String? = null,

    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val guardado: Boolean = false
) {
    val esEdicion: Boolean get() = idEmpresa != 0L
}