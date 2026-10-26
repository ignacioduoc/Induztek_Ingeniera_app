package com.example.induztek_ingeniera_app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.induztek_ingeniera_app.data.dao.EquipoDao
import com.example.induztek_ingeniera_app.data.dao.ProtocoloDao
import com.example.induztek_ingeniera_app.data.dao.UsuarioDao
import com.example.induztek_ingeniera_app.data.entities.EquipoEntity
import com.example.induztek_ingeniera_app.data.entities.ProtocoloEntity
import com.example.induztek_ingeniera_app.data.entities.RolEntity
import com.example.induztek_ingeniera_app.data.entities.UsuarioEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [RolEntity::class, UsuarioEntity::class, EquipoEntity::class, ProtocoloEntity::class],
    version = 17, // Subimos a la versión 17 para incorporar foto_perfil en UsuarioEntity
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
                    "induztek_database_v17"
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

                        private fun poblarBaseDatos(db: SupportSQLiteDatabase) {
                            db.beginTransaction()
                            try {
                                // 1. Roles
                                db.execSQL("INSERT OR REPLACE INTO roles (id_rol, nombre_rol) VALUES (1, 'Técnico'), (2, 'Cliente');")

                                // 2. Usuarios (Incluye 'foto_perfil' como NULL)
                                db.execSQL("INSERT OR REPLACE INTO usuarios (id_usuario, nombre, correo, contrasena, empresa, id_rol, foto_perfil) VALUES (1, 'Matías Rojas', 'tecnico@gmail.cl', '123456', 'Induztek Ingeniería SpA', 1, NULL);")
                                db.execSQL("INSERT OR REPLACE INTO usuarios (id_usuario, nombre, correo, contrasena, empresa, id_rol, foto_perfil) VALUES (2, 'Pedro Salas', 'cliente@gmail.cl', '123456', 'Industrias Aurora S.A.', 2, NULL);")
                                db.execSQL("INSERT OR REPLACE INTO usuarios (id_usuario, nombre, correo, contrasena, empresa, id_rol, foto_perfil) VALUES (3, 'Daniela Vera', 'cliente2@gmail.cl', '123456', 'Envases del Sur S.A.', 2, NULL);")

                                // 3. Equipos
                                db.execSQL("INSERT OR REPLACE INTO equipos (id_equipo, codigo, tipo, instalacion, direccion, latitud, longitud, id_cliente) VALUES (1, 'TR-0142', 'Transformador 500 kVA', 'Planta Lampa · S/E-02', 'Camino Los Maitenes 1200, Lampa', -33.2845, -70.8752, 2);")
                                db.execSQL("INSERT OR REPLACE INTO equipos (id_equipo, codigo, tipo, instalacion, direccion, latitud, longitud, id_cliente) VALUES (2, 'TR-0098', 'Transformador 300 kVA', 'Planta Lampa · S/E-01', 'Camino Los Maitenes 1200, Lampa', -33.2845, -70.8752, 2);")
                                db.execSQL("INSERT OR REPLACE INTO equipos (id_equipo, codigo, tipo, instalacion, direccion, latitud, longitud, id_cliente) VALUES (3, 'TR-0215', 'Transformador 1000 kVA', 'Planta Quilicura · S/E-03', 'Av. Américo Vespucio 1500, Quilicura', -33.3611, -70.7305, 2);")
                                db.execSQL("INSERT OR REPLACE INTO equipos (id_equipo, codigo, tipo, instalacion, direccion, latitud, longitud, id_cliente) VALUES (4, 'TR-0330', 'Transformador 750 kVA', 'Planta Puerto Montt · S/E-01', 'Ruta 5 Sur Km 1020, Puerto Montt', -41.4693, -72.9424, 3);")

                                // 4. Protocolos (Incluye 'foto' como NULL y 'sincronizado' como 1)
                                db.execSQL("INSERT OR REPLACE INTO protocolos (id_equipo, id_tecnico, fecha_hora, tipo_prueba, at_bt, at_tierra, bt_tierra, indice_polarizacion, resultado, observaciones, estado, foto, sincronizado) VALUES (1, 1, 1744727400000, 'Aislamiento 5 kV DC', 13.6, 9.8, 8.3, 2.5, 'APROBADO', '', 'FORMALIZADO', NULL, 1);")
                                db.execSQL("INSERT OR REPLACE INTO protocolos (id_equipo, id_tecnico, fecha_hora, tipo_prueba, at_bt, at_tierra, bt_tierra, indice_polarizacion, resultado, observaciones, estado, foto, sincronizado) VALUES (1, 1, 1760019300000, 'Aislamiento 5 kV DC', 13.4, 9.6, 8.1, 2.4, 'APROBADO', '', 'FORMALIZADO', NULL, 1);")

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