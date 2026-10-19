package com.example.induztek_ingeniera_app.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.induztek_ingeniera_app.model.Equipo
import com.example.induztek_ingeniera_app.model.EstadoProtocolo
import com.example.induztek_ingeniera_app.model.Protocolo
import com.example.induztek_ingeniera_app.model.Resultado
import com.example.induztek_ingeniera_app.model.Usuario

// =====================================================================
//  ProtocoloDbHelper.kt
//  ---------------------------------------------------------------------
//  UNA base de datos (induztek.db) con CUATRO tablas relacionadas (el MER):
//
//   - Un rol tiene muchos usuarios           usuarios.id_rol       -> roles
//   - Un cliente tiene muchos equipos        equipos.id_cliente    -> usuarios
//   - Un equipo tiene muchos protocolos      protocolos.id_equipo  -> equipos
//   - Un técnico registra muchos protocolos  protocolos.id_tecnico -> usuarios
//
//  Regla: UN helper por base de datos. Todas las tablas se crean en el MISMO onCreate().
//  Las columnas tienen nombres distintos en cada tabla (nombre_rol / nombre,
//  id_cliente / id_tecnico) para que los JOIN no tengan columnas repetidas.
// =====================================================================

class ProtocoloDbHelper(context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    companion object {
        // Estos dos valores viajan al constructor del padre (línea de arriba)
        private const val NOMBRE_BD = "induztek.db"
        private const val VERSION_BD = 1

        // Tabla roles
        private const val TABLA_ROLES = "roles"
        private const val ROL_ID = "id_rol"
        private const val ROL_NOMBRE = "nombre_rol"

        // Tabla usuarios
        private const val TABLA_USUARIOS = "usuarios"
        private const val USU_ID = "id_usuario"
        private const val USU_NOMBRE = "nombre"
        private const val USU_CORREO = "correo"
        private const val USU_CONTRASENA = "contrasena"
        private const val USU_EMPRESA = "empresa"
        private const val USU_ROL = "id_rol"

        // Tabla equipos
        private const val TABLA_EQUIPOS = "equipos"
        private const val EQU_ID = "id_equipo"
        private const val EQU_CODIGO = "codigo"
        private const val EQU_TIPO = "tipo"
        private const val EQU_INSTALACION = "instalacion"
        private const val EQU_CLIENTE = "id_cliente"

        // Tabla protocolos
        private const val TABLA_PROTOCOLOS = "protocolos"
        private const val PRO_ID = "id_protocolo"
        private const val PRO_EQUIPO = "id_equipo"
        private const val PRO_TECNICO = "id_tecnico"
        private const val PRO_FECHA = "fecha_hora"
        private const val PRO_TIPO_PRUEBA = "tipo_prueba"
        private const val PRO_AT_BT = "at_bt"                        // MEDICIÓN
        private const val PRO_AT_TIERRA = "at_tierra"                // MEDICIÓN
        private const val PRO_BT_TIERRA = "bt_tierra"                // MEDICIÓN
        private const val PRO_INDICE_POL = "indice_polarizacion"     // MEDICIÓN
        private const val PRO_RESULTADO = "resultado"
        private const val PRO_OBSERVACIONES = "observaciones"
        private const val PRO_ESTADO = "estado"
        private const val PRO_FOTO = "foto"

        // Alias del JOIN: el nombre del técnico (usuarios.nombre) llega con este nombre
        private const val ALIAS_NOMBRE_TECNICO = "nombre_tecnico"

        // ------------------------- CREATE TABLE ------------------------

        // Las constantes se "pegan" dentro del texto SQL con $.
        // SQL_CREAR_ROLES termina siendo:
        // CREATE TABLE roles (id_rol INTEGER PRIMARY KEY AUTOINCREMENT, nombre_rol TEXT NOT NULL UNIQUE)
        private const val SQL_CREAR_ROLES =
            "CREATE TABLE $TABLA_ROLES (" +
                "$ROL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$ROL_NOMBRE TEXT NOT NULL UNIQUE)"

        // REFERENCES crea la CLAVE FORÁNEA hacia roles(id_rol).
        // UNIQUE en correo: no puede haber dos usuarios con el mismo correo.
        private const val SQL_CREAR_USUARIOS =
            "CREATE TABLE $TABLA_USUARIOS (" +
                "$USU_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$USU_NOMBRE TEXT NOT NULL, " +
                "$USU_CORREO TEXT NOT NULL UNIQUE, " +
                "$USU_CONTRASENA TEXT NOT NULL, " +
                "$USU_EMPRESA TEXT NOT NULL, " +
                "$USU_ROL INTEGER NOT NULL REFERENCES $TABLA_ROLES($ROL_ID))"

        // id_cliente apunta a un usuario (el cliente dueño del equipo)
        private const val SQL_CREAR_EQUIPOS =
            "CREATE TABLE $TABLA_EQUIPOS (" +
                "$EQU_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$EQU_CODIGO TEXT NOT NULL UNIQUE, " +
                "$EQU_TIPO TEXT NOT NULL, " +
                "$EQU_INSTALACION TEXT NOT NULL, " +
                "$EQU_CLIENTE INTEGER NOT NULL REFERENCES $TABLA_USUARIOS($USU_ID))"

        // Dos claves foráneas: el equipo probado y el técnico que registró la prueba
        private const val SQL_CREAR_PROTOCOLOS =
            "CREATE TABLE $TABLA_PROTOCOLOS (" +
                "$PRO_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$PRO_EQUIPO INTEGER NOT NULL REFERENCES $TABLA_EQUIPOS($EQU_ID), " +
                "$PRO_TECNICO INTEGER NOT NULL REFERENCES $TABLA_USUARIOS($USU_ID), " +
                "$PRO_FECHA INTEGER NOT NULL, " +
                "$PRO_TIPO_PRUEBA TEXT NOT NULL, " +
                // MEDICIÓN: una columna REAL (número con decimales) por cada medición
                "$PRO_AT_BT REAL NOT NULL, " +
                "$PRO_AT_TIERRA REAL NOT NULL, " +
                "$PRO_BT_TIERRA REAL NOT NULL, " +
                "$PRO_INDICE_POL REAL NOT NULL, " +
                "$PRO_RESULTADO TEXT NOT NULL, " +
                "$PRO_OBSERVACIONES TEXT NOT NULL, " +
                "$PRO_ESTADO TEXT NOT NULL, " +
                // BLOB = "Binary Large OBject": guarda bytes tal cual (la foto). Puede ir vacía.
                "$PRO_FOTO BLOB)"

        // ----------------------- DATOS PRECARGADOS ---------------------

        // Los roles llevan id explícito para que Rol.ID_TECNICO (1) y
        // Rol.ID_CLIENTE (2) siempre apunten al rol correcto.
        private const val SQL_PRECARGAR_ROLES =
            "INSERT INTO $TABLA_ROLES ($ROL_ID, $ROL_NOMBRE) VALUES " +
                "(1, 'Técnico'), (2, 'Cliente')"

        // Usuarios de prueba (datos ficticios). Todos con contraseña 123456.
        // Llevan id explícito para que los equipos puedan apuntar a su cliente.
        private const val SQL_PRECARGAR_USUARIOS =
            "INSERT INTO $TABLA_USUARIOS " +
                "($USU_ID, $USU_NOMBRE, $USU_CORREO, $USU_CONTRASENA, $USU_EMPRESA, $USU_ROL) VALUES " +
                "(1, 'Matías Rojas', 'tecnico@gmail.cl', '123456', 'Induztek Ingeniería SpA', 1), " +
                "(2, 'Pedro Salas', 'cliente@gmail.cl', '123456', 'Industrias Aurora S.A.', 2), " +
                "(3, 'Daniela Vera', 'cliente2@gmail.cl', '123456', 'Envases del Sur S.A.', 2)"

        // Equipos: 3 de Pedro (id_cliente 2) y 1 de Daniela (id_cliente 3)
        private const val SQL_PRECARGAR_EQUIPOS =
            "INSERT INTO $TABLA_EQUIPOS " +
                "($EQU_ID, $EQU_CODIGO, $EQU_TIPO, $EQU_INSTALACION, $EQU_CLIENTE) VALUES " +
                "(1, 'TR-0142', 'Transformador 500 kVA', 'Planta Lampa · S/E-02', 2), " +
                "(2, 'TR-0098', 'Transformador 300 kVA', 'Planta Lampa · S/E-01', 2), " +
                "(3, 'TR-0215', 'Transformador 1000 kVA', 'Planta Quilicura · S/E-03', 2), " +
                "(4, 'TR-0330', 'Transformador 750 kVA', 'Planta Puerto Montt · S/E-01', 3)"

        // Protocolos anteriores (ya formalizados) para tener historial que comparar.
        // Fechas en milisegundos: 15-04-2025, 09-10-2025, 14-04-2026 y 16-04-2026.
        private const val SQL_PRECARGAR_PROTOCOLOS =
            "INSERT INTO $TABLA_PROTOCOLOS " +
                "($PRO_EQUIPO, $PRO_TECNICO, $PRO_FECHA, $PRO_TIPO_PRUEBA, " +
                "$PRO_AT_BT, $PRO_AT_TIERRA, $PRO_BT_TIERRA, $PRO_INDICE_POL, " +
                "$PRO_RESULTADO, $PRO_OBSERVACIONES, $PRO_ESTADO) VALUES " +
                "(1, 1, 1744727400000, 'Aislamiento 5 kV DC', 13.6, 9.8, 8.3, 2.5, 'APROBADO', '', 'FORMALIZADO'), " +
                "(1, 1, 1760019300000, 'Aislamiento 5 kV DC', 13.4, 9.6, 8.1, 2.4, 'APROBADO', '', 'FORMALIZADO'), " +
                "(1, 1, 1776174300000, 'Aislamiento 5 kV DC', 13.1, 9.4, 7.8, 2.4, 'APROBADO', '', 'FORMALIZADO'), " +
                "(2, 1, 1776367200000, 'Aislamiento 5 kV DC', 11.2, 8.7, 7.9, 2.6, 'APROBADO', '', 'FORMALIZADO')"

        // ------------------------ SELECT CON JOIN ----------------------

        // Une cada usuario con SU rol:
        // "trae las columnas de u (usuarios) + el nombre_rol de r (roles)
        //  donde el id_rol del usuario sea igual al id_rol del rol".
        private const val SQL_SELECT_USUARIOS =
            "SELECT u.*, r.$ROL_NOMBRE AS $ROL_NOMBRE " +
                "FROM $TABLA_USUARIOS u " +
                "INNER JOIN $TABLA_ROLES r ON u.$USU_ROL = r.$ROL_ID"

        // Une cada equipo con su cliente para traer la empresa
        private const val SQL_SELECT_EQUIPOS =
            "SELECT e.*, c.$USU_EMPRESA AS $USU_EMPRESA " +
                "FROM $TABLA_EQUIPOS e " +
                "INNER JOIN $TABLA_USUARIOS c ON e.$EQU_CLIENTE = c.$USU_ID"

        // Une cada protocolo con su equipo (código, tipo, instalación)
        // y con el técnico que lo registró (su nombre)
        private const val SQL_SELECT_PROTOCOLOS =
            "SELECT p.*, e.$EQU_CODIGO AS $EQU_CODIGO, e.$EQU_TIPO AS $EQU_TIPO, " +
                "e.$EQU_INSTALACION AS $EQU_INSTALACION, t.$USU_NOMBRE AS $ALIAS_NOMBRE_TECNICO " +
                "FROM $TABLA_PROTOCOLOS p " +
                "INNER JOIN $TABLA_EQUIPOS e ON p.$PRO_EQUIPO = e.$EQU_ID " +
                "INNER JOIN $TABLA_USUARIOS t ON p.$PRO_TECNICO = t.$USU_ID"
    }

    // Se ejecuta CADA VEZ que se abre la conexión, antes de onCreate.
    // SQLite trae las claves foráneas APAGADAS por defecto: aquí se encienden.
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    // Una sola vez, cuando induztek.db no existe.
    // Orden: primero las tablas "padre" y después las que las referencian.
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREAR_ROLES)
        db.execSQL(SQL_CREAR_USUARIOS)
        db.execSQL(SQL_CREAR_EQUIPOS)
        db.execSQL(SQL_CREAR_PROTOCOLOS)
        db.execSQL(SQL_PRECARGAR_ROLES)
        db.execSQL(SQL_PRECARGAR_USUARIOS)
        db.execSQL(SQL_PRECARGAR_EQUIPOS)
        db.execSQL(SQL_PRECARGAR_PROTOCOLOS)
    }

    // Si sube VERSION_BD. Versión simple: borra y recrea (se pierden los datos).
    // Orden inverso: primero las tablas "hijas" y al final la "padre".
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLA_PROTOCOLOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_EQUIPOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_USUARIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_ROLES")
        onCreate(db)
    }

    // ---------------------------- USUARIOS ---------------------------

    // READ para el inicio de sesión: busca al usuario con ese correo Y esa contraseña.
    // Los datos viajan en los "?" (nunca concatenados) para evitar inyección SQL.
    // Devuelve el usuario con su rol, o null si el correo o la contraseña no coinciden.
    fun iniciarSesion(correo: String, contrasena: String): Usuario? =
        consultarUsuarios(
            "$SQL_SELECT_USUARIOS WHERE u.$USU_CORREO = ? AND u.$USU_CONTRASENA = ?",
            arrayOf(correo, contrasena)
        ).firstOrNull()

    // READ de UNA fila por su clave primaria (o null si no existe).
    // Se usa al abrir la app para recuperar la sesión guardada.
    fun buscarUsuarioPorId(id: Long): Usuario? =
        consultarUsuarios("$SQL_SELECT_USUARIOS WHERE u.$USU_ID = ?", arrayOf(id.toString()))
            .firstOrNull()

    // ----------------------------- EQUIPOS ---------------------------

    // READ -> todos los equipos con la empresa de su cliente, ordenados por código
    fun listarEquipos(): List<Equipo> =
        consultarEquipos("$SQL_SELECT_EQUIPOS ORDER BY e.$EQU_CODIGO", null)

    // READ de UN equipo por su clave primaria (para el encabezado del formulario)
    fun buscarEquipoPorId(id: Long): Equipo? =
        consultarEquipos("$SQL_SELECT_EQUIPOS WHERE e.$EQU_ID = ?", arrayOf(id.toString()))
            .firstOrNull()

    // ---------------------------- PROTOCOLOS -------------------------

    // CREATE -> INSERT. Devuelve el id generado (o -1 si falla).
    fun insertarProtocolo(protocolo: Protocolo): Long =
        writableDatabase.insert(TABLA_PROTOCOLOS, null, valoresDe(protocolo))

    // READ -> los protocolos de los equipos de UN cliente, del más nuevo al más antiguo
    fun listarProtocolosDeCliente(clienteId: Long): List<Protocolo> =
        consultarProtocolos(
            "$SQL_SELECT_PROTOCOLOS WHERE e.$EQU_CLIENTE = ? ORDER BY p.$PRO_FECHA DESC",
            arrayOf(clienteId.toString())
        )

    // READ -> el historial de UN equipo, del más nuevo al más antiguo
    fun listarProtocolosDeEquipo(equipoId: Long): List<Protocolo> =
        consultarProtocolos(
            "$SQL_SELECT_PROTOCOLOS WHERE p.$PRO_EQUIPO = ? ORDER BY p.$PRO_FECHA DESC",
            arrayOf(equipoId.toString())
        )

    // READ de UN protocolo por su clave primaria (para la pantalla de detalle)
    fun buscarProtocoloPorId(id: Long): Protocolo? =
        consultarProtocolos("$SQL_SELECT_PROTOCOLOS WHERE p.$PRO_ID = ?", arrayOf(id.toString()))
            .firstOrNull()

    // UPDATE de UNA sola columna: el estado pasa a FORMALIZADO.
    // Devuelve cuántas filas cambió (1 = OK). ¡Sin el WHERE se formalizarían TODOS!
    fun formalizarProtocolo(id: Long): Int {
        val valores = ContentValues().apply { put(PRO_ESTADO, EstadoProtocolo.FORMALIZADO.name) }
        return writableDatabase.update(TABLA_PROTOCOLOS, valores, "$PRO_ID = ?", arrayOf(id.toString()))
    }

    // ---------------------------- AYUDANTES --------------------------

    // ContentValues = mapa columna -> valor (para el INSERT).
    // Los enum se guardan con su nombre: Resultado.APROBADO -> "APROBADO".
    private fun valoresDe(protocolo: Protocolo) = ContentValues().apply {
        put(PRO_EQUIPO, protocolo.equipoId)
        put(PRO_TECNICO, protocolo.tecnicoId)
        put(PRO_FECHA, protocolo.fechaHora)
        put(PRO_TIPO_PRUEBA, protocolo.tipoPrueba)
        put(PRO_AT_BT, protocolo.atBt)                         // MEDICIÓN
        put(PRO_AT_TIERRA, protocolo.atTierra)                 // MEDICIÓN
        put(PRO_BT_TIERRA, protocolo.btTierra)                 // MEDICIÓN
        put(PRO_INDICE_POL, protocolo.indicePolarizacion)      // MEDICIÓN
        put(PRO_RESULTADO, protocolo.resultado.name)
        put(PRO_OBSERVACIONES, protocolo.observaciones)
        put(PRO_ESTADO, protocolo.estado.name)
        put(PRO_FOTO, protocolo.foto)
    }

    // Ejecuta un SELECT y recorre el Cursor fila por fila.
    // Hay una por tabla: el SQL cambia, la forma de leer es la misma.
    private fun consultarUsuarios(sql: String, args: Array<String>?): List<Usuario> {
        val lista = mutableListOf<Usuario>()
        readableDatabase.rawQuery(sql, args).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(leerUsuario(cursor))
            }
        }
        return lista
    }

    private fun consultarEquipos(sql: String, args: Array<String>?): List<Equipo> {
        val lista = mutableListOf<Equipo>()
        readableDatabase.rawQuery(sql, args).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(leerEquipo(cursor))
            }
        }
        return lista
    }

    private fun consultarProtocolos(sql: String, args: Array<String>?): List<Protocolo> {
        val lista = mutableListOf<Protocolo>()
        readableDatabase.rawQuery(sql, args).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(leerProtocolo(cursor))
            }
        }
        return lista
    }

    // Convierte la fila actual del Cursor en un Usuario.
    // nombre_rol viene de la tabla roles gracias al JOIN. La contraseña NO se lee.
    private fun leerUsuario(cursor: Cursor) = Usuario(
        nombre = cursor.getString(cursor.getColumnIndexOrThrow(USU_NOMBRE)),
        correo = cursor.getString(cursor.getColumnIndexOrThrow(USU_CORREO)),
        empresa = cursor.getString(cursor.getColumnIndexOrThrow(USU_EMPRESA)),
        rolId = cursor.getLong(cursor.getColumnIndexOrThrow(USU_ROL)),
        id = cursor.getLong(cursor.getColumnIndexOrThrow(USU_ID)),
        rolNombre = cursor.getString(cursor.getColumnIndexOrThrow(ROL_NOMBRE))
    )

    // Convierte la fila actual en un Equipo.
    // La empresa viene del cliente gracias al JOIN.
    private fun leerEquipo(cursor: Cursor) = Equipo(
        codigo = cursor.getString(cursor.getColumnIndexOrThrow(EQU_CODIGO)),
        tipo = cursor.getString(cursor.getColumnIndexOrThrow(EQU_TIPO)),
        instalacion = cursor.getString(cursor.getColumnIndexOrThrow(EQU_INSTALACION)),
        clienteId = cursor.getLong(cursor.getColumnIndexOrThrow(EQU_CLIENTE)),
        id = cursor.getLong(cursor.getColumnIndexOrThrow(EQU_ID)),
        empresaCliente = cursor.getString(cursor.getColumnIndexOrThrow(USU_EMPRESA))
    )

    // Convierte la fila actual en un Protocolo.
    // valueOf() hace lo contrario de .name: "APROBADO" -> Resultado.APROBADO.
    private fun leerProtocolo(cursor: Cursor) = Protocolo(
        equipoId = cursor.getLong(cursor.getColumnIndexOrThrow(PRO_EQUIPO)),
        tecnicoId = cursor.getLong(cursor.getColumnIndexOrThrow(PRO_TECNICO)),
        fechaHora = cursor.getLong(cursor.getColumnIndexOrThrow(PRO_FECHA)),
        tipoPrueba = cursor.getString(cursor.getColumnIndexOrThrow(PRO_TIPO_PRUEBA)),
        atBt = cursor.getDouble(cursor.getColumnIndexOrThrow(PRO_AT_BT)),                     // MEDICIÓN
        atTierra = cursor.getDouble(cursor.getColumnIndexOrThrow(PRO_AT_TIERRA)),             // MEDICIÓN
        btTierra = cursor.getDouble(cursor.getColumnIndexOrThrow(PRO_BT_TIERRA)),             // MEDICIÓN
        indicePolarizacion = cursor.getDouble(cursor.getColumnIndexOrThrow(PRO_INDICE_POL)), // MEDICIÓN
        resultado = Resultado.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(PRO_RESULTADO))),
        observaciones = cursor.getString(cursor.getColumnIndexOrThrow(PRO_OBSERVACIONES)),
        estado = EstadoProtocolo.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(PRO_ESTADO))),
        // getBlob() lee los bytes. Si la columna está vacía (NULL) queda null = sin foto.
        foto = cursor.getColumnIndexOrThrow(PRO_FOTO).let { columna ->
            if (cursor.isNull(columna)) null else cursor.getBlob(columna)
        },
        id = cursor.getLong(cursor.getColumnIndexOrThrow(PRO_ID)),
        codigoEquipo = cursor.getString(cursor.getColumnIndexOrThrow(EQU_CODIGO)),
        tipoEquipo = cursor.getString(cursor.getColumnIndexOrThrow(EQU_TIPO)),
        instalacion = cursor.getString(cursor.getColumnIndexOrThrow(EQU_INSTALACION)),
        nombreTecnico = cursor.getString(cursor.getColumnIndexOrThrow(ALIAS_NOMBRE_TECNICO))
    )
}
