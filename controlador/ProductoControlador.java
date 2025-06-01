package informviva.gest.controlador;

/**
 * @author Roberto Rivas
 * @version 2.0
 */


import informviva.gest.model.Producto;
import informviva.gest.service.ProductoServicio;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoControlador {

    private final ProductoServicio productoServicio;

    public ProductoControlador(ProductoServicio productoServicio) {
        this.productoServicio = productoServicio;
    }

    @GetMapping
    public List<Producto> listarTodos() {
        return productoServicio.listar();
    }

    @PostMapping
    public ResponseEntity<Producto> guardarProducto(@Valid @RequestBody Producto producto) {
        producto.setFechaCreacion(LocalDateTime.now());
        producto.setFechaActualizacion(LocalDateTime.now());
        Producto guardado = productoServicio.guardar(producto);
        return ResponseEntity.ok(guardado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoServicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> buscarPorId(@PathVariable Long id) {
        Producto producto = productoServicio.buscarPorId(id);
        return producto != null ? ResponseEntity.ok(producto) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(@PathVariable Long id, @RequestBody Producto productoActualizado) {
        Producto existente = productoServicio.buscarPorId(id);
        if (existente == null) return ResponseEntity.notFound().build();

        existente.setCodigo(productoActualizado.getCodigo());
        existente.setNombre(productoActualizado.getNombre());
        existente.setDescripcion(productoActualizado.getDescripcion());
        existente.setPrecio(productoActualizado.getPrecio());
        existente.setStock(productoActualizado.getStock());
        existente.setMarca(productoActualizado.getMarca());
        existente.setModelo(productoActualizado.getModelo());
        existente.setFechaActualizacion(LocalDateTime.now());

        return ResponseEntity.ok(productoServicio.guardar(existente));
    }

    /**
     * Obtiene el número de ventas realizadas hoy
     * @return Número de ventas del día actual
     */

}
