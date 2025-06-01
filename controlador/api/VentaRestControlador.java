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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @GetMapping("/productos")
    public List<Producto> obtenerProductosConStock() {
        return productoServicio.listar().stream()
                .filter(producto -> producto.getStock() > 0)
                .collect(Collectors.toList());
    }

    @GetMapping("/productos/{id}")
    public ResponseEntity<Producto> obtenerProductoPorId(@PathVariable Long id) {
        try {
            Producto producto = productoServicio.buscarPorId(id);
            return ResponseEntity.ok(producto);
        } catch (RecursoNoEncontradoException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/clientes/{id}")
    public ResponseEntity<Cliente> obtenerClientePorId(@PathVariable Long id) {
        try {
            Cliente cliente = clienteServicio.buscarPorId(id);
            return ResponseEntity.ok(cliente);
        } catch (RecursoNoEncontradoException e) {
            return ResponseEntity.notFound().build();
        }
    }

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

    @GetMapping
    public List<VentaDTO> listarVentas() {
        List<Venta> ventas = ventaServicio.listarTodas();
        return ventas.stream()
                .map(ventaServicio::convertirADTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> obtenerVentaPorId(@PathVariable Long id) {
        try {
            Venta venta = ventaServicio.buscarPorId(id);
            return ResponseEntity.ok(ventaServicio.convertirADTO(venta));
        } catch (RecursoNoEncontradoException e) {
            return ResponseEntity.notFound().build();
        }
    }

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

    @GetMapping("/filtrar")
    public ResponseEntity<Map<String, Object>> filtrarVentas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        // Conversión a LocalDateTime para compatibilidad con VentaServicio
        LocalDateTime fechaInicioDT = fechaInicio.atStartOfDay();
        LocalDateTime fechaFinDT = fechaFin.atTime(LocalTime.MAX);

        List<Venta> ventasFiltradas = ventaServicio.buscarPorRangoFechas(fechaInicioDT, fechaFinDT);
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