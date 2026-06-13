package pt.kartodromo.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pt.kartodromo.web.auth.WebAuthService;
import pt.kartodromo.web.auth.WebAuthUser;

import java.util.Optional;

@Controller
public class LoginController {

    private final WebAuthService authService = new WebAuthService();

    @GetMapping("/")
    public String root(HttpSession session) {
        if (session.getAttribute("authUser") != null) return "redirect:/dashboard";
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("authUser") != null) return "redirect:/dashboard";
        return "login";
    }

    @PostMapping("/login/submit")
    public String loginSubmit(@RequestParam("username") String username,
                              @RequestParam("password") String password,
                              HttpSession session,
                              Model model) {
        try {
            Optional<WebAuthUser> result = authService.login(username, password);
            if (result.isEmpty()) {
                model.addAttribute("erro", "Utilizador ou password incorretos.");
                return "login";
            }
            session.setAttribute("authUser", result.get());
            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("erro", "Erro interno: " + e.getClass().getSimpleName() + " — " + e.getMessage());
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
