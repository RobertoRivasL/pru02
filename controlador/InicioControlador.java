package informviva.gest.controlador;

import informviva.gest.dto.MetricaDTO;
import informviva.gest.dto.VentaResumenDTO;
import informviva.gest.model.Producto;
import informviva.gest.model.Venta;
import informviva.gest.service.ProductoServicio;
import informviva.gest.service.ReporteServicio;
import informviva.gest.service.VentaServicio;
import informviva.gest.util.RutasConstantes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Controller
public class InicioControlador {

    private static final Logger logger = LoggerFactory.getLogger(InicioControlador.class);
    private static final int LIMITE_PRODUCTOS_BAJO_STOCK = 5;
    private static final int LIMITE_VENTAS_RECIENTES = 10;

    private final VentaServicio ventaServicio;
    private final ProductoServicio productoServicio;
    private final ReporteServicio reporteServicio;

    public InicioControlador(VentaServicio ventaServicio,
                             ProductoServicio productoServicio,
                             ReporteServicio reporteServicio) {
        this.ventaServicio = ventaServicio;
        this.productoServicio = productoServicio;
        this.reporteServicio = reporteServicio;
    }

    @GetMapping({"/", "/inicio"})
    public String mostrarInicio(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            cargarPanel(model);
        }
        return "inicio";
    }

    private void cargarPanel(Model model) {
        VentaResumenDTO resumen = obtenerResumenVentas();
        cargarVentasDelDia(model);
        cargarProductosBajoStock(model);
        cargarMetricas(model, resumen);
        cargarVentasRecientes(model);
        cargarVentasPorResumen(model, resumen);
    }

    private VentaResumenDTO obtenerResumenVentas() {
        try {
            LocalDate hoy = LocalDate.now();
            LocalDate inicioMes = hoy.withDayOfMonth(1);
            return reporteServicio.generarResumenVentas(inicioMes, hoy);
        } catch (Exception e) {
            logger.error("Error generando resumen de ventas: {}", e.getMessage());
            return crearResumenVentasVacio();
        }
    }

    private VentaResumenDTO crearResumenVentasVacio() {
        VentaResumenDTO dto = new VentaResumenDTO();
        dto.setTotalVentas(BigDecimal.ZERO);
        dto.setTotalTransacciones(0L);
        dto.setTicketPromedio(BigDecimal.ZERO);
        dto.setClientesNuevos(0L);
        dto.setProductosMasVendidos(Collections.emptyList());
        dto.setVentasPorPeriodo(Collections.emptyList());
        dto.setVentasPorCategoria(Collections.emptyList());
        return dto;
    }

    private void cargarVentasDelDia(Model model) {
        try {
            LocalDate hoy = LocalDate.now();
            Long ventasHoy = ventaServicio.contarTransacciones(hoy, hoy);
            model.addAttribute(RutasConstantes.VENTAS_HOY, ventasHoy);
        } catch (Exception e) {
            logger.error("Error contando ventas del día: {}", e.getMessage());
            model.addAttribute(RutasConstantes.VENTAS_HOY, 0L);
        }
    }

    private void cargarProductosBajoStock(Model model) {
        try {
            List<Producto> productos = productoServicio.listarConBajoStock(LIMITE_PRODUCTOS_BAJO_STOCK);
            model.addAttribute(RutasConstantes.PRODUCTOS_BAJO_STOCK, productos);
            model.addAttribute(RutasConstantes.PRODUCTOS_BAJOS, (long) productos.size());
        } catch (Exception e) {
            logger.error("Error obteniendo productos con bajo stock: {}", e.getMessage());
            model.addAttribute(RutasConstantes.PRODUCTOS_BAJO_STOCK, Collections.emptyList());
            model.addAttribute(RutasConstantes.PRODUCTOS_BAJOS, 0L);
        }
    }

    private void cargarMetricas(Model model, VentaResumenDTO resumen) {
        model.addAttribute(RutasConstantes.INGRESOS_MES, resumen.getTotalVentas());

        model.addAttribute("ventasMetrica", MetricaDTO.of(
                resumen.getTotalVentas(), resumen.getPorcentajeCambioVentas()));
        model.addAttribute("transaccionesMetrica", MetricaDTO.of(
                resumen.getTotalTransacciones(), resumen.getPorcentajeCambioTransacciones()));
        model.addAttribute("ticketMetrica", MetricaDTO.of(
                resumen.getTicketPromedio(), resumen.getPorcentajeCambioTicketPromedio()));
        model.addAttribute("clientesMetrica", MetricaDTO.of(
                resumen.getClientesNuevos(), resumen.getPorcentajeCambioClientesNuevos()));
        model.addAttribute("clientesNuevos", resumen.getClientesNuevos());
    }

    private void cargarVentasRecientes(Model model) {
        try {
            LocalDate hoy = LocalDate.now();
            LocalDate inicioMes = hoy.withDayOfMonth(1);
            List<Venta> ventas = ventaServicio.buscarPorRangoFechas(inicioMes, hoy);
            if (ventas.size() > LIMITE_VENTAS_RECIENTES) {
                ventas = ventas.subList(0, LIMITE_VENTAS_RECIENTES);
            }
            model.addAttribute(RutasConstantes.VENTAS_RECIENTES, ventas);
        } catch (Exception e) {
            logger.error("Error buscando ventas recientes: {}", e.getMessage());
            model.addAttribute(RutasConstantes.VENTAS_RECIENTES, Collections.emptyList());
        }
    }

    private void cargarVentasPorResumen(Model model, VentaResumenDTO resumen) {
        model.addAttribute("productosMasVendidos",
                resumen.getProductosMasVendidos() != null ? resumen.getProductosMasVendidos() : Collections.emptyList());
        model.addAttribute("ventasPorPeriodo",
                resumen.getVentasPorPeriodo() != null ? resumen.getVentasPorPeriodo() : Collections.emptyList());
        model.addAttribute("ventasPorCategoria",
                resumen.getVentasPorCategoria() != null ? resumen.getVentasPorCategoria() : Collections.emptyList());
        model.addAttribute("resumenVentas", resumen);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_VENTAS')")
    @GetMapping("/panel-ventas")
    public String mostrarPanelVentas(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model) {
        try {
            // Crear fechas por defecto
            LocalDate hoy = LocalDate.now();
            LocalDate inicioMes = hoy.withDayOfMonth(1);

            // Procesar fechas del filtro si existen
            LocalDate fechaInicio = inicioMes;
            LocalDate fechaFin = hoy;

            try {
                if (startDate != null && !startDate.isEmpty()) {
                    fechaInicio = LocalDate.parse(startDate);
                }
                if (endDate != null && !endDate.isEmpty()) {
                    fechaFin = LocalDate.parse(endDate);
                }
            } catch (Exception e) {
                logger.warn("Error al parsear fechas del filtro: {}", e.getMessage());
                // Si hay error en el formato de fecha, usamos las fechas por defecto
            }

            // Validar que la fecha de inicio no sea posterior a la fecha fin
            if (fechaInicio.isAfter(fechaFin)) {
                fechaInicio = inicioMes;
                fechaFin = hoy;
            }

            // Agregar fechas al modelo para los inputs
            model.addAttribute("startDate", fechaInicio);
            model.addAttribute("endDate", fechaFin);

            // Generar el resumen con las fechas correspondientes
            VentaResumenDTO resumenVentas = reporteServicio.generarResumenVentas(fechaInicio, fechaFin);

            // Asegurar que no haya valores nulos para evitar errores en el JavaScript
            if (resumenVentas.getVentasPorVendedor() == null) {
                resumenVentas.setVentasPorVendedor(Collections.emptyList());
            }
            if (resumenVentas.getVentasPorPeriodo() == null) {
                resumenVentas.setVentasPorPeriodo(Collections.emptyList());
            }
            if (resumenVentas.getVentasPorCategoria() == null) {
                resumenVentas.setVentasPorCategoria(Collections.emptyList());
            }
            if (resumenVentas.getProductosMasVendidos() == null) {
                resumenVentas.setProductosMasVendidos(Collections.emptyList());
            }

            // Agregar al modelo con el nombre que espera la plantilla
            model.addAttribute("ventaSummary", resumenVentas);

        } catch (Exception e) {
            logger.error("Error cargando datos para panel de ventas: {}", e.getMessage());
            // Crear un DTO vacío con todas las listas inicializadas
            VentaResumenDTO dto = new VentaResumenDTO();
            dto.setTotalVentas(BigDecimal.ZERO);
            dto.setTotalTransacciones(0L);
            dto.setTicketPromedio(BigDecimal.ZERO);
            dto.setClientesNuevos(0L);
            dto.setProductosMasVendidos(Collections.emptyList());
            dto.setVentasPorPeriodo(Collections.emptyList());
            dto.setVentasPorCategoria(Collections.emptyList());
            dto.setVentasPorVendedor(Collections.emptyList());

            model.addAttribute("ventaSummary", dto);

            // Agregar fechas por defecto en caso de error
            LocalDate hoy = LocalDate.now();
            LocalDate inicioMes = hoy.withDayOfMonth(1);
            model.addAttribute("startDate", inicioMes);
            model.addAttribute("endDate", hoy);
        }
        return "panel-ventas";
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_PRODUCTOS')")
    @GetMapping("/productos")
    public String mostrarGestionProductos() {
        return "productos";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/clientes-desde-inicio")
    public String mostrarClientesDesdeInicio() {
        return "/clientes/lista-clientes";
    }

    @GetMapping("/contacto")
    public String mostrarContacto() {
        return "contacto";
    }
}


