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
import java.util.List;

@Service
public class ReporteServicio {

    private final ReporteRepositorio reporteRepository;

    public ReporteServicio(ReporteRepositorio reporteRepository) {
        this.reporteRepository = reporteRepository;
    }

    public VentaResumenDTO generarResumenVentas(LocalDate startDate, LocalDate endDate) {
        VentaResumenDTO resumen = new VentaResumenDTO();

        resumen.setTotalVentas(reporteRepository.sumarTotalVentasEntreFechas(startDate, endDate));
        resumen.setTotalTransacciones(reporteRepository.contarVentasEntreFechas(startDate, endDate));
        resumen.setTotalArticulosVendidos(reporteRepository.sumarCantidadArticulosVendidosEntreFechas(startDate, endDate));
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

        List<ProductoVendidoDTO> productosVendidos = reporteRepository.obtenerProductosMasVendidosEntreFechas(startDate, endDate);

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

        resumen.setVentasPorPeriodo(reporteRepository.obtenerVentasPorPeriodoEntreFechas(startDate, endDate));
        resumen.setVentasPorCategoria(reporteRepository.obtenerVentasPorCategoriaEntreFechas(startDate, endDate));
        resumen.setVentasPorVendedor(reporteRepository.obtenerVentasPorVendedorEntreFechas(startDate, endDate));

        return resumen;
    }

    public List<VentaPorPeriodoDTO> obtenerVentasPorPeriodoEntreFechas(LocalDate inicio, LocalDate fin) {
        return reporteRepository.obtenerVentasPorPeriodoEntreFechas(inicio, fin);
    }

    public List<VentaPorCategoriaDTO> obtenerVentasPorCategoriaEntreFechas(LocalDate inicio, LocalDate fin) {
        return reporteRepository.obtenerVentasPorCategoriaEntreFechas(inicio, fin);
    }

    public Long contarClientesNuevosEntreFechas(LocalDate inicio, LocalDate fin) {
        return reporteRepository.contarClientesNuevosEntreFechas(inicio, fin);
    }
}
