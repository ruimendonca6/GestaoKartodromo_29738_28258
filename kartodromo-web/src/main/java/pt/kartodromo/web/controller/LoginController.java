package pt.kartodromo.web.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import pt.kartodromo.web.auth.WebAuthService;
import pt.kartodromo.web.auth.WebAuthUser;

@Controller
public class LoginController {

    private final WebAuthService authService = new WebAuthService();

    @GetMapping("/")
    public String root(HttpSession session) {
        if (session.getAttribute("authUser") != null) {
            return "redirect:/dashboard";
        }

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session, Model model) {
        if (session.getAttribute("authUser") != null) {
            return "redirect:/dashboard";
        }

        model.addAttribute("activeTab", "login");
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
                model.addAttribute("activeTab", "login");
                return "login";
            }

            session.setAttribute("authUser", result.get());
            return "redirect:/dashboard";

        } catch (Exception e) {
            model.addAttribute("erro", "Erro interno: " + e.getMessage());
            model.addAttribute("activeTab", "login");
            return "login";
        }
    }

    @PostMapping("/login/register")
    public String registerSubmit(@RequestParam("username") String username,
                                 @RequestParam("email") String email,
                                 @RequestParam("password") String password,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 @RequestParam("role") String role,
                                 HttpSession session,
                                 Model model) {
        try {
            authService.criarContaPublica(username, email, password, confirmPassword, role);

            Optional<WebAuthUser> result = authService.login(username, password);

            if (result.isPresent()) {
                session.setAttribute("authUser", result.get());
                return "redirect:/dashboard";
            }

            model.addAttribute("sucesso", "Conta criada com sucesso. Inicie sessão.");
            model.addAttribute("activeTab", "login");
            return "login";

        } catch (Exception e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("activeTab", "register");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}