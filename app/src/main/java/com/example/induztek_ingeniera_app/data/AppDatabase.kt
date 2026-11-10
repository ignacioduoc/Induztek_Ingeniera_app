package com.example.induztek_ingeniera_app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.induztek_ingeniera_app.data.dao.EquipoDao
import com.example.induztek_ingeniera_app.data.dao.ProtocoloDao
import com.example.induztek_ingeniera_app.data.dao.UsuarioDao
import com.example.induztek_ingeniera_app.data.entities.EmpresaEntity
import com.example.induztek_ingeniera_app.data.entities.EquipoEntity
import com.example.induztek_ingeniera_app.data.entities.ProtocoloEntity
import com.example.induztek_ingeniera_app.data.entities.RolEntity
import com.example.induztek_ingeniera_app.data.entities.UsuarioEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        RolEntity::class,
        EmpresaEntity::class,
        UsuarioEntity::class,
        EquipoEntity::class,
        ProtocoloEntity::class
    ],
    version = 19,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun protocoloDao(): ProtocoloDao
    abstract fun equipoDao(): EquipoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "induztek_database_v18"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                poblarBaseDatos(db)
                            }
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                var cantidad = 0
                                val cursor = db.query("SELECT COUNT(*) FROM usuarios")
                                if (cursor.moveToFirst()) {
                                    cantidad = cursor.getInt(0)
                                }
                                cursor.close()

                                if (cantidad == 0) {
                                    poblarBaseDatos(db)
                                }
                            }
                        }

                        // OR IGNORE + ids fijos: si por alguna razón se ejecuta dos veces
                        // (onCreate y onOpen), la segunda no duplica ni borra nada.
                        // El orden importa por las claves foráneas: padres primero.
                        private fun poblarBaseDatos(db: SupportSQLiteDatabase) {
                            db.beginTransaction()
                            try {
                                // 1. Roles
                                db.execSQL("INSERT OR IGNORE INTO roles (id_rol, nombre_rol) VALUES (1, 'Técnico'), (2, 'Cliente'), (3, 'Administrador');")

                                // 2. Empresas (la de Induztek tiene una dirección de ejemplo)
                                db.execSQL("INSERT OR IGNORE INTO empresas (id_empresa, nombre, direccion, logo) VALUES (1, 'Induztek Ingeniería SpA', 'Av. Providencia 1234, Providencia', NULL);")
                                db.execSQL("INSERT OR IGNORE INTO empresas (id_empresa, nombre, direccion, logo) VALUES (2, 'Industrias Aurora S.A.', 'Camino Los Maitenes 1200, Lampa', NULL);")
                                db.execSQL("INSERT OR IGNORE INTO empresas (id_empresa, nombre, direccion, logo) VALUES (3, 'Envases del Sur S.A.', 'Ruta 5 Sur Km 1020, Puerto Montt', NULL);")

                                // 3. Usuarios (la respuesta de seguridad va en minúsculas y sin espacios sobrantes)
                                db.execSQL("INSERT OR IGNORE INTO usuarios (id_usuario, nombre, apellido, correo, contrasena, id_empresa, id_rol, rut, telefono, pregunta_seguridad, respuesta_seguridad, foto_perfil) VALUES (1, 'Matías', 'Rojas', 'tecnico@gmail.com', '123456', 1, 1, '16482913-8', '+56911112222', '¿Cuál es el nombre de tu primera mascota?', 'firulais', NULL);")
                                db.execSQL("INSERT OR IGNORE INTO usuarios (id_usuario, nombre, apellido, correo, contrasena, id_empresa, id_rol, rut, telefono, pregunta_seguridad, respuesta_seguridad, foto_perfil) VALUES (2, 'Pedro', 'Salas', 'cliente@gmail.com', '123456', 2, 2, '14756201-2', '+56933334444', '¿En qué ciudad naciste?', 'santiago', NULL);")
                                db.execSQL("INSERT OR IGNORE INTO usuarios (id_usuario, nombre, apellido, correo, contrasena, id_empresa, id_rol, rut, telefono, pregunta_seguridad, respuesta_seguridad, foto_perfil) VALUES (3, 'Daniela', 'Vera', 'cliente2@gmail.com', '123456', 3, 2, '17093845-3', '+56955556666', '¿En qué ciudad naciste?', 'puerto montt', NULL);")
                                db.execSQL("INSERT OR IGNORE INTO usuarios (id_usuario, nombre, apellido, correo, contrasena, id_empresa, id_rol, rut, telefono, pregunta_seguridad, respuesta_seguridad, foto_perfil) VALUES (4, 'Admin', 'Induztek', 'admin@gmail.com', '123456', 1, 3, '13570246-3', '+56977778888', '¿Cuál es el nombre de tu primer colegio?', 'colegio central', NULL);")

                                // 4. Equipos (cada uno con su empresa y su cliente dueño)
                                db.execSQL("INSERT OR IGNORE INTO equipos (id_equipo, codigo, tipo, instalacion, direccion, latitud, longitud, id_cliente, id_empresa) VALUES (1, 'TR-0142', 'Transformador 500 kVA', 'Planta Lampa · S/E-02', 'Camino Los Maitenes 1200, Lampa', -33.2845, -70.8752, 2, 2);")
                                db.execSQL("INSERT OR IGNORE INTO equipos (id_equipo, codigo, tipo, instalacion, direccion, latitud, longitud, id_cliente, id_empresa) VALUES (2, 'TR-0098', 'Transformador 300 kVA', 'Planta Lampa · S/E-01', 'Camino Los Maitenes 1200, Lampa', -33.2845, -70.8752, 2, 2);")
                                db.execSQL("INSERT OR IGNORE INTO equipos (id_equipo, codigo, tipo, instalacion, direccion, latitud, longitud, id_cliente, id_empresa) VALUES (3, 'TR-0215', 'Transformador 1000 kVA', 'Planta Quilicura · S/E-03', 'Av. Américo Vespucio 1500, Quilicura', -33.3611, -70.7305, 2, 2);")
                                db.execSQL("INSERT OR IGNORE INTO equipos (id_equipo, codigo, tipo, instalacion, direccion, latitud, longitud, id_cliente, id_empresa) VALUES (4, 'TR-0330', 'Transformador 750 kVA', 'Planta Puerto Montt · S/E-01', 'Ruta 5 Sur Km 1020, Puerto Montt', -41.4693, -72.9424, 3, 3);")

                                // 5. Protocolos (con id fijo para que no se dupliquen; 'foto' NULL y 'sincronizado' 1)
                                db.execSQL("INSERT OR IGNORE INTO protocolos (id_protocolo, id_equipo, id_tecnico, fecha_hora, tipo_prueba, at_bt, at_tierra, bt_tierra, indice_polarizacion, resultado, observaciones, estado, foto, sincronizado) VALUES (1, 1, 1, 1744727400000, 'Aislamiento 5 kV DC', 13.6, 9.8, 8.3, 2.5, 'APROBADO', '', 'FORMALIZADO', NULL, 1);")
                                db.execSQL("INSERT OR IGNORE INTO protocolos (id_protocolo, id_equipo, id_tecnico, fecha_hora, tipo_prueba, at_bt, at_tierra, bt_tierra, indice_polarizacion, resultado, observaciones, estado, foto, sincronizado) VALUES (2, 1, 1, 1760019300000, 'Aislamiento 5 kV DC', 13.4, 9.6, 8.1, 2.4, 'APROBADO', '', 'FORMALIZADO', NULL, 1);")

                                db.setTransactionSuccessful()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                db.endTransaction()
                            }
                        }
                    })
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}