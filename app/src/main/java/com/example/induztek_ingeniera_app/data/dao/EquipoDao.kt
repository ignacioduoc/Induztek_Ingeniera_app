package com.example.induztek_ingeniera_app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.induztek_ingeniera_app.data.entities.EmpresaEntity
import com.example.induztek_ingeniera_app.data.entities.EquipoConDetalles
import com.example.induztek_ingeniera_app.data.entities.EquipoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipoDao {

    // IGNORE: si el código ya existe no pisa el equipo de otro (ni rompe los protocolos), devuelve -1
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarEquipos(equipos: List<EquipoEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarEquipo(equipo: EquipoEntity): Long

    @Update
    suspend fun actualizarEquipo(equipo: EquipoEntity): Int


    // LEFT JOIN: no se pierde un equipo aunque todavía no tenga empresa asignada
    @Query("""
        SELECT e.*,
               COALESCE(emp.nombre, '') AS nombre_empresa,
               COALESCE(emp.direccion, '') AS direccion_empresa,
               TRIM(COALESCE(cli.nombre, '') || ' ' || COALESCE(cli.apellido, '')) AS nombre_cliente
        FROM equipos e
        LEFT JOIN empresas emp ON e.id_empresa = emp.id_empresa
        LEFT JOIN usuarios cli ON e.id_cliente = cli.id_usuario
        ORDER BY e.id_equipo ASC
    """)
    fun obtenerTodos(): Flow<List<EquipoConDetalles>>


    @Query("""
        SELECT e.*,
               COALESCE(emp.nombre, '') AS nombre_empresa,
               COALESCE(emp.direccion, '') AS direccion_empresa,
               TRIM(COALESCE(cli.nombre, '') || ' ' || COALESCE(cli.apellido, '')) AS nombre_cliente
        FROM equipos e
        LEFT JOIN empresas emp ON e.id_empresa = emp.id_empresa
        LEFT JOIN usuarios cli ON e.id_cliente = cli.id_usuario
        WHERE e.id_cliente = :idCliente
        ORDER BY e.id_equipo ASC
    """)
    fun obtenerEquiposPorCliente(idCliente: Long): Flow<List<EquipoConDetalles>>


    @Query("SELECT * FROM equipos WHERE id_equipo = :idEquipo LIMIT 1")
    fun buscarEquipoPorId(idEquipo: Long): Flow<EquipoEntity?>


    @Query("UPDATE equipos SET direccion = :direccion, latitud = :lat, longitud = :lng WHERE id_equipo = :idEquipo")
    suspend fun actualizarUbicacion(idEquipo: Long, direccion: String, lat: Double, lng: Double)


    // Validar código repetido. Al crear se pasa idExcluir = 0; al editar, el id del equipo
    @Query("SELECT COUNT(*) FROM equipos WHERE LOWER(codigo) = LOWER(:codigo) AND id_equipo != :idExcluir")
    suspend fun contarPorCodigo(codigo: String, idExcluir: Long): Int


    // ---------- Empresas (el equipo pertenece a una empresa, por eso van en este DAO) ----------

    // IGNORE: si el nombre ya existe no pisa la fila de otra empresa, devuelve -1
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarEmpresa(empresa: EmpresaEntity): Long

    @Update
    suspend fun actualizarEmpresa(empresa: EmpresaEntity): Int

    @Query("SELECT * FROM empresas ORDER BY nombre ASC")
    fun obtenerEmpresas(): Flow<List<EmpresaEntity>>

    @Query("SELECT * FROM empresas WHERE id_empresa = :idEmpresa LIMIT 1")
    suspend fun buscarEmpresaPorId(idEmpresa: Long): EmpresaEntity?

    // Validar nombre repetido. Al crear se pasa idExcluir = 0; al editar, el id de la empresa
    @Query("SELECT COUNT(*) FROM empresas WHERE LOWER(nombre) = LOWER(:nombre) AND id_empresa != :idExcluir")
    suspend fun contarEmpresasPorNombre(nombre: String, idExcluir: Long): Int
}