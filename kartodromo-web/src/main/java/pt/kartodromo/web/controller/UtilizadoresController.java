package pt.kartodromo.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pt.kartodromo.web.auth.WebAuthService;
import pt.kartodromo.web.auth.WebAuthUser;

@Controller
@RequestMapping("/utilizadores")
public class UtilizadoresController {

    private final WebAuthService service = new WebAuthService();

    @GetMapping
    public String listar(HttpSession session, Model model) {
        WebAuthUser auth = (WebAuthUser) session.getAttribute("authUser");
        if (auth == null || !auth.isAdmin()) return "redirect:/dashboard";
        model.addAttribute("authUser",     auth);
        model.addAttribute("activePage",   "utilizadores");
        model.addAttribute("utilizadores", service.listarTodos());
        model.addAttribute("roles",        new String[]{"ADMIN", "FUNCIONARIO", "CLIENTE"});
        return "utilizadores";
    }

    @PostMapping("/criar")
    public String criar(@RequestParam("username") String username,
                        @RequestParam("email") String email,
                        @RequestParam("password") String password,
                        @RequestParam("role") String role,
                        RedirectAttributes ra) {
        try {
            service.criar(username, email, password, role);
            ra.addFlashAttribute("sucesso", "Utilizador criado.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/utilizadores";
    }

    @PostMapping("/atualizar")
    public String atualizar(@RequestParam("username") String username,
                            @RequestParam("email") String email,
                            @RequestParam("role") String role,
                            RedirectAttributes ra) {
        try {
            service.atualizar(username, email, role);
            ra.addFlashAttribute("sucesso", "Utilizador atualizado.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/utilizadores";
    }

    @PostMapping("/remover")
    public String remover(@RequestParam("username") String username, RedirectAttributes ra) {
        try {
            service.remover(username);
            ra.addFlashAttribute("sucesso", "Utilizador removido.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/utilizadores";
    }

    @PostMapping("/alterarPassword")
    public String alterarPassword(@RequestParam("username") String username,
                                  @RequestParam("novaPassword") String novaPassword,
                                  RedirectAttributes ra) {
        try {
            service.alterarPassword(username, novaPassword);
            ra.addFlashAttribute("sucesso", "Password alterada.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/utilizadores";
    }
}
