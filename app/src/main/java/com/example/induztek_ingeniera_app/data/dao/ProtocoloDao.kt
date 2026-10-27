package com.example.induztek_ingeniera_app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.induztek_ingeniera_app.data.entities.ProtocoloConDetalles
import com.example.induztek_ingeniera_app.data.entities.ProtocoloEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProtocoloDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProtocolo(protocolo: ProtocoloEntity): Long

    @Query("""
        SELECT p.*, e.codigo, e.tipo, e.instalacion, t.nombre AS nombre_tecnico 
        FROM protocolos p 
        INNER JOIN equipos e ON p.id_equipo = e.id_equipo 
        INNER JOIN usuarios t ON p.id_tecnico = t.id_usuario
        WHERE e.id_cliente = :clienteId 
        ORDER BY p.fecha_hora DESC
    """)
    fun listarProtocolosDeCliente(clienteId: Long): Flow<List<ProtocoloConDetalles>>

    @Query("""
        SELECT p.*, e.codigo, e.tipo, e.instalacion, t.nombre AS nombre_tecnico 
        FROM protocolos p 
        INNER JOIN equipos e ON p.id_equipo = e.id_equipo 
        INNER JOIN usuarios t ON p.id_tecnico = t.id_usuario
        WHERE p.id_equipo = :equipoId 
        ORDER BY p.fecha_hora DESC
    """)
    fun listarProtocolosDeEquipo(equipoId: Long): Flow<List<ProtocoloConDetalles>>

    @Query("""
        SELECT p.*, e.codigo, e.tipo, e.instalacion, t.nombre AS nombre_tecnico 
        FROM protocolos p 
        INNER JOIN equipos e ON p.id_equipo = e.id_equipo 
        INNER JOIN usuarios t ON p.id_tecnico = t.id_usuario
        WHERE p.id_protocolo = :id LIMIT 1
    """)
    fun buscarProtocoloPorId(id: Long): Flow<ProtocoloConDetalles?>


    @Query("UPDATE protocolos SET estado = 'FORMALIZADO' WHERE id_protocolo = :id")
    suspend fun formalizarProtocolo(id: Long): Int
}