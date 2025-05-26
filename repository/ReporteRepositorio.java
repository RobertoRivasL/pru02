package informviva.gest.repository;

import informviva.gest.dto.ProductoVendidoDTO;
import informviva.gest.dto.VentaPorCategoriaDTO;
import informviva.gest.dto.VentaPorPeriodoDTO;
import informviva.gest.dto.VentaPorVendedorDTO;
import informviva.gest.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReporteRepositorio extends JpaRepository<Venta, Long> {

    // Consulta para obtener el Total de Ventas (suma del campo 'total' de Venta)
    // Retorna BigDecimal para precisión monetaria. COALESCE(..., 0) maneja el caso sin ventas.
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.fecha BETWEEN :startDate AND :endDate")
    BigDecimal sumarTotalVentasEntreFechas(LocalDate startDate, LocalDate endDate);

    // Consulta para contar el Total de Transacciones (número de registros de Venta)
    @Query("SELECT COUNT(v) FROM Venta v WHERE v.fecha BETWEEN :startDate AND :endDate")
    Long contarVentasEntreFechas(LocalDate startDate, LocalDate endDate);

    // Consulta para obtener el Total de Artículos Vendidos (suma de cantidades en VentaDetalle)
    // Requiere JOIN con VentaDetalle. Retorna Long para la cantidad total. COALESCE(..., 0) maneja el caso sin ventas.
    @Query("SELECT COALESCE(SUM(vd.cantidad), 0L) FROM VentaDetalle vd JOIN vd.venta v WHERE v.fecha BETWEEN :startDate AND :endDate")
    // Usar 0L para Long
    Long sumarCantidadArticulosVendidosEntreFechas(LocalDate startDate, LocalDate endDate);

    // Consulta para contar Clientes Nuevos (basado en fechaRegistro en la entidad Cliente)
    // Retorna Long para el conteo.
    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.fechaRegistro BETWEEN :startDate AND :endDate")
    Long contarClientesNuevosEntreFechas(LocalDate startDate, LocalDate endDate);

    // Consulta para obtener Productos Más Vendidos (unidades e ingresos por producto)
    // Retorna una lista de DTOs ProductoVendidoDTO.
    // Usa JPQL SELECT new DTO(...). CAST se usa para asegurar tipo String.
    @Query("SELECT new informviva.gest.dto.ProductoVendidoDTO(CAST(p.nombre AS string), SUM(vd.cantidad), SUM(vd.total), 0.0) " + // 0.0 para porcentaje, se calcula en el servicio
            "FROM VentaDetalle vd JOIN vd.producto p JOIN vd.venta v " +
            "WHERE v.fecha BETWEEN :startDate AND :endDate " +
            "GROUP BY p.nombre " + // Agrupar por nombre de producto
            "ORDER BY SUM(vd.cantidad) DESC")
    // Ordenar por unidades vendidas (más vendido)
    List<ProductoVendidoDTO> obtenerProductosMasVendidosEntreFechas(LocalDate startDate, LocalDate endDate);

    // Consulta para obtener Ventas por Período (agrupado por día como String)
    // Retorna una lista de DTOs VentaPorPeriodoDTO.
    // Usa Native Query = true porque utiliza la función DATE_FORMAT (específica de MySQL).
    // **Verifica la compatibilidad de DATE_FORMAT con tu base de datos.**
    @Query(value = "SELECT DATE_FORMAT(v.fecha, '%Y-%m-%d') AS periodo, SUM(v.total) AS total " + // Alias 'periodo' y 'total' para mapear al DTO
            "FROM ventas v WHERE v.fecha BETWEEN :startDate AND :endDate " + // Usar nombre de tabla real 'ventas'
            "GROUP BY DATE_FORMAT(v.fecha, '%Y-%m-%d') " +
            "ORDER BY DATE_FORMAT(v.fecha, '%Y-%m-%d') ASC", nativeQuery = true)
    List<VentaPorPeriodoDTO> obtenerVentasPorPeriodoEntreFechas(LocalDate startDate, LocalDate endDate);

    // Consulta para obtener Ventas por Categoría (agrupado por nombre de categoría)
    // Retorna una lista de DTOs VentaPorCategoriaDTO.
    // Usa JPQL SELECT new DTO(...). CAST se usa para asegurar tipo String. COALESCE con 0.0 asegura tipo Double.
    @Query("SELECT new informviva.gest.dto.VentaPorCategoriaDTO(CAST(cat.nombre AS string), COALESCE(SUM(vd.total), 0.0)) " + // SUM(vd.total) es Double, COALESCE(..., 0.0) es Double
            "FROM VentaDetalle vd JOIN vd.producto p JOIN p.categoria cat JOIN vd.venta v " +
            "WHERE v.fecha BETWEEN :startDate AND :endDate " +
            "GROUP BY cat.nombre " + // Agrupar por nombre de categoría
            "ORDER BY cat.nombre ASC")
    List<VentaPorCategoriaDTO> obtenerVentasPorCategoriaEntreFechas(LocalDate startDate, LocalDate endDate);

    // Consulta para obtener Ventas por Vendedor (agrupado por username del vendedor)
    // Retorna una lista de DTOs VentaPorVendedorDTO.
    // Usa JPQL SELECT new DTO(...). CAST se usa para asegurar tipo String. COALESCE con 0.0 asegura tipo Double.
    @Query("SELECT new informviva.gest.dto.VentaPorVendedorDTO(CAST(u.username AS string), COALESCE(SUM(v.total), 0.0)) " + // SUM(v.total) es Double, COALESCE(..., 0.0) es Double
            "FROM Venta v JOIN v.vendedor u " + // v.vendedor es la relación a Usuario
            "WHERE v.fecha BETWEEN :startDate AND :endDate " +
            "GROUP BY u.username " + // Agrupar por username del vendedor
            "ORDER BY u.username ASC")
    List<VentaPorVendedorDTO> obtenerVentasPorVendedorEntreFechas(LocalDate startDate, LocalDate endDate);

    // Nota: Los métodos originales obtenerVentasPorDia y obtenerVentasPorProducto (que retornaban List<Object[]>)
    // ya no son necesarios si usas los nuevos métodos que retornan DTOs. Puedes eliminarlos si no los usas.

}