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
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador para la vista del dashboard principal
 *
 * @author Roberto Rivas
 * @version 2.1
 */
@Controller
@RequestMapping("/dashboard")
public class DashboardControladorVista {

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
    public DashboardControladorVista(VentaServicio ventaServicio,
                                     ProductoServicio productoServicio,
                                     ReporteServicio reporteServicio) {
        this.ventaServicio = ventaServicio;
        this.productoServicio = productoServicio;
        this.reporteServicio = reporteServicio;
    }

    /**
     * Muestra el dashboard principal
     */
    @GetMapping
    public String mostrarDashboard(Model model, Authentication authentication) {
        // Obtener el nombre de usuario
        String username = authentication.getName();
        model.addAttribute("username", username);

        // Fecha actual
        LocalDate currentDate = LocalDate.now();
        model.addAttribute("currentDate", currentDate);

        // Períodos para cálculos
        LocalDate hoy = LocalDate.now();
        LocalDate inicioSemana = hoy.minusDays(hoy.getDayOfWeek().getValue() - 1);
        LocalDate finSemana = inicioSemana.plusDays(6);

        // Métricas de ventas para la semana actual
        Double totalVentas = ventaServicio.calcularTotalVentas(inicioSemana, finSemana);
        Long totalTransacciones = ventaServicio.contarTransacciones(inicioSemana, finSemana);
        Double ticketPromedio = ventaServicio.calcularTicketPromedio(inicioSemana, finSemana);
        Long clientesNuevos = reporteServicio.contarClientesNuevosEntreFechas(inicioSemana, finSemana);
        Long productosVendidos = ventaServicio.contarArticulosVendidos(inicioSemana, finSemana);

        // Métricas de la semana anterior para comparación
        LocalDate inicioSemanaAnterior = inicioSemana.minusWeeks(1);
        LocalDate finSemanaAnterior = finSemana.minusWeeks(1);

        Double totalVentasAnterior = ventaServicio.calcularTotalVentas(inicioSemanaAnterior, finSemanaAnterior);
        Long totalTransaccionesAnterior = ventaServicio.contarTransacciones(inicioSemanaAnterior, finSemanaAnterior);
        Double ticketPromedioAnterior = ventaServicio.calcularTicketPromedio(inicioSemanaAnterior, finSemanaAnterior);
        Long clientesNuevosAnterior = reporteServicio.contarClientesNuevosEntreFechas(inicioSemanaAnterior, finSemanaAnterior);
        Long productosVendidosAnterior = ventaServicio.contarArticulosVendidos(inicioSemanaAnterior, finSemanaAnterior);

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

        // Ventas por día para gráfico
        List<VentaPorPeriodoDTO> ventasPorDia = reporteServicio.obtenerVentasPorPeriodoEntreFechas(inicioSemana, finSemana);

        // Ventas por categoría para gráfico
        List<VentaPorCategoriaDTO> ventasPorCategoria = reporteServicio.obtenerVentasPorCategoriaEntreFechas(inicioSemana, finSemana);

        // Ventas recientes
        List<Venta> ventasRecientes = ventaServicio.buscarPorRangoFechas(inicioSemana, hoy);

        // Productos con bajo stock
        List<Producto> productosConBajoStock = productoServicio.listarConBajoStock(5);

        // Agregar atributos al modelo
        model.addAttribute("ventasMetrica", ventasMetrica);
        model.addAttribute("transaccionesMetrica", transaccionesMetrica);
        model.addAttribute("ticketMetrica", ticketMetrica);
        model.addAttribute("clientesMetrica", clientesMetrica);
        model.addAttribute("productosMetrica", productosMetrica);

        model.addAttribute("ventasPorDiaData", ventasPorDia);
        model.addAttribute("ventasPorCategoriaData", ventasPorCategoria);
        model.addAttribute("ventasRecientes", ventasRecientes);
        model.addAttribute("productosConBajoStock", productosConBajoStock);

        return "dashboard";
    }
}