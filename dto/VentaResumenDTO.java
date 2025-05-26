package informviva.gest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data // Proporciona Getters, Setters, toString, equals, hashCode
@NoArgsConstructor // Constructor sin argumentos
@AllArgsConstructor // Constructor con todos los argumentos (puedes necesitar constructores adicionales si prefieres)
public class VentaResumenDTO {

    // --- Métricas de Resumen (basado en reportes.html y panel-ventas.html) ---
    // Usamos BigDecimal para manejar dinero con precisión
    private BigDecimal totalVentas;
    private Long totalTransacciones;
    private Long totalArticulosVendidos;
    private BigDecimal ticketPromedio;
    private Long clientesNuevos;

    // --- Métricas de Comparación (basado en panel-ventas.html) ---
    private Double porcentajeCambioVentas;
    private Double porcentajeCambioTransacciones;
    private Double porcentajeCambioTicketPromedio;
    private Double porcentajeCambioClientesNuevos;

    // --- Datos para Gráficos y Tablas (basado en reportes.html y panel-ventas.html) ---
    private List<ProductoVendidoDTO> productosMasVendidos;
    private List<VentaPorPeriodoDTO> ventasPorPeriodo;
    private List<VentaPorCategoriaDTO> ventasPorCategoria;
    private List<VentaPorVendedorDTO> ventasPorVendedor;

    // Nota: Los campos 'labels' y 'data' originales se eliminan ya que las listas específicas contienen esta info.
}