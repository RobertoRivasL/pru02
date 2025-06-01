package informviva.gest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaPorPeriodoDTO {
    private String periodo; // Etiqueta del período (ej.: "Enero 2024", "2024-01-15")
    private BigDecimal total; // Total de ventas en este período
}