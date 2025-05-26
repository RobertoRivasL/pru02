package informviva.gest.controlador;

import informviva.gest.model.ConfiguracionSistema;
import informviva.gest.service.ConfiguracionServicio;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador para la administración de la configuración del sistema
 */
@Controller
@RequestMapping("/admin/configuracion")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class ConfiguracionControlador {

    private static final Logger logger = LoggerFactory.getLogger(ConfiguracionControlador.class);

    private final ConfiguracionServicio configuracionServicio;

    @Autowired
    public ConfiguracionControlador(ConfiguracionServicio configuracionServicio) {
        this.configuracionServicio = configuracionServicio;
    }

    /**
     * Muestra la página de configuración del sistema
     */
    @GetMapping
    public String mostrarConfiguracion(Model model) {
        ConfiguracionSistema configuracion = configuracionServicio.obtenerConfiguracion();
        model.addAttribute("configuracion", configuracion);
        return "admin/configuracion";
    }

    /**
     * Procesa la actualización de la configuración del sistema
     */
    @PostMapping("/guardar")
    public String guardarConfiguracion(
            @Valid @ModelAttribute("configuracion") ConfiguracionSistema configuracion,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {

        if (result.hasErrors()) {
            return "admin/configuracion";
        }

        try {
            String usuarioActual = authentication.getName();
            configuracionServicio.guardarConfiguracion(configuracion, usuarioActual);

            redirectAttributes.addFlashAttribute("mensajeExito",
                    "La configuración se ha guardado correctamente");
        } catch (Exception e) {
            logger.error("Error al guardar la configuración: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError",
                    "Error al guardar la configuración: " + e.getMessage());
        }

        return "redirect:/admin/configuracion";
    }

    /**
     * Prueba la configuración de correo electrónico
     */
    @PostMapping("/probar-correo")
    @ResponseBody
    public String probarCorreo(@RequestParam String email) {
        try {
            boolean resultado = configuracionServicio.probarConfiguracionCorreo(email);

            if (resultado) {
                return "Correo de prueba enviado correctamente a: " + email;
            } else {
                return "No se pudo enviar el correo de prueba. Por favor, revisa la configuración SMTP.";
            }
        } catch (Exception e) {
            logger.error("Error al probar configuración de correo: {}", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }
}