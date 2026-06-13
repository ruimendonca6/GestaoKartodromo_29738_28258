package pt.kartodromo.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pt.kartodromo.core.bll.NotificacaoService;

@Controller
@RequestMapping("/notificacoes")
public class NotificacaoController {

    private final NotificacaoService service = new NotificacaoService();

    @GetMapping
    public String listar(HttpSession session, Model model) {
        model.addAttribute("authUser",      session.getAttribute("authUser"));
        model.addAttribute("activePage",    "notificacoes");
        model.addAttribute("notificacoes",  service.listarTodas());
        return "notificacoes";
    }
}
