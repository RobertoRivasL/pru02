package informviva.gest.controlador;


import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorControlador implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            if (statusCode == HttpServletResponse.SC_NOT_FOUND) {
                return "default"; // Página personalizada para 404
            } else if (statusCode == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
                return "error/500"; // Página personalizada para 500
            }
        }
        return "error/default"; // Página genérica
    }
}
