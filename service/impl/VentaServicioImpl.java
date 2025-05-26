package informviva.gest.service.impl;

import informviva.gest.dto.VentaDTO;
import informviva.gest.model.Cliente;
import informviva.gest.model.Usuario;
import informviva.gest.model.Venta;
import informviva.gest.repository.VentaRepositorio;
import informviva.gest.service.VentaServicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class VentaServicioImpl implements VentaServicio {

    private static final String ESTADO_ANULADA = "ANULADA";
    private static final double CIEN_PORCIENTO = 100.0;
    private static final double CERO_PORCIENTO = 0.0;

    private final VentaRepositorio ventaRepositorio;

    public VentaServicioImpl(VentaRepositorio ventaRepositorio) {
        this.ventaRepositorio = ventaRepositorio;
    }

    @Override
    public List<Venta> listarTodas() {
        return ventaRepositorio.findAll();
    }

    @Override
    public Page<Venta> listarPaginadas(Pageable pageable) {
        return ventaRepositorio.findAll(pageable);
    }

    @Override
    public Venta buscarPorId(Long id) {
        return ventaRepositorio.findById(id).orElse(null);
    }

    @Override
    public Venta guardar(VentaDTO ventaDTO) {
        Venta venta = convertirAEntidad(ventaDTO);
        return ventaRepositorio.save(venta);
    }

    @Override
    public Venta actualizar(Long id, VentaDTO ventaDTO) {
        Venta venta = buscarPorId(id);
        if (venta != null) {
            // Actualizar campos de la venta
            return ventaRepositorio.save(venta);
        }
        return null;
    }

    @Override
    public void eliminar(Long id) {
        ventaRepositorio.deleteById(id);
    }

    @Override
    public Venta anular(Long id) {
        Venta venta = buscarPorId(id);
        if (venta != null) {
            venta.setEstado(ESTADO_ANULADA);
            return ventaRepositorio.save(venta);
        }
        return null;
    }

    @Override
    public List<Venta> buscarPorRangoFechas(LocalDate inicio, LocalDate fin) {
        return ventaRepositorio.findByFechaBetween(inicio, fin);
    }

    @Override
    public List<Venta> buscarPorCliente(Cliente cliente) {
        return ventaRepositorio.findByCliente(cliente);
    }

    @Override
    public List<Venta> buscarPorVendedor(Usuario vendedor) {
        return ventaRepositorio.findByVendedor(vendedor);
    }

    @Override
    public boolean existenVentasPorCliente(Long clienteId) {
        return ventaRepositorio.existsByClienteId(clienteId);
    }

    @Override
    public boolean existenVentasPorProducto(Long productoId) {
        return ventaRepositorio.existsByDetallesProductoId(productoId);
    }

    @Override
    public VentaDTO convertirADTO(Venta venta) {
        if (venta == null) {
            return null;
        }
        VentaDTO dto = new VentaDTO();
        // Copiar propiedades de venta a dto
        return dto;
    }

    private Venta convertirAEntidad(VentaDTO dto) {
        if (dto == null) {
            return null;
        }
        Venta venta = new Venta();
        // Copiar propiedades de dto a venta
        return venta;
    }

    @Override
    public Double calcularTotalVentas(LocalDate inicio, LocalDate fin) {
        List<Venta> ventas = buscarPorRangoFechas(inicio, fin);
        return ventas.stream()
                .filter(v -> !ESTADO_ANULADA.equals(v.getEstado()))
                .mapToDouble(Venta::getTotal)
                .sum();
    }

    @Override
    public Double calcularTotalVentas() {
        List<Venta> ventas = listarTodas();
        return ventas.stream()
                .filter(v -> !ESTADO_ANULADA.equals(v.getEstado()))
                .mapToDouble(Venta::getTotal)
                .sum();
    }

    @Override
    public Long contarTransacciones(LocalDate inicio, LocalDate fin) {
        return ventaRepositorio.countByFechaBetweenAndEstadoNot(inicio, fin, ESTADO_ANULADA);
    }

    @Override
    public Long contarArticulosVendidos(LocalDate inicio, LocalDate fin) {
        return ventaRepositorio.countArticulosVendidosBetweenFechas(inicio, fin);
    }

    @Override
    public Double calcularTicketPromedio(LocalDate inicio, LocalDate fin) {
        Double totalVentas = calcularTotalVentas(inicio, fin);
        Long totalTransacciones = contarTransacciones(inicio, fin);
        return (totalTransacciones == 0) ? 0.0 : totalVentas / totalTransacciones;
    }

    @Override
    public Double calcularPorcentajeCambio(Double valorActual, Double valorAnterior) {
        if (valorAnterior == null || Math.abs(valorAnterior) < 0.0001) {
            return (valorActual != null && valorActual > 0.0) ? CIEN_PORCIENTO : CERO_PORCIENTO;
        }
        return ((valorActual - valorAnterior) / valorAnterior) * CIEN_PORCIENTO;
    }

    @Override
    public Long contarVentasHoy() {
        LocalDate hoy = LocalDate.now();
        return contarTransacciones(hoy, hoy);
    }

    @Override
    public Long contarVentasPorCliente(Long clienteId) {
        return ventaRepositorio.countByClienteId(clienteId);
    }

    @Override
    public Double calcularTotalVentasPorCliente(Long clienteId) {
        return ventaRepositorio.calcularTotalPorCliente(clienteId);
    }

    @Override
    public Long contarUnidadesVendidasPorProducto(Long productoId) {
        return ventaRepositorio.contarUnidadesVendidasPorProducto(productoId);
    }

    @Override
    public Double calcularIngresosPorProducto(Long productoId) {
        return ventaRepositorio.calcularIngresosPorProducto(productoId);
    }

    @Override
    public List<Venta> buscarVentasRecientesPorProducto(Long productoId, int limite) {
        Pageable pageable = PageRequest.of(0, limite, Sort.by(Sort.Direction.DESC, "fecha"));
        return ventaRepositorio.buscarVentasRecientesPorProducto(productoId, pageable);
    }

    @Override
    public List<Venta> buscarVentasRecientesPorCliente(Long clienteId, int limite) {
        Pageable pageable = PageRequest.of(0, limite, Sort.by(Sort.Direction.DESC, "fecha"));
        return ventaRepositorio.findTopByClienteIdOrderByFechaDesc(clienteId, pageable);
    }
}