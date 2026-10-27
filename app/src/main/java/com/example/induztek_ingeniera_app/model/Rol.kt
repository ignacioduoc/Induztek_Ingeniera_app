package com.example.induztek_ingeniera_app.model

data class Rol(
    val id: Long,
    val nombre: String
) {
    companion object {
        const val ID_TECNICO = 1L
        const val ID_CLIENTE = 2L
    }
}