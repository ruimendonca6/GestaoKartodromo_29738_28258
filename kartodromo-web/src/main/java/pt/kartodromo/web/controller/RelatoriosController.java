package pt.kartodromo.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pt.kartodromo.core.bll.*;
import pt.kartodromo.web.auth.WebAuthUser;

@Controller
@RequestMapping("/relatorios")
public class RelatoriosController {

    private final ClienteService    clienteService    = new ClienteService();
    private final KartService       kartService       = new KartService();
    private final ReservaService    reservaService    = new ReservaService();
    private final CorridaService    corridaService    = new CorridaService();
    private final ManutencaoService manutencaoService = new ManutencaoService();
    private final ResultadoService  resultadoService  = new ResultadoService();

    @GetMapping
    public String ver(HttpSession session, Model model) {
        WebAuthUser auth = (WebAuthUser) session.getAttribute("authUser");
        if (auth == null || !auth.isAdmin()) return "redirect:/dashboard";
        model.addAttribute("authUser",    auth);
        model.addAttribute("activePage",  "relatorios");
        model.addAttribute("clientes",    clienteService.listarClientes());
        model.addAttribute("karts",       kartService.listarKarts());
        model.addAttribute("reservas",    reservaService.listarReservas());
        model.addAttribute("corridas",    corridaService.listarCorridas());
        model.addAttribute("manutencoes", manutencaoService.listarTodas());
        model.addAttribute("resultados",  resultadoService.listarTodos());
        return "relatorios";
    }
}
