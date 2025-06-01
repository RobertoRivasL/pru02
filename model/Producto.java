package informviva.gest.model;

/**
 * @author Roberto Rivas
 * @version 2.0
 * @author Roberto Rivas
 * @version 2.0
 */


/**
 * @author Roberto Rivas
 * @version 2.0
 */


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa un producto en el sistema
 */
@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El código no puede estar vacío")
    @Column(unique = true)
    private String codigo;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El precio no puede ser nulo")
    @Positive(message = "El precio debe ser mayor que cero")
    private Double precio;

    @PositiveOrZero(message = "El stock no puede ser negativo")
    private Integer stock;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    private String marca;

    private String modelo;

    private Boolean activo = true;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;

    /**
     * Obtiene el nombre del producto formateado con su código
     * @return String con el formato [CODIGO] - Nombre del Producto
     */
    @Transient
    public String getNombreFormateado() {
        return "[" + codigo + "] - " + nombre;
    }

    /**
     * Verifica si el producto está disponible (stock > 0)
     * @return true si el producto está disponible, false en caso contrario
     */
    @Transient
    public boolean isDisponible() {
        return activo && stock != null && stock > 0;
    }

    /**
     * Actualiza el stock del producto
     * @param cantidad Cantidad a aumentar (positiva) o disminuir (negativa)
     * @throws IllegalArgumentException si el stock resultante es negativo
     */
    public void actualizarStock(int cantidad) {
        if (stock == null) {
            stock = 0;
        }

        int nuevoStock = stock + cantidad;
        if (nuevoStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo. Stock actual: "
                    + stock + ", cantidad a descontar: " + Math.abs(cantidad));
        }

        stock = nuevoStock;
    }
}
