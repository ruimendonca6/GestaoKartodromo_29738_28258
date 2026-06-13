package pt.kartodromo.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pt.kartodromo.web.auth.WebAuthService;
import pt.kartodromo.web.auth.WebAuthUser;

import java.util.Optional;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    private final WebAuthService service = new WebAuthService();

    @GetMapping
    public String ver(HttpSession session, Model model) {
        model.addAttribute("authUser",   session.getAttribute("authUser"));
        model.addAttribute("activePage", "perfil");
        return "perfil";
    }

    @PostMapping("/alterarPassword")
    public String alterarPassword(@RequestParam("passwordAtual") String passwordAtual,
                                  @RequestParam("novaPassword") String novaPassword,
                                  @RequestParam("confirmarPassword") String confirmarPassword,
                                  HttpSession session, RedirectAttributes ra) {
        WebAuthUser auth = (WebAuthUser) session.getAttribute("authUser");
        if (auth == null) return "redirect:/login";
        try {
            if (!novaPassword.equals(confirmarPassword)) throw new RuntimeException("As passwords não coincidem.");
            if (novaPassword.length() < 4) throw new RuntimeException("Password deve ter pelo menos 4 caracteres.");
            Optional<WebAuthUser> check = service.login(auth.getUsername(), passwordAtual);
            if (check.isEmpty()) throw new RuntimeException("Password atual incorreta.");
            service.alterarPassword(auth.getUsername(), novaPassword);
            ra.addFlashAttribute("sucesso", "Password alterada com sucesso.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/perfil";
    }

    @PostMapping("/atualizarEmail")
    public String atualizarEmail(@RequestParam("email") String email,
                                 HttpSession session, RedirectAttributes ra) {
        WebAuthUser auth = (WebAuthUser) session.getAttribute("authUser");
        if (auth == null) return "redirect:/login";
        try {
            service.atualizar(auth.getUsername(), email, auth.getRole());
            WebAuthUser updated = new WebAuthUser(auth.getUsername(), email, auth.getRole());
            session.setAttribute("authUser", updated);
            ra.addFlashAttribute("sucesso", "Email atualizado.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/perfil";
    }
}
