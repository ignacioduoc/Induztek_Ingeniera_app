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

class ProtocoloDbHelper(context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    companion object {

        private const val NOMBRE_BD = "induztek.db"
        private const val VERSION_BD = 1

        private const val TABLA_ROLES = "roles"
        private const val ROL_ID = "id_rol"
        private const val ROL_NOMBRE = "nombre_rol"


        private const val TABLA_USUARIOS = "usuarios"
        private const val USU_ID = "id_usuario"
        private const val USU_NOMBRE = "nombre"
        private const val USU_CORREO = "correo"
        private const val USU_CONTRASENA = "contrasena"
        private const val USU_EMPRESA = "empresa"
        private const val USU_ROL = "id_rol"


        private const val TABLA_EQUIPOS = "equipos"
        private const val EQU_ID = "id_equipo"
        private const val EQU_CODIGO = "codigo"
        private const val EQU_TIPO = "tipo"
        private const val EQU_INSTALACION = "instalacion"
        private const val EQU_CLIENTE = "id_cliente"


        private const val TABLA_PROTOCOLOS = "protocolos"
        private const val PRO_ID = "id_protocolo"
        private const val PRO_EQUIPO = "id_equipo"
        private const val PRO_TECNICO = "id_tecnico"
        private const val PRO_FECHA = "fecha_hora"
        private const val PRO_TIPO_PRUEBA = "tipo_prueba"
        private const val PRO_AT_BT = "at_bt"
        private const val PRO_AT_TIERRA = "at_tierra"
        private const val PRO_BT_TIERRA = "bt_tierra"
        private const val PRO_INDICE_POL = "indice_polarizacion"
        private const val PRO_RESULTADO = "resultado"
        private const val PRO_OBSERVACIONES = "observaciones"
        private const val PRO_ESTADO = "estado"
        private const val PRO_FOTO = "foto"


        private const val ALIAS_NOMBRE_TECNICO = "nombre_tecnico"


        private const val SQL_CREAR_ROLES =
            "CREATE TABLE $TABLA_ROLES (" +
                "$ROL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$ROL_NOMBRE TEXT NOT NULL UNIQUE)"


        private const val SQL_CREAR_USUARIOS =
            "CREATE TABLE $TABLA_USUARIOS (" +
                "$USU_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$USU_NOMBRE TEXT NOT NULL, " +
                "$USU_CORREO TEXT NOT NULL UNIQUE, " +
                "$USU_CONTRASENA TEXT NOT NULL, " +
                "$USU_EMPRESA TEXT NOT NULL, " +
                "$USU_ROL INTEGER NOT NULL REFERENCES $TABLA_ROLES($ROL_ID))"


        private const val SQL_CREAR_EQUIPOS =
            "CREATE TABLE $TABLA_EQUIPOS (" +
                "$EQU_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$EQU_CODIGO TEXT NOT NULL UNIQUE, " +
                "$EQU_TIPO TEXT NOT NULL, " +
                "$EQU_INSTALACION TEXT NOT NULL, " +
                "$EQU_CLIENTE INTEGER NOT NULL REFERENCES $TABLA_USUARIOS($USU_ID))"


        private const val SQL_CREAR_PROTOCOLOS =
            "CREATE TABLE $TABLA_PROTOCOLOS (" +
                "$PRO_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$PRO_EQUIPO INTEGER NOT NULL REFERENCES $TABLA_EQUIPOS($EQU_ID), " +
                "$PRO_TECNICO INTEGER NOT NULL REFERENCES $TABLA_USUARIOS($USU_ID), " +
                "$PRO_FECHA INTEGER NOT NULL, " +
                "$PRO_TIPO_PRUEBA TEXT NOT NULL, " +

                "$PRO_AT_BT REAL NOT NULL, " +
                "$PRO_AT_TIERRA REAL NOT NULL, " +
                "$PRO_BT_TIERRA REAL NOT NULL, " +
                "$PRO_INDICE_POL REAL NOT NULL, " +
                "$PRO_RESULTADO TEXT NOT NULL, " +
                "$PRO_OBSERVACIONES TEXT NOT NULL, " +
                "$PRO_ESTADO TEXT NOT NULL, " +

                "$PRO_FOTO BLOB)"


        private const val SQL_PRECARGAR_ROLES =
            "INSERT INTO $TABLA_ROLES ($ROL_ID, $ROL_NOMBRE) VALUES " +
                "(1, 'Técnico'), (2, 'Cliente')"


        private const val SQL_PRECARGAR_USUARIOS =
            "INSERT INTO $TABLA_USUARIOS " +
                "($USU_ID, $USU_NOMBRE, $USU_CORREO, $USU_CONTRASENA, $USU_EMPRESA, $USU_ROL) VALUES " +
                "(1, 'Matías Rojas', 'tecnico@gmail.cl', '123456', 'Induztek Ingeniería SpA', 1), " +
                "(2, 'Pedro Salas', 'cliente@gmail.cl', '123456', 'Industrias Aurora S.A.', 2), " +
                "(3, 'Daniela Vera', 'cliente2@gmail.cl', '123456', 'Envases del Sur S.A.', 2)"


        private const val SQL_PRECARGAR_EQUIPOS =
            "INSERT INTO $TABLA_EQUIPOS " +
                "($EQU_ID, $EQU_CODIGO, $EQU_TIPO, $EQU_INSTALACION, $EQU_CLIENTE) VALUES " +
                "(1, 'TR-0142', 'Transformador 500 kVA', 'Planta Lampa · S/E-02', 2), " +
                "(2, 'TR-0098', 'Transformador 300 kVA', 'Planta Lampa · S/E-01', 2), " +
                "(3, 'TR-0215', 'Transformador 1000 kVA', 'Planta Quilicura · S/E-03', 2), " +
                "(4, 'TR-0330', 'Transformador 750 kVA', 'Planta Puerto Montt · S/E-01', 3)"


        private const val SQL_PRECARGAR_PROTOCOLOS =
            "INSERT INTO $TABLA_PROTOCOLOS " +
                "($PRO_EQUIPO, $PRO_TECNICO, $PRO_FECHA, $PRO_TIPO_PRUEBA, " +
                "$PRO_AT_BT, $PRO_AT_TIERRA, $PRO_BT_TIERRA, $PRO_INDICE_POL, " +
                "$PRO_RESULTADO, $PRO_OBSERVACIONES, $PRO_ESTADO) VALUES " +
                "(1, 1, 1744727400000, 'Aislamiento 5 kV DC', 13.6, 9.8, 8.3, 2.5, 'APROBADO', '', 'FORMALIZADO'), " +
                "(1, 1, 1760019300000, 'Aislamiento 5 kV DC', 13.4, 9.6, 8.1, 2.4, 'APROBADO', '', 'FORMALIZADO'), " +
                "(1, 1, 1776174300000, 'Aislamiento 5 kV DC', 13.1, 9.4, 7.8, 2.4, 'APROBADO', '', 'FORMALIZADO'), " +
                "(2, 1, 1776367200000, 'Aislamiento 5 kV DC', 11.2, 8.7, 7.9, 2.6, 'APROBADO', '', 'FORMALIZADO')"


        private const val SQL_SELECT_USUARIOS =
            "SELECT u.*, r.$ROL_NOMBRE AS $ROL_NOMBRE " +
                "FROM $TABLA_USUARIOS u " +
                "INNER JOIN $TABLA_ROLES r ON u.$USU_ROL = r.$ROL_ID"


        private const val SQL_SELECT_EQUIPOS =
            "SELECT e.*, c.$USU_EMPRESA AS $USU_EMPRESA " +
                "FROM $TABLA_EQUIPOS e " +
                "INNER JOIN $TABLA_USUARIOS c ON e.$EQU_CLIENTE = c.$USU_ID"


        private const val SQL_SELECT_PROTOCOLOS =
            "SELECT p.*, e.$EQU_CODIGO AS $EQU_CODIGO, e.$EQU_TIPO AS $EQU_TIPO, " +
                "e.$EQU_INSTALACION AS $EQU_INSTALACION, t.$USU_NOMBRE AS $ALIAS_NOMBRE_TECNICO " +
                "FROM $TABLA_PROTOCOLOS p " +
                "INNER JOIN $TABLA_EQUIPOS e ON p.$PRO_EQUIPO = e.$EQU_ID " +
                "INNER JOIN $TABLA_USUARIOS t ON p.$PRO_TECNICO = t.$USU_ID"
    }


    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }


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


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLA_PROTOCOLOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_EQUIPOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_USUARIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_ROLES")
        onCreate(db)
    }


    fun iniciarSesion(correo: String, contrasena: String): Usuario? =
        consultarUsuarios(
            "$SQL_SELECT_USUARIOS WHERE u.$USU_CORREO = ? AND u.$USU_CONTRASENA = ?",
            arrayOf(correo, contrasena)
        ).firstOrNull()


    fun buscarUsuarioPorId(id: Long): Usuario? =
        consultarUsuarios("$SQL_SELECT_USUARIOS WHERE u.$USU_ID = ?", arrayOf(id.toString()))
            .firstOrNull()


    fun listarEquipos(): List<Equipo> =
        consultarEquipos("$SQL_SELECT_EQUIPOS ORDER BY e.$EQU_CODIGO", null)


    fun buscarEquipoPorId(id: Long): Equipo? =
        consultarEquipos("$SQL_SELECT_EQUIPOS WHERE e.$EQU_ID = ?", arrayOf(id.toString()))
            .firstOrNull()


    fun insertarProtocolo(protocolo: Protocolo): Long =
        writableDatabase.insert(TABLA_PROTOCOLOS, null, valoresDe(protocolo))


    fun listarProtocolosDeCliente(clienteId: Long): List<Protocolo> =
        consultarProtocolos(
            "$SQL_SELECT_PROTOCOLOS WHERE e.$EQU_CLIENTE = ? ORDER BY p.$PRO_FECHA DESC",
            arrayOf(clienteId.toString())
        )


    fun listarProtocolosDeEquipo(equipoId: Long): List<Protocolo> =
        consultarProtocolos(
            "$SQL_SELECT_PROTOCOLOS WHERE p.$PRO_EQUIPO = ? ORDER BY p.$PRO_FECHA DESC",
            arrayOf(equipoId.toString())
        )


    fun buscarProtocoloPorId(id: Long): Protocolo? =
        consultarProtocolos("$SQL_SELECT_PROTOCOLOS WHERE p.$PRO_ID = ?", arrayOf(id.toString()))
            .firstOrNull()


    fun formalizarProtocolo(id: Long): Int {
        val valores = ContentValues().apply { put(PRO_ESTADO, EstadoProtocolo.FORMALIZADO.name) }
        return writableDatabase.update(TABLA_PROTOCOLOS, valores, "$PRO_ID = ?", arrayOf(id.toString()))
    }


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


    private fun leerUsuario(cursor: Cursor) = Usuario(
        nombre = cursor.getString(cursor.getColumnIndexOrThrow(USU_NOMBRE)),
        correo = cursor.getString(cursor.getColumnIndexOrThrow(USU_CORREO)),
        empresa = cursor.getString(cursor.getColumnIndexOrThrow(USU_EMPRESA)),
        rolId = cursor.getLong(cursor.getColumnIndexOrThrow(USU_ROL)),
        id = cursor.getLong(cursor.getColumnIndexOrThrow(USU_ID)),
        rolNombre = cursor.getString(cursor.getColumnIndexOrThrow(ROL_NOMBRE))
    )


    private fun leerEquipo(cursor: Cursor) = Equipo(
        codigo = cursor.getString(cursor.getColumnIndexOrThrow(EQU_CODIGO)),
        tipo = cursor.getString(cursor.getColumnIndexOrThrow(EQU_TIPO)),
        instalacion = cursor.getString(cursor.getColumnIndexOrThrow(EQU_INSTALACION)),
        clienteId = cursor.getLong(cursor.getColumnIndexOrThrow(EQU_CLIENTE)),
        id = cursor.getLong(cursor.getColumnIndexOrThrow(EQU_ID)),
        empresaCliente = cursor.getString(cursor.getColumnIndexOrThrow(USU_EMPRESA))
    )


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
