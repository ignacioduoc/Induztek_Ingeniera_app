package com.example.induztek_ingeniera_app.data.remote

import android.util.Base64
import com.example.induztek_ingeniera_app.data.entities.EquipoEntity
import com.example.induztek_ingeniera_app.data.entities.ProtocoloConDetalles
import com.example.induztek_ingeniera_app.data.entities.ProtocoloEntity
import com.example.induztek_ingeniera_app.model.Usuario


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

// ---------------- protocolos (tu compañero) ----------------
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

// ---------------- auth ----------------
data class LoginRequest(
    val correo: String,
    val contrasena: String
)

// La API devuelve el token JWT, quién es y su rol
data class LoginResponse(
    val token: String,
    val usuarioId: Long,
    val rolId: Long,
    val primerIngreso: Boolean
)

// El administrador crea la cuenta (correo + clave inicial) de una persona ya creada en usuarios
data class CredencialRequest(
    val usuarioId: Long,
    val correo: String,
    val contrasena: String
)

// Configurar cuenta (primer ingreso) o cambiar la clave
data class CambiarContrasenaRequest(
    val correo: String,
    val contrasenaActual: String,
    val contrasenaNueva: String
)

data class OlvideRequest(
    val correo: String
)

data class RestablecerRequest(
    val correo: String,
    val codigo: String,
    val contrasenaNueva: String
)

// ---------------- usuarios ----------------
data class RegistroRequest(
    val nombre: String,
    val apellido: String,
    val rut: String,
    val telefono: String,
    val empresaId: Long?,
    val rolId: Long
)

data class ActualizarUsuarioRequest(
    val nombre: String,
    val apellido: String,
    val rut: String,
    val telefono: String,
    val empresaId: Long?,
    val rolId: Long
)

// La API NUNCA devuelve la contraseña (ni el correo: ese vive en auth)
data class UsuarioResponse(
    val id: Long,
    val nombre: String,
    val apellido: String,
    val rut: String,
    val telefono: String,
    val empresaId: Long?,
    val empresaNombre: String?,
    val rolId: Long,
    val rolNombre: String,
    val avatarUrl: String?
)

// ---------------- empresas ----------------
data class EmpresaRequest(
    val nombre: String,
    val direccion: String
)

data class EmpresaResponse(
    val id: Long,
    val nombre: String,
    val direccion: String
)

// ---------------- conversión a modelo de la app ----------------
// Igual que aUsuario() del profe: las pantallas siguen usando Usuario
// y no saben que los datos vienen de una API.
// El correo no viene en UsuarioResponse (vive en auth), por eso se pasa aparte.
// cuentaConfigurada = !primerIngreso: si es false, la app lo manda a Configurar cuenta.
fun UsuarioResponse.aUsuario(correo: String = "", cuentaConfigurada: Boolean = true) = Usuario(
    nombre = nombre,
    correo = correo,
    empresa = empresaNombre ?: "",
    rolId = rolId,
    id = id,
    rolNombre = rolNombre,
    apellido = apellido,
    rut = rut,
    telefono = telefono,
    idEmpresa = empresaId ?: 0L,
    tienePreguntaSeguridad = cuentaConfigurada
)