package informviva.gest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor // Lombok generará el constructor (String, Long, Double, Double)
public class ProductoVendidoDTO {
    private String nombre;
    private Long unidadesVendidas;
    private Double ingresos;
    private Double porcentajeTotal;
    // Elimina el constructor manual si lo añadiste
}