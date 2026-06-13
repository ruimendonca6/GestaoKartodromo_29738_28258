package pt.kartodromo.web.config;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status    = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message   = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object uri       = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        StringBuilder sb = new StringBuilder();
        sb.append("URL: ").append(uri).append("\n");
        sb.append("Status: ").append(status).append("\n");
        sb.append("Mensagem: ").append(message).append("\n");
        if (exception instanceof Throwable t) {
            sb.append("Exceção: ").append(t.getClass().getName()).append(": ").append(t.getMessage()).append("\n");
            if (t.getCause() != null) {
                sb.append("Causa: ").append(t.getCause().getClass().getName()).append(": ").append(t.getCause().getMessage());
            }
        }

        model.addAttribute("errorMessage", sb.toString());
        model.addAttribute("stackTrace", exception instanceof Throwable t ? stackTrace(t) : "");
        return "error-debug";
    }

    private String stackTrace(Throwable t) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement el : t.getStackTrace()) {
            sb.append(el).append("\n");
            if (sb.length() > 3000) { sb.append("..."); break; }
        }
        return sb.toString();
    }
}
