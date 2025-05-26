package informviva.gest.controlador;

/**
 * @author Roberto Rivas
 * @version 2.0
 */

import informviva.gest.dto.MetricaDTO;
import informviva.gest.dto.VentaPorCategoriaDTO;
import informviva.gest.dto.VentaPorPeriodoDTO;
import informviva.gest.model.Producto;
import informviva.gest.model.Venta;
import informviva.gest.service.ProductoServicio;
import informviva.gest.service.ReporteServicio;
import informviva.gest.service.VentaServicio;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * API REST para los datos del dashboard
 *
 * @author Roberto Rivas
 * @version 2.1
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardControladorAPI {

    private final VentaServicio ventaServicio;
    private final ProductoServicio productoServicio;
    private final ReporteServicio reporteServicio;

    /**
     * Constructor con inyección de dependencias
     *
     * @param ventaServicio    Servicio de ventas
     * @param productoServicio Servicio de productos
     * @param reporteServicio  Servicio de reportes
     */
    public DashboardControladorAPI(VentaServicio ventaServicio,
                                   ProductoServicio productoServicio,
                                   ReporteServicio reporteServicio) {
        this.ventaServicio = ventaServicio;
        this.productoServicio = productoServicio;
        this.reporteServicio = reporteServicio;
    }

    /**
     * Endpoint para obtener datos actualizados para el dashboard
     */
    @GetMapping("/datos")
    public ResponseEntity<Map<String, Object>> obtenerDatosDashboard(
            @RequestParam(required = false, defaultValue = "semana") String periodo) {

        // Determinar rango de fechas según el período
        LocalDate hoy = LocalDate.now();
        LocalDate inicio;
        LocalDate fin = hoy;

        // Períodos anteriores para comparación
        LocalDate inicioAnterior;
        LocalDate finAnterior;

        // Refactorizado para eliminar código duplicado
        switch (periodo) {
            case "hoy":
                inicio = hoy;
                inicioAnterior = hoy.minusDays(1);
                finAnterior = inicioAnterior;
                break;
            case "mes":
                inicio = hoy.withDayOfMonth(1);
                inicioAnterior = inicio.minusMonths(1);
                finAnterior = inicioAnterior.plusMonths(1).minusDays(1);
                break;
            case "trimestre":
                int quarterMonth = (hoy.getMonthValue() - 1) / 3 * 3 + 1;
                inicio = hoy.withMonth(quarterMonth).withDayOfMonth(1);
                inicioAnterior = inicio.minusMonths(3);
                finAnterior = inicioAnterior.plusMonths(3).minusDays(1);
                break;
            case "año":
                inicio = hoy.withDayOfYear(1);
                inicioAnterior = inicio.minusYears(1);
                finAnterior = inicioAnterior.plusYears(1).minusDays(1);
                break;
            case "semana":
            default:
                inicio = hoy.minusDays(hoy.getDayOfWeek().getValue() - 1);
                inicioAnterior = inicio.minusWeeks(1);
                finAnterior = inicioAnterior.plusDays(6);
                break;
        }

        // Métricas para el período actual
        Double totalVentas = ventaServicio.calcularTotalVentas(inicio, fin);
        Long totalTransacciones = ventaServicio.contarTransacciones(inicio, fin);
        Double ticketPromedio = ventaServicio.calcularTicketPromedio(inicio, fin);
        Long clientesNuevos = reporteServicio.contarClientesNuevosEntreFechas(inicio, fin);
        Long productosVendidos = ventaServicio.contarArticulosVendidos(inicio, fin);

        // Métricas para el período anterior
        Double totalVentasAnterior = ventaServicio.calcularTotalVentas(inicioAnterior, finAnterior);
        Long totalTransaccionesAnterior = ventaServicio.contarTransacciones(inicioAnterior, finAnterior);
        Double ticketPromedioAnterior = ventaServicio.calcularTicketPromedio(inicioAnterior, finAnterior);
        Long clientesNuevosAnterior = reporteServicio.contarClientesNuevosEntreFechas(inicioAnterior, finAnterior);
        Long productosVendidosAnterior = ventaServicio.contarArticulosVendidos(inicioAnterior, finAnterior);

        // Cálculo de porcentajes de cambio
        Double porcentajeCambioVentas = ventaServicio.calcularPorcentajeCambio(totalVentas, totalVentasAnterior);
        Double porcentajeCambioTransacciones = ventaServicio.calcularPorcentajeCambio(
                totalTransacciones != null ? totalTransacciones.doubleValue() : 0.0,
                totalTransaccionesAnterior != null ? totalTransaccionesAnterior.doubleValue() : 0.0);
        Double porcentajeCambioTicket = ventaServicio.calcularPorcentajeCambio(ticketPromedio, ticketPromedioAnterior);
        Double porcentajeCambioClientes = ventaServicio.calcularPorcentajeCambio(
                clientesNuevos != null ? clientesNuevos.doubleValue() : 0.0,
                clientesNuevosAnterior != null ? clientesNuevosAnterior.doubleValue() : 0.0);
        Double porcentajeCambioProductos = ventaServicio.calcularPorcentajeCambio(
                productosVendidos != null ? productosVendidos.doubleValue() : 0.0,
                productosVendidosAnterior != null ? productosVendidosAnterior.doubleValue() : 0.0);

        // Crear DTOs para las métricas
        MetricaDTO ventasMetrica = new MetricaDTO(totalVentas, porcentajeCambioVentas);
        MetricaDTO transaccionesMetrica = new MetricaDTO(totalTransacciones, porcentajeCambioTransacciones);
        MetricaDTO ticketMetrica = new MetricaDTO(ticketPromedio, porcentajeCambioTicket);
        MetricaDTO clientesMetrica = new MetricaDTO(clientesNuevos, porcentajeCambioClientes);
        MetricaDTO productosMetrica = new MetricaDTO(productosVendidos, porcentajeCambioProductos);

        // Ventas por día/período para gráfico
        List<VentaPorPeriodoDTO> ventasPorPeriodo = reporteServicio.obtenerVentasPorPeriodoEntreFechas(inicio, fin);

        // Ventas por categoría para gráfico
        List<VentaPorCategoriaDTO> ventasPorCategoria = reporteServicio.obtenerVentasPorCategoriaEntreFechas(inicio, fin);

        // Ventas recientes
        List<Venta> ventasRecientes = ventaServicio.buscarPorRangoFechas(inicio, fin)
                .stream()
                .limit(10)
                .collect(Collectors.toList());

        // Crear DTOs simplificados para ventas recientes
        List<Map<String, Object>> ventasRecientesDTO = ventasRecientes.stream()
                .map(v -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", v.getId());
                    dto.put("fecha", v.getFecha());
                    dto.put("cliente", v.getCliente().getNombreCompleto());
                    dto.put("vendedor", v.getVendedor().getNombreCompleto());
                    dto.put("total", v.getTotal());
                    dto.put("estado", v.getEstado());
                    return dto;
                })
                .collect(Collectors.toList());

        // Productos con bajo stock - Corregido para usar la instancia y no el tipo
        List<Producto> productosConBajoStock = productoServicio.listarConBajoStock(5);

        // Crear DTOs simplificados para productos con bajo stock
        List<Map<String, Object>> productosConBajoStockDTO = productosConBajoStock.stream()
                .map(p -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", p.getId());
                    dto.put("nombre", p.getNombre());
                    dto.put("codigo", p.getCodigo());
                    dto.put("stock", p.getStock());
                    dto.put("categoria", p.getCategoria() != null ? p.getCategoria().getNombre() : "Sin categoría");
                    return dto;
                })
                .collect(Collectors.toList());

        // Preparar respuesta
        Map<String, Object> metricas = new HashMap<>();
        metricas.put("ventas", ventasMetrica);
        metricas.put("transacciones", transaccionesMetrica);
        metricas.put("ticket", ticketMetrica);
        metricas.put("clientes", clientesMetrica);
        metricas.put("productos", productosMetrica);

        Map<String, Object> graficos = new HashMap<>();
        graficos.put("ventasPorPeriodo", ventasPorPeriodo);
        graficos.put("ventasPorCategoria", ventasPorCategoria);

        Map<String, Object> tablas = new HashMap<>();
        tablas.put("ventasRecientes", ventasRecientesDTO);
        tablas.put("productosConBajoStock", productosConBajoStockDTO);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("periodo", periodo);
        respuesta.put("metricas", metricas);
        respuesta.put("graficos", graficos);
        respuesta.put("tablas", tablas);

        return ResponseEntity.ok(respuesta);
    }
}