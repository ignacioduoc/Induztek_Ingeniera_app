package com.example.induztek_ingeniera_app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.induztek_ingeniera_app.data.entities.EquipoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipoDao {

    // 1. Inserción masiva o individual de equipos
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEquipos(equipos: List<EquipoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEquipo(equipo: EquipoEntity): Long

    // 2. Consulta reactiva de TODOS los equipos (Para la vista del Técnico)
    @Query("SELECT * FROM equipos ORDER BY id_equipo ASC")
    fun obtenerTodos(): Flow<List<EquipoEntity>>

    // 3. Consulta reactiva de equipos por CLIENTE (Para la vista del Cliente)
    @Query("SELECT * FROM equipos WHERE id_cliente = :idCliente ORDER BY id_equipo ASC")
    fun obtenerEquiposPorCliente(idCliente: Long): Flow<List<EquipoEntity>>

    // 4. Búsqueda puntual de un equipo por su ID
    @Query("SELECT * FROM equipos WHERE id_equipo = :idEquipo LIMIT 1")
    fun buscarEquipoPorId(idEquipo: Long): Flow<EquipoEntity?>

    // 5. Actualiza la dirección y las coordenadas GPS obtenidas
    @Query("UPDATE equipos SET direccion = :direccion, latitud = :lat, longitud = :lng WHERE id_equipo = :idEquipo")
    suspend fun actualizarUbicacion(idEquipo: Long, direccion: String, lat: Double, lng: Double)
}