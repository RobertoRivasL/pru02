package informviva.gest.service;

import informviva.gest.dto.ClienteReporteDTO;
import informviva.gest.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio para la gestión de clientes
 *
 * @author Roberto Rivas
 * @version 2.0
 */
public interface ClienteServicio {

    // Métodos básicos CRUD
    List<Cliente> obtenerTodos();

    Cliente buscarPorId(Long id);

    Cliente guardar(Cliente cliente);

    void eliminar(Long id);

    boolean rutEsValido(String rut);

    // Métodos para reportes
    List<ClienteReporteDTO> obtenerClientesConCompras(LocalDate fechaInicio, LocalDate fechaFin);

    List<Cliente> obtenerClientesNuevos(LocalDate fechaInicio, LocalDate fechaFin);

    Long contarClientesNuevos(LocalDate fechaInicio, LocalDate fechaFin);

    List<Cliente> obtenerClientesPorCategoria(String categoria);

    List<ClienteReporteDTO> obtenerTopClientesPorCompras(int limite);

    // Métodos de análisis
    Double calcularPromedioComprasPorCliente();

    Long contarClientesActivos();

    List<Cliente> buscarPorNombre(String nombre);

    List<Cliente> buscarPorEmail(String email);

    boolean existeClienteConEmail(String email);

    boolean existeClienteConRut(String rut);

    Page<Cliente> obtenerTodosPaginados(Pageable pageable);

    Page<Cliente> buscarPorNombreOEmail(String busqueda, Pageable pageable);

}