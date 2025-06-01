package informviva.gest.service.impl;

import informviva.gest.dto.ClienteReporteDTO;
import informviva.gest.model.Cliente;
import informviva.gest.repository.ClienteRepositorio;
import informviva.gest.repository.VentaRepositorio;
import informviva.gest.service.ClienteServicio;
import informviva.gest.validador.ValidadorRutUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio para la gestión de clientes
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Service
public class ClienteServicioImpl implements ClienteServicio {

    @Autowired
    private ClienteRepositorio clienteRepositorio;

    @Autowired
    private VentaRepositorio ventaRepositorio;

    // Métodos básicos CRUD
    @Override
    public List<Cliente> obtenerTodos() {
        return clienteRepositorio.findAll();
    }

    @Override
    public Cliente buscarPorId(Long id) {
        return clienteRepositorio.findById(id).orElse(null);
    }

    @Override
    public Cliente guardar(Cliente cliente) {
        return clienteRepositorio.save(cliente);
    }

    @Override
    public void eliminar(Long id) {
        clienteRepositorio.deleteById(id);
    }

    @Override
    public boolean rutEsValido(String rut) {
        return ValidadorRutUtil.validar(rut);
    }

    // Métodos para reportes
    @Override
    public List<ClienteReporteDTO> obtenerClientesConCompras(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Cliente> todosLosClientes = clienteRepositorio.findAll();

        return todosLosClientes.stream()
                .map(cliente -> {
                    ClienteReporteDTO dto = new ClienteReporteDTO();
                    dto.setId(cliente.getId());
                    dto.setRut(cliente.getRut());
                    dto.setNombreCompleto(cliente.getNombreCompleto());
                    dto.setEmail(cliente.getEmail());
                    dto.setFechaRegistro(cliente.getFechaRegistro());

                    // Calcular estadísticas de compras
                    var ventasCliente = ventaRepositorio.findByCliente(cliente);

                    // Filtrar por fechas si se proporcionan
                    if (fechaInicio != null && fechaFin != null) {
                        ventasCliente = ventasCliente.stream()
                                .filter(venta -> {
                                    LocalDate fechaVenta = venta.getFechaAsLocalDate();
                                    return fechaVenta != null &&
                                            !fechaVenta.isBefore(fechaInicio) &&
                                            !fechaVenta.isAfter(fechaFin);
                                })
                                .collect(Collectors.toList());
                    }

                    dto.setComprasRealizadas(ventasCliente.size());

                    BigDecimal totalCompras = ventasCliente.stream()
                            .filter(venta -> !"ANULADA".equals(venta.getEstado()))
                            .map(venta -> BigDecimal.valueOf(venta.getTotal()))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    dto.setTotalCompras(totalCompras);

                    // Calcular promedio por compra
                    if (dto.getComprasRealizadas() > 0) {
                        dto.setPromedioPorCompra(totalCompras.divide(
                                BigDecimal.valueOf(dto.getComprasRealizadas()),
                                2, RoundingMode.HALF_UP));
                    } else {
                        dto.setPromedioPorCompra(BigDecimal.ZERO);
                    }

                    // Encontrar última compra
                    dto.setUltimaCompra(ventasCliente.stream()
                            .map(venta -> venta.getFechaAsLocalDate())
                            .filter(fecha -> fecha != null)
                            .max(LocalDate::compareTo)
                            .orElse(null));

                    return dto;
                })
                .filter(dto -> dto.getComprasRealizadas() > 0) // Solo clientes con compras
                .collect(Collectors.toList());
    }

    @Override
    public List<Cliente> obtenerClientesNuevos(LocalDate fechaInicio, LocalDate fechaFin) {
        return clienteRepositorio.findAll().stream()
                .filter(cliente -> {
                    LocalDate fechaRegistro = cliente.getFechaRegistro();
                    return fechaRegistro != null &&
                            !fechaRegistro.isBefore(fechaInicio) &&
                            !fechaRegistro.isAfter(fechaFin);
                })
                .collect(Collectors.toList());
    }

    @Override
    public Long contarClientesNuevos(LocalDate fechaInicio, LocalDate fechaFin) {
        return (long) obtenerClientesNuevos(fechaInicio, fechaFin).size();
    }

    @Override
    public List<Cliente> obtenerClientesPorCategoria(String categoria) {
        return clienteRepositorio.findAll().stream()
                .filter(cliente -> categoria.equals(cliente.getCategoria()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ClienteReporteDTO> obtenerTopClientesPorCompras(int limite) {
        return obtenerClientesConCompras(null, null).stream()
                .sorted((c1, c2) -> c2.getTotalCompras().compareTo(c1.getTotalCompras()))
                .limit(limite)
                .collect(Collectors.toList());
    }

    // Métodos de análisis
    @Override
    public Double calcularPromedioComprasPorCliente() {
        List<ClienteReporteDTO> clientesConCompras = obtenerClientesConCompras(null, null);

        if (clientesConCompras.isEmpty()) {
            return 0.0;
        }

        BigDecimal totalCompras = clientesConCompras.stream()
                .map(ClienteReporteDTO::getTotalCompras)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalCompras.divide(BigDecimal.valueOf(clientesConCompras.size()),
                2, RoundingMode.HALF_UP).doubleValue();
    }

    @Override
    public Long contarClientesActivos() {
        // Considerar activos a los clientes que han comprado en los últimos 90 días
        LocalDate hace90Dias = LocalDate.now().minusDays(90);

        return obtenerClientesConCompras(hace90Dias, LocalDate.now()).stream()
                .filter(cliente -> cliente.getUltimaCompra() != null)
                .count();
    }

    @Override
    public List<Cliente> buscarPorNombre(String nombre) {
        return clienteRepositorio.findAll().stream()
                .filter(cliente -> cliente.getNombre().toLowerCase().contains(nombre.toLowerCase()) ||
                        cliente.getApellido().toLowerCase().contains(nombre.toLowerCase()) ||
                        cliente.getNombreCompleto().toLowerCase().contains(nombre.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Cliente> buscarPorEmail(String email) {
        return clienteRepositorio.findAll().stream()
                .filter(cliente -> cliente.getEmail() != null &&
                        cliente.getEmail().toLowerCase().contains(email.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existeClienteConEmail(String email) {
        return clienteRepositorio.existsByEmail(email);
    }

    @Override
    public boolean existeClienteConRut(String rut) {
        return clienteRepositorio.findAll().stream()
                .anyMatch(cliente -> rut.equals(cliente.getRut()));
    }

    @Override
    public Page<Cliente> obtenerTodosPaginados(Pageable pageable) {
        return clienteRepositorio.findAll(pageable);
    }

    @Override
    public Page<Cliente> buscarPorNombreOEmail(String busqueda, Pageable pageable) {
        return clienteRepositorio.findByNombreContainingOrEmailContainingIgnoreCase(
                busqueda, busqueda, pageable);
    }

}