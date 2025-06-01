package informviva.gest.repository;

import informviva.gest.model.Cliente;
import informviva.gest.model.Usuario;
import informviva.gest.model.Venta;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para acceder a las entidades Venta en la base de datos.
 * Proporciona métodos para consultas específicas relacionadas con ventas.
 *
 * @author Roberto Rivas
 * @version 2.1
 */
@Repository
public interface VentaRepositorio extends JpaRepository<Venta, Long> {

    /**
     * Verifica si existen ventas para un cliente específico
     *
     * @param clienteId ID del cliente
     * @return true si existen ventas, false en caso contrario
     */
    boolean existsByClienteId(Long clienteId);

    /**
     * Verifica si existen ventas que contienen un producto específico
     *
     * @param productoId ID del producto
     * @return true si existen ventas con el producto, false en caso contrario
     */
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM Venta v WHERE EXISTS (SELECT 1 FROM VentaDetalle vd WHERE vd.venta = v AND vd.producto.id = :productoId)")
    boolean existsByDetallesProductoId(@Param("productoId") Long productoId);

    /**
     * Busca ventas de un cliente específico
     *
     * @param cliente Entidad Cliente
     * @return Lista de ventas del cliente
     */
    List<Venta> findByCliente(Cliente cliente);

    /**
     * Busca ventas de un vendedor específico
     *
     * @param vendedor Entidad Usuario que representa al vendedor
     * @return Lista de ventas del vendedor
     */
    List<Venta> findByVendedor(Usuario vendedor);

    /**
     * Busca ventas en un rango de fechas
     * IMPORTANTE: Ahora recibe LocalDateTime para compatibilidad con el modelo
     *
     * @param start Fecha y hora de inicio
     * @param end   Fecha y hora de fin
     * @return Lista de ventas en el rango de fechas
     */
    @Query("SELECT v FROM Venta v WHERE v.fecha BETWEEN :start AND :end")
    List<Venta> findByFechaBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Cuenta el número de ventas en un rango de fechas excluyendo un estado específico
     *
     * @param inicio Fecha y hora de inicio
     * @param fin    Fecha y hora de fin
     * @param estado Estado a excluir
     * @return Número de ventas
     */
    @Query("SELECT COUNT(v) FROM Venta v WHERE v.fecha BETWEEN :inicio AND :fin AND v.estado != :estado")
    Long countByFechaBetweenAndEstadoNot(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin, @Param("estado") String estado);

    /**
     * Calcula el total de ingresos en un rango de fechas
     *
     * @param start Fecha y hora de inicio
     * @param end   Fecha y hora de fin
     * @return Total de ingresos
     */
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.fecha BETWEEN :start AND :end AND v.estado != 'ANULADA'")
    Double calcularTotalIngresos(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Cuenta el número de transacciones en un rango de fechas
     *
     * @param start Fecha y hora de inicio
     * @param end   Fecha y hora de fin
     * @return Número de transacciones
     */
    @Query("SELECT COUNT(v) FROM Venta v WHERE v.fecha BETWEEN :start AND :end AND v.estado != 'ANULADA'")
    Long contarTransacciones(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Cuenta la cantidad de artículos vendidos en un rango de fechas
     *
     * @param start Fecha y hora de inicio
     * @param end   Fecha y hora de fin
     * @return Cantidad de artículos vendidos
     */
    @Query("SELECT COALESCE(SUM(vd.cantidad), 0) FROM VentaDetalle vd JOIN vd.venta v WHERE v.fecha BETWEEN :start AND :end AND v.estado != 'ANULADA'")
    Long contarArticulosVendidos(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Método helper para contar artículos vendidos entre fechas (compatibilidad)
     *
     * @param inicio Fecha y hora de inicio
     * @param fin    Fecha y hora de fin
     * @return Cantidad de artículos vendidos
     */
    default Long countArticulosVendidosBetweenFechas(LocalDateTime inicio, LocalDateTime fin) {
        return contarArticulosVendidos(inicio, fin);
    }

    @Query("SELECT COUNT(v) FROM Venta v WHERE v.cliente.id = :clienteId")
    Long countByClienteId(Long clienteId);

    @Query("SELECT SUM(v.total) FROM Venta v WHERE v.cliente.id = :clienteId")
    Double calcularTotalPorCliente(Long clienteId);

    @Query("SELECT SUM(v.cantidad) FROM Venta v WHERE v.producto.id = :productoId")
    Long contarUnidadesVendidasPorProducto(Long productoId);

    @Query("SELECT SUM(v.total) FROM Venta v WHERE v.producto.id = :productoId")
    Double calcularIngresosPorProducto(Long productoId);

    @Query("SELECT v FROM Venta v WHERE v.producto.id = :productoId ORDER BY v.fecha DESC")
    List<Venta> buscarVentasRecientesPorProducto(Long productoId, Pageable limite);

    @Query("SELECT v FROM Venta v WHERE v.cliente.id = :clienteId ORDER BY v.fecha DESC")
    List<Venta> findTopByClienteIdOrderByFechaDesc(@Param("clienteId") Long clienteId, Pageable pageable);
}