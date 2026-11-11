package com.example.induztek_ingeniera_app.ui.utils


// Reglas de validación compartidas (formulario de usuarios; después también el perfil)
object Validaciones {

    // Preguntas entre las que se elige al crear o editar un usuario
    val PREGUNTAS_SEGURIDAD = listOf(
        "¿Cuál es el nombre de tu primera mascota?",
        "¿En qué ciudad naciste?",
        "¿Cuál es el nombre de tu primer colegio?"
    )

    // Equipos: el código siempre empieza con "TR-" y el resto son solo números
    const val PREFIJO_CODIGO = "TR-"
    const val TIPO_TRANSFORMADOR = "Transformador"
    const val TIPO_OTRO = "OTRO"
    val UNIDADES_TIPO = listOf("kVA", "kW", "kV", "A")

    private val regexCodigoEquipo = Regex("^\\d{3,5}$")                  // Ej: 0142 -> TR-0142
    private val regexNombreTipo = Regex("^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ ]{3,30}$") // Ej: Motor, Interruptor

    private val regexNombre = Regex("^[\\p{L}]+( [\\p{L}]+)*$")          // solo letras y espacios
    private val regexCorreo = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val regexTelefono = Regex("^9\\d{8}$")                       // celular chileno: 9 dígitos que parten con 9

    fun nombreValido(texto: String): Boolean {
        val limpio = texto.trim()
        return limpio.length in 2..40 && regexNombre.matches(limpio)
    }

    private val regexNombreEmpresa = Regex("^[\\p{L}\\p{N}][\\p{L}\\p{N} .,&'-]{2,59}$")   // letras, números y . , & ' -

    fun nombreEmpresaValido(texto: String): Boolean = regexNombreEmpresa.matches(texto.trim())

    // Dirección: texto libre, pero con un largo razonable (Ej: "Camino Los Maitenes 1200, Lampa")
    fun direccionValida(texto: String): Boolean = texto.trim().length in 5..100

    fun codigoEquipoValido(numero: String): Boolean = regexCodigoEquipo.matches(numero.trim())

    fun nombreTipoValido(texto: String): Boolean = regexNombreTipo.matches(texto.trim())

    fun valorTipoValido(texto: String): Boolean = texto.toIntOrNull()?.let { it in 1..99999 } == true

    fun instalacionValida(texto: String): Boolean = texto.trim().length in 3..60

    // "puerto MONTT" -> "Puerto Montt" (cada palabra con solo la primera letra en mayúscula)
    fun conMayusculaInicial(texto: String): String =
        texto.trim().split(" ").filter { it.isNotBlank() }
            .joinToString(" ") { palabra -> palabra.lowercase().replaceFirstChar { it.uppercase() } }

    fun correoValido(texto: String): Boolean = regexCorreo.matches(texto.trim())

    fun telefonoValido(nueveDigitos: String): Boolean = regexTelefono.matches(nueveDigitos)

    /**
     * Deja el RUT como "16482913-8" (sin puntos y con guion) si es válido.
     * Devuelve null si el formato es malo o el dígito verificador no coincide.
     */
    fun normalizarRut(texto: String): String? {
        val limpio = texto.filter { it.isLetterOrDigit() }.uppercase()
        if (limpio.length !in 8..9) return null                          // 7 u 8 dígitos + verificador
        val cuerpo = limpio.dropLast(1)
        val dv = limpio.last()
        if (!cuerpo.all { it.isDigit() }) return null
        return if (dv == calcularDigitoVerificador(cuerpo)) "$cuerpo-$dv" else null
    }

    // Módulo 11: se multiplica de derecha a izquierda por 2,3,4,5,6,7,2,3...
    private fun calcularDigitoVerificador(cuerpo: String): Char {
        var suma = 0
        var factor = 2
        for (digito in cuerpo.reversed()) {
            suma += digito.digitToInt() * factor
            factor = if (factor == 7) 2 else factor + 1
        }
        return when (val resto = 11 - suma % 11) {
            11 -> '0'
            10 -> 'K'
            else -> resto.digitToChar()
        }
    }
}