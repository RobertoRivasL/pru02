package informviva.gest.service;

/**
 * @author Roberto Rivas
 * @version 2.0
 */


import informviva.gest.model.ConfiguracionSistema;

public interface ConfiguracionServicio {

    /**
     * Obtiene la configuración actual del sistema
     *
     * @return La configuración del sistema
     */
    ConfiguracionSistema obtenerConfiguracion();

    /**
     * Guarda o actualiza la configuración del sistema
     *
     * @param configuracion La configuración a guardar
     * @param usuarioActual Nombre del usuario que realiza la actualización
     * @return La configuración guardada
     */
    ConfiguracionSistema guardarConfiguracion(ConfiguracionSistema configuracion, String usuarioActual);

    /**
     * Envía un correo de prueba para verificar la configuración SMTP
     *
     * @return true si el correo se envió correctamente, false en caso contrario
     */
    boolean probarConfiguracionCorreo(String emailDestino);
}