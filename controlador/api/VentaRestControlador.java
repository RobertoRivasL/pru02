package informviva.gest.controlador.api;

import informviva.gest.dto.VentaDTO;
import informviva.gest.exception.RecursoNoEncontradoException;
import informviva.gest.exception.StockInsuficienteException;
import informviva.gest.model.Cliente;
import informviva.gest.model.Producto;
import informviva.gest.model.Venta;
import informviva.gest.service.ClienteServicio;
import informviva.gest.service.ProductoServicio;
import informviva.gest.service.VentaServicio;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador REST para la API de ventas
 *
 * @author Roberto Rivas
 * @version 1.0
 */
@RestController
@RequestMapping("/api/ventas")
public class VentaRestControlador {

    private static final String ERROR_CREAR = "Error al crear la venta: ";
    private static final String ERROR_ACTUALIZAR = "Error al actualizar la venta: ";
    private static final String ERROR_ANULAR = "Error al anular la venta: ";

    private final VentaServicio ventaServicio;
    private final ProductoServicio productoServicio;
    private final ClienteServicio clienteServicio;

    public VentaRestControlador(VentaServicio ventaServicio,
                                ProductoServicio productoServicio,
                                ClienteServicio clienteServicio) {
        this.ventaServicio = ventaServicio;
        this.productoServicio = productoServicio;
        this.clienteServicio = clienteServicio;
    }

    /**
     * Busca productos con stock disponible
     */
    @GetMapping("/productos")
    public List<Producto> obtenerProductosConStock() {
        return productoServicio.listar().stream()
                .filter(producto -> producto.getStock() > 0)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene datos de un producto por ID
     */
    @GetMapping("/productos/{id}")
    public ResponseEntity<Producto> obtenerProductoPorId(@PathVariable Long id) {
        try {
            Producto producto = productoServicio.buscarPorId(id);
            return ResponseEntity.ok(producto);
        } catch (RecursoNoEncontradoException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene datos de un cliente por ID
     */
    @GetMapping("/clientes/{id}")
    public ResponseEntity<Cliente> obtenerClientePorId(@PathVariable Long id) {
        try {
            Cliente cliente = clienteServicio.buscarPorId(id);
            return ResponseEntity.ok(cliente);
        } catch (RecursoNoEncontradoException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Crea una nueva venta
     */
    @PostMapping
    public ResponseEntity<Object> crearVenta(@Valid @RequestBody VentaDTO ventaDTO) {
        try {
            Venta venta = ventaServicio.guardar(ventaDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(ventaServicio.convertirADTO(venta));
        } catch (StockInsuficienteException | RecursoNoEncontradoException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_CREAR + e.getMessage());
        }
    }

    /**
     * Obtiene todas las ventas
     */
    @GetMapping
    public List<VentaDTO> listarVentas() {
        List<Venta> ventas = ventaServicio.listarTodas();
        return ventas.stream()
                .map(ventaServicio::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una venta por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Object> obtenerVentaPorId(@PathVariable Long id) {
        try {
            Venta venta = ventaServicio.buscarPorId(id);
            return ResponseEntity.ok(ventaServicio.convertirADTO(venta));
        } catch (RecursoNoEncontradoException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Actualiza una venta
     */
    @PutMapping("/{id}")
    public ResponseEntity<Object> actualizarVenta(@PathVariable Long id, @Valid @RequestBody VentaDTO ventaDTO) {
        try {
            Venta venta = ventaServicio.actualizar(id, ventaDTO);
            return ResponseEntity.ok(ventaServicio.convertirADTO(venta));
        } catch (StockInsuficienteException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RecursoNoEncontradoException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_ACTUALIZAR + e.getMessage());
        }
    }

    /**
     * Anula una venta
     */
    @PutMapping("/{id}/anular")
    public ResponseEntity<Object> anularVenta(@PathVariable Long id) {
        try {
            Venta venta = ventaServicio.anular(id);
            return ResponseEntity.ok(ventaServicio.convertirADTO(venta));
        } catch (RecursoNoEncontradoException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_ANULAR + e.getMessage());
        }
    }

    /**
     * Filtra ventas por rango de fechas
     */
    @GetMapping("/filtrar")
    public ResponseEntity<Map<String, Object>> filtrarVentas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        List<Venta> ventasFiltradas = ventaServicio.buscarPorRangoFechas(fechaInicio, fechaFin);
        List<VentaDTO> ventasDTO = ventasFiltradas.stream()
                .map(ventaServicio::convertirADTO)
                .collect(Collectors.toList());

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("ventas", ventasDTO);
        respuesta.put("fechaInicio", fechaInicio);
        respuesta.put("fechaFin", fechaFin);
        respuesta.put("total", ventasDTO.size());

        return ResponseEntity.ok(respuesta);
    }
}