package informviva.gest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor // Lombok generará el constructor (String, Double)
public class VentaPorCategoriaDTO {
    private String categoria;
    private Double total;
    // Elimina el constructor manual si lo añadiste
}