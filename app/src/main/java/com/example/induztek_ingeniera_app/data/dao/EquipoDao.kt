package com.example.induztek_ingeniera_app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.induztek_ingeniera_app.data.entities.EquipoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipoDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEquipos(equipos: List<EquipoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEquipo(equipo: EquipoEntity): Long


    @Query("SELECT * FROM equipos ORDER BY id_equipo ASC")
    fun obtenerTodos(): Flow<List<EquipoEntity>>


    @Query("SELECT * FROM equipos WHERE id_cliente = :idCliente ORDER BY id_equipo ASC")
    fun obtenerEquiposPorCliente(idCliente: Long): Flow<List<EquipoEntity>>


    @Query("SELECT * FROM equipos WHERE id_equipo = :idEquipo LIMIT 1")
    fun buscarEquipoPorId(idEquipo: Long): Flow<EquipoEntity?>


    @Query("UPDATE equipos SET direccion = :direccion, latitud = :lat, longitud = :lng WHERE id_equipo = :idEquipo")
    suspend fun actualizarUbicacion(idEquipo: Long, direccion: String, lat: Double, lng: Double)
}