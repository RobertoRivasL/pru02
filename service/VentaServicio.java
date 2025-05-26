package informviva.gest.service;

/**
 * @author Roberto Rivas
 * @version 2.0
 */

import informviva.gest.dto.VentaDTO;
import informviva.gest.model.Cliente;
import informviva.gest.model.Usuario;
import informviva.gest.model.Venta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Interfaz para la gestión de ventas
 */
public interface VentaServicio {

    /**
     * Obtiene todas las ventas
     *
     * @return Lista de ventas
     */
    List<Venta> listarTodas();

    /**
     * Obtiene ventas paginadas
     *
     * @param pageable Configuración de paginación
     * @return Página de ventas
     */
    Page<Venta> listarPaginadas(Pageable pageable);

    /**
     * Busca una venta por su ID
     *
     * @param id ID de la venta
     * @return Venta encontrada o null si no existe
     */
    Venta buscarPorId(Long id);

    /**
     * Guarda una nueva venta
     *
     * @param ventaDTO DTO con los datos de la venta
     * @return Venta guardada
     */
    Venta guardar(VentaDTO ventaDTO);

    /**
     * Actualiza una venta existente
     *
     * @param id       ID de la venta
     * @param ventaDTO DTO con los nuevos datos
     * @return Venta actualizada
     */
    Venta actualizar(Long id, VentaDTO ventaDTO);

    /**
     * Elimina una venta
     *
     * @param id ID de la venta
     */
    void eliminar(Long id);

    /**
     * Anula una venta sin eliminarla
     *
     * @param id ID de la venta
     * @return Venta anulada
     */
    Venta anular(Long id);

    /**
     * Busca ventas por rango de fechas
     *
     * @param inicio Fecha de inicio
     * @param fin    Fecha de fin
     * @return Lista de ventas en el rango
     */
    List<Venta> buscarPorRangoFechas(LocalDate inicio, LocalDate fin);

    /**
     * Busca ventas por cliente
     *
     * @param cliente Cliente
     * @return Lista de ventas del cliente
     */
    List<Venta> buscarPorCliente(Cliente cliente);

    /**
     * Busca ventas por vendedor
     *
     * @param vendedor Vendedor
     * @return Lista de ventas del vendedor
     */
    List<Venta> buscarPorVendedor(Usuario vendedor);

    /**
     * Verifica si existen ventas para un cliente
     *
     * @param clienteId ID del cliente
     * @return true si existen ventas, false en caso contrario
     */
    boolean existenVentasPorCliente(Long clienteId);

    /**
     * Verifica si existen ventas para un producto
     *
     * @param productoId ID del producto
     * @return true si existen ventas, false en caso contrario
     */
    boolean existenVentasPorProducto(Long productoId);

    /**
     * Convierte una entidad Venta a VentaDTO
     *
     * @param venta Entidad Venta
     * @return DTO de la venta
     */
    VentaDTO convertirADTO(Venta venta);

    /**
     * Obtiene el monto total de ventas en un rango de fechas
     *
     * @param inicio Fecha de inicio
     * @param fin    Fecha de fin
     * @return Monto total de ventas
     */
    Double calcularTotalVentas(LocalDate inicio, LocalDate fin);

    /**
     * Obtiene el monto total de todas las ventas
     *
     * @return Monto total de ventas
     */
    Double calcularTotalVentas();

    /**
     * Obtiene el número total de transacciones en un rango de fechas
     *
     * @param inicio Fecha de inicio
     * @param fin    Fecha de fin
     * @return Número total de transacciones
     */
    Long contarTransacciones(LocalDate inicio, LocalDate fin);

    /**
     * Obtiene el número total de artículos vendidos en un rango de fechas
     *
     * @param inicio Fecha de inicio
     * @param fin    Fecha de fin
     * @return Número total de artículos vendidos
     */
    Long contarArticulosVendidos(LocalDate inicio, LocalDate fin);

    /**
     * Calcula el ticket promedio en un rango de fechas
     *
     * @param inicio Fecha de inicio
     * @param fin    Fecha de fin
     * @return Ticket promedio
     */
    Double calcularTicketPromedio(LocalDate inicio, LocalDate fin);

    /**
     * Calcula el porcentaje de cambio entre dos períodos
     *
     * @param valorActual   Valor actual
     * @param valorAnterior Valor anterior
     * @return Porcentaje de cambio
     */
    Double calcularPorcentajeCambio(Double valorActual, Double valorAnterior);

//    **
//            * Obtiene el número de ventas realizadas hoy
//     * @return Número de ventas del día actual
//     */

    /**
     * Obtiene el número de ventas realizadas hoy
     *
     * @return Número de ventas del día actual
     */
    Long contarVentasHoy();

    Long contarVentasPorCliente(Long clienteId);

    Double calcularTotalVentasPorCliente(Long clienteId);

    Long contarUnidadesVendidasPorProducto(Long productoId);

    Double calcularIngresosPorProducto(Long productoId);

    List<Venta> buscarVentasRecientesPorProducto(Long productoId, int limite);

    List<Venta> buscarVentasRecientesPorCliente(Long clienteId, int limite);


}
