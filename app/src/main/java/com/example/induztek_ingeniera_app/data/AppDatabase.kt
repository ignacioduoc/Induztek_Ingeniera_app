package com.example.induztek_ingeniera_app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.induztek_ingeniera_app.data.dao.ProtocoloDao
import com.example.induztek_ingeniera_app.data.dao.UsuarioDao
import com.example.induztek_ingeniera_app.data.entities.*
import java.util.concurrent.Executors

@Database(
    entities = [RolEntity::class, UsuarioEntity::class, EquipoEntity::class, ProtocoloEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun protocoloDao(): ProtocoloDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "induztek_database"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Ejecuta la precarga en un hilo secundario al instalarse la app
                            Executors.newSingleThreadExecutor().execute {
                                db.execSQL("INSERT INTO roles (id_rol, nombre_rol) VALUES (1, 'Técnico'), (2, 'Cliente')")
                                db.execSQL("INSERT INTO usuarios (id_usuario, nombre, correo, contrasena, empresa, id_rol) VALUES (1, 'Matías Rojas', 'tecnico@gmail.cl', '123456', 'Induztek Ingeniería SpA', 1), (2, 'Pedro Salas', 'cliente@gmail.cl', '123456', 'Industrias Aurora S.A.', 2), (3, 'Daniela Vera', 'cliente2@gmail.cl', '123456', 'Envases del Sur S.A.', 2)")
                                db.execSQL("INSERT INTO equipos (id_equipo, codigo, tipo, instalacion, id_cliente) VALUES (1, 'TR-0142', 'Transformador 500 kVA', 'Planta Lampa · S/E-02', 2), (2, 'TR-0098', 'Transformador 300 kVA', 'Planta Lampa · S/E-01', 2), (3, 'TR-0215', 'Transformador 1000 kVA', 'Planta Quilicura · S/E-03', 2), (4, 'TR-0330', 'Transformador 750 kVA', 'Planta Puerto Montt · S/E-01', 3)")
                                db.execSQL("INSERT INTO protocolos (id_equipo, id_tecnico, fecha_hora, tipo_prueba, at_bt, at_tierra, bt_tierra, indice_polarizacion, resultado, observaciones, estado) VALUES (1, 1, 1744727400000, 'Aislamiento 5 kV DC', 13.6, 9.8, 8.3, 2.5, 'APROBADO', '', 'FORMALIZADO'), (1, 1, 1760019300000, 'Aislamiento 5 kV DC', 13.4, 9.6, 8.1, 2.4, 'APROBADO', '', 'FORMALIZADO'), (1, 1, 1776174300000, 'Aislamiento 5 kV DC', 13.1, 9.4, 7.8, 2.4, 'APROBADO', '', 'FORMALIZADO'), (2, 1, 1776367200000, 'Aislamiento 5 kV DC', 11.2, 8.7, 7.9, 2.6, 'APROBADO', '', 'FORMALIZADO')")
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
