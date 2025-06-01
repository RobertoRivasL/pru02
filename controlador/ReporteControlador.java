package informviva.gest.controlador;

import informviva.gest.dto.VentaResumenDTO;
import informviva.gest.service.ReporteServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class ReporteControlador {

    @Autowired
    private ReporteServicio reporteServicio;

    @GetMapping("/reportes/panel-ventas")
    public String mostrarPanelVentas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model modelo
    ) {
        // Si no se pasa startDate o endDate, usa el mes actual por defecto
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);  // Un mes atrás
        }
        if (endDate == null) {
            endDate = LocalDate.now();  // Hasta hoy
        }

        // Generar resumen de ventas
        VentaResumenDTO resumen = reporteServicio.generarResumenVentas(startDate, endDate);
        modelo.addAttribute("ventaSummary", resumen);
        modelo.addAttribute("startDate", startDate);
        modelo.addAttribute("endDate", endDate);

        return "reportes"; // Retorna la vista "reportes.html"
    }
}