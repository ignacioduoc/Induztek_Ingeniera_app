package com.example.induztek_ingeniera_app.data.network

import android.util.Base64
import com.example.induztek_ingeniera_app.data.entities.EquipoEntity
import com.example.induztek_ingeniera_app.data.entities.ProtocoloConDetalles
import com.example.induztek_ingeniera_app.data.entities.ProtocoloEntity

data class UbicacionRequestDto(
    val direccion: String,
    val latitud: Double,
    val longitud: Double
)

data class EquipoApiDto(
    val idEquipo: Long? = null,
    val codigo: String,
    val tipo: String,
    val instalacion: String?,
    val direccion: String?,
    val latitud: Double?,
    val longitud: Double?,
    val idCliente: Long
) {
    fun toEntity() = EquipoEntity(
        id_equipo = idEquipo ?: 0L,
        codigo = codigo,
        tipo = tipo,
        instalacion = instalacion ?: "",
        direccion = direccion ?: "",
        latitud = latitud ?: 0.0,
        longitud = longitud ?: 0.0,
        id_cliente = idCliente
    )
}

data class ProtocoloApiDto(
    val idProtocolo: Long? = null,
    val idEquipo: Long,
    val idTecnico: Long,
    val fechaHora: Long,
    val tipoPrueba: String,
    val atBt: Double?,
    val atTierra: Double?,
    val btTierra: Double?,
    val indicePolarizacion: Double?,
    val resultado: String,
    val observaciones: String?,
    val estado: String,
    val foto: String? = null
) {
    fun toEntity() = ProtocoloEntity(
        idProtocolo = idProtocolo ?: 0L,
        idEquipo = idEquipo,
        idTecnico = idTecnico,
        fechaHora = fechaHora,
        tipoPrueba = tipoPrueba,
        atBt = atBt ?: 0.0,
        atTierra = atTierra ?: 0.0,
        btTierra = btTierra ?: 0.0,
        indicePolarizacion = indicePolarizacion ?: 0.0,
        resultado = resultado,
        observaciones = observaciones ?: "",
        estado = estado,
        foto = foto?.let { Base64.decode(it, Base64.DEFAULT) },
        sincronizado = true
    )
}

data class ProtocoloDetalleApiDto(
    val protocolo: ProtocoloApiDto,
    val codigoEquipo: String,
    val tipoEquipo: String,
    val instalacion: String,
    val direccionEquipo: String? = "",
    val nombreTecnico: String
) {
    fun toConDetalles() = ProtocoloConDetalles(
        protocolo = protocolo.toEntity(),
        codigoEquipo = codigoEquipo,
        tipoEquipo = tipoEquipo,
        instalacion = instalacion,
        direccionEquipo = direccionEquipo ?: "",
        nombreTecnico = nombreTecnico
    )
}