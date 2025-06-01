package informviva.gest.service;

/**
 * @author Roberto Rivas
 * @version 2.0
 **/

import informviva.gest.dto.ProductoVendidoDTO;
import informviva.gest.dto.VentaPorCategoriaDTO;
import informviva.gest.dto.VentaPorPeriodoDTO;
import informviva.gest.dto.VentaResumenDTO;
import informviva.gest.repository.ReporteRepositorio;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReporteServicio {

    private final ReporteRepositorio reporteRepository;

    public ReporteServicio(ReporteRepositorio reporteRepository) {
        this.reporteRepository = reporteRepository;
    }

    public VentaResumenDTO generarResumenVentas(LocalDate startDate, LocalDate endDate) {
        VentaResumenDTO resumen = new VentaResumenDTO();

        // Convertir LocalDate a LocalDateTime para consultas con campos de tipo LocalDateTime
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        resumen.setTotalVentas(reporteRepository.sumarTotalVentasEntreFechas(startDateTime, endDateTime));
        resumen.setTotalTransacciones(reporteRepository.contarVentasEntreFechas(startDateTime, endDateTime));
        resumen.setTotalArticulosVendidos(reporteRepository.sumarCantidadArticulosVendidosEntreFechas(startDateTime, endDateTime));

        // Para clientes nuevos, usar LocalDate directamente si el campo es LocalDate
        resumen.setClientesNuevos(reporteRepository.contarClientesNuevosEntreFechas(startDate, endDate));

        BigDecimal ticketPromedio = BigDecimal.ZERO;
        if (resumen.getTotalTransacciones() != null && resumen.getTotalTransacciones() > 0 && resumen.getTotalVentas() != null) {
            ticketPromedio = resumen.getTotalVentas().divide(BigDecimal.valueOf(resumen.getTotalTransacciones()), 2, RoundingMode.HALF_UP);
        }
        resumen.setTicketPromedio(ticketPromedio);

        resumen.setPorcentajeCambioVentas(null);
        resumen.setPorcentajeCambioTransacciones(null);
        resumen.setPorcentajeCambioTicketPromedio(null);
        resumen.setPorcentajeCambioClientesNuevos(null);

        List<ProductoVendidoDTO> productosVendidos = reporteRepository.obtenerProductosMasVendidosEntreFechas(startDateTime, endDateTime);

        BigDecimal totalVentasGeneral = resumen.getTotalVentas() != null ? resumen.getTotalVentas() : BigDecimal.ZERO;
        if (totalVentasGeneral.compareTo(BigDecimal.ZERO) > 0) {
            productosVendidos.forEach(producto -> {
                BigDecimal ingresosBigDecimal = BigDecimal.valueOf(producto.getIngresos());
                double porcentaje = ingresosBigDecimal
                        .divide(totalVentasGeneral, 4, RoundingMode.HALF_UP)
                        .doubleValue() * 100.0;
                producto.setPorcentajeTotal(porcentaje);
            });
        } else {
            productosVendidos.forEach(producto -> producto.setPorcentajeTotal(0.0));
        }
        resumen.setProductosMasVendidos(productosVendidos);

        resumen.setVentasPorPeriodo(reporteRepository.obtenerVentasPorPeriodoEntreFechas(startDateTime, endDateTime));
        resumen.setVentasPorCategoria(reporteRepository.obtenerVentasPorCategoriaEntreFechas(startDateTime, endDateTime));
        resumen.setVentasPorVendedor(reporteRepository.obtenerVentasPorVendedorEntreFechas(startDateTime, endDateTime));

        return resumen;
    }

    public List<VentaPorPeriodoDTO> obtenerVentasPorPeriodoEntreFechas(LocalDate inicio, LocalDate fin) {
        // Convertir LocalDate a LocalDateTime
        LocalDateTime startDateTime = inicio.atStartOfDay();
        LocalDateTime endDateTime = fin.atTime(LocalTime.MAX);

        return reporteRepository.obtenerVentasPorPeriodoEntreFechas(startDateTime, endDateTime);
    }

    public List<VentaPorCategoriaDTO> obtenerVentasPorCategoriaEntreFechas(LocalDate inicio, LocalDate fin) {
        // Convertir LocalDate a LocalDateTime
        LocalDateTime startDateTime = inicio.atStartOfDay();
        LocalDateTime endDateTime = fin.atTime(LocalTime.MAX);

        return reporteRepository.obtenerVentasPorCategoriaEntreFechas(startDateTime, endDateTime);
    }

    public Long contarClientesNuevosEntreFechas(LocalDate inicio, LocalDate fin) {
        // Este método usa LocalDate directamente porque el campo fechaRegistro en Cliente es LocalDate
        return reporteRepository.contarClientesNuevosEntreFechas(inicio, fin);
    }

    /**
     * Método auxiliar para convertir LocalDate a LocalDateTime inicio del día
     */
    private LocalDateTime toStartOfDay(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }

    /**
     * Método auxiliar para convertir LocalDate a LocalDateTime fin del día
     */
    private LocalDateTime toEndOfDay(LocalDate date) {
        return date != null ? date.atTime(LocalTime.MAX) : null;
    }
}