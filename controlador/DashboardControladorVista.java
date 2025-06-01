package informviva.gest.controlador;

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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/dashboard")
public class DashboardControladorVista {

    private final VentaServicio ventaServicio;
    private final ProductoServicio productoServicio;
    private final ReporteServicio reporteServicio;

    public DashboardControladorVista(VentaServicio ventaServicio,
                                     ProductoServicio productoServicio,
                                     ReporteServicio reporteServicio) {
        this.ventaServicio = ventaServicio;
        this.productoServicio = productoServicio;
        this.reporteServicio = reporteServicio;
    }

    @GetMapping
    public String mostrarDashboard(Model model, Authentication authentication) {
        String username = authentication.getName();
        model.addAttribute("username", username);

        LocalDate hoy = LocalDate.now();
        model.addAttribute("currentDate", hoy);

        // Rango de la semana actual
        LocalDate inicioSemana = hoy.minusDays(hoy.getDayOfWeek().getValue() - 1);
        LocalDate finSemana = inicioSemana.plusDays(6);

        LocalDateTime inicioSemanaDT = inicioSemana.atStartOfDay();
        LocalDateTime finSemanaDT = finSemana.atTime(LocalTime.MAX);

        Double totalVentas = ventaServicio.calcularTotalVentas(inicioSemanaDT, finSemanaDT);
        Long totalTransacciones = ventaServicio.contarTransacciones(inicioSemanaDT, finSemanaDT);
        Long productosVendidos = ventaServicio.contarArticulosVendidos(inicioSemanaDT, finSemanaDT);
        Double ticketPromedio = ventaServicio.calcularTicketPromedio(inicioSemanaDT, finSemanaDT);

        Long clientesNuevos = reporteServicio.contarClientesNuevosEntreFechas(inicioSemana, finSemana);

        // Semana anterior
        LocalDate inicioSemanaAnterior = inicioSemana.minusWeeks(1);
        LocalDate finSemanaAnterior = finSemana.minusWeeks(1);

        LocalDateTime inicioSemanaAnteriorDT = inicioSemanaAnterior.atStartOfDay();
        LocalDateTime finSemanaAnteriorDT = finSemanaAnterior.atTime(LocalTime.MAX);

        Double totalVentasAnterior = ventaServicio.calcularTotalVentas(inicioSemanaAnteriorDT, finSemanaAnteriorDT);
        Long totalTransaccionesAnterior = ventaServicio.contarTransacciones(inicioSemanaAnteriorDT, finSemanaAnteriorDT);
        Long productosVendidosAnterior = ventaServicio.contarArticulosVendidos(inicioSemanaAnteriorDT, finSemanaAnteriorDT);
        Double ticketPromedioAnterior = ventaServicio.calcularTicketPromedio(inicioSemanaAnteriorDT, finSemanaAnteriorDT);

        Long clientesNuevosAnterior = reporteServicio.contarClientesNuevosEntreFechas(inicioSemanaAnterior, finSemanaAnterior);

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

        MetricaDTO ventasMetrica = new MetricaDTO(totalVentas, porcentajeCambioVentas);
        MetricaDTO transaccionesMetrica = new MetricaDTO(totalTransacciones, porcentajeCambioTransacciones);
        MetricaDTO ticketMetrica = new MetricaDTO(ticketPromedio, porcentajeCambioTicket);
        MetricaDTO clientesMetrica = new MetricaDTO(clientesNuevos, porcentajeCambioClientes);
        MetricaDTO productosMetrica = new MetricaDTO(productosVendidos, porcentajeCambioProductos);

        List<VentaPorPeriodoDTO> ventasPorDia = reporteServicio.obtenerVentasPorPeriodoEntreFechas(inicioSemana, finSemana);
        List<VentaPorCategoriaDTO> ventasPorCategoria = reporteServicio.obtenerVentasPorCategoriaEntreFechas(inicioSemana, finSemana);

        // Ventas recientes usando LocalDateTime
        List<Venta> ventasRecientes = ventaServicio.buscarPorRangoFechas(inicioSemanaDT, LocalDateTime.now());

        List<Producto> productosConBajoStock = productoServicio.listarConBajoStock(5);

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
