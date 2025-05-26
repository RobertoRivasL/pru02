package informviva.gest.repository;

/**
 * @author Roberto Rivas
 * @version 2.0
 **/


import informviva.gest.model.VentaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para acceder a las entidades VentaDetalle en la base de datos.
 * Proporciona métodos para consultas específicas relacionadas con detalles de venta.
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Repository
public interface VentaDetalleRepositorio extends JpaRepository<VentaDetalle, Long> {

    /**
     * Busca detalles de venta por ID de venta
     *
     * @param ventaId ID de la venta
     * @return Lista de detalles de venta
     */
    List<VentaDetalle> findByVentaId(Long ventaId);

    /**
     * Verifica si existen detalles para un producto específico
     *
     * @param productoId ID del producto
     * @return true si existen detalles, false en caso contrario
     */
    boolean existsByProductoId(Long productoId);

    /**
     * Elimina todos los detalles de una venta específica
     *
     * @param ventaId ID de la venta
     */
    void deleteByVentaId(Long ventaId);

    /**
     * Cuenta la cantidad total de un producto vendido
     *
     * @param productoId ID del producto
     * @return Cantidad total vendida
     */
    Long countByProductoId(Long productoId);
}