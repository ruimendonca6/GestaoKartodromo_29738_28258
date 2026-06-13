package pt.kartodromo.web.config;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.getClass().getName()).append(": ").append(e.getMessage());
        if (e.getCause() != null) {
            sb.append("\nCausa: ").append(e.getCause().getClass().getName())
              .append(": ").append(e.getCause().getMessage());
        }
        model.addAttribute("errorMessage", sb.toString());
        model.addAttribute("stackTrace", stackTraceToString(e));
        return "error-debug";
    }

    private String stackTraceToString(Throwable t) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement el : t.getStackTrace()) {
            sb.append(el.toString()).append("\n");
            if (sb.length() > 3000) { sb.append("..."); break; }
        }
        return sb.toString();
    }
}
