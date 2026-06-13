package pt.kartodromo.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pt.kartodromo.core.bll.*;
import pt.kartodromo.core.model.Corrida;
import pt.kartodromo.core.model.Reserva;
import pt.kartodromo.web.auth.WebAuthUser;

import java.time.LocalDate;
import java.util.List;

@Controller
public class DashboardController {

    private final ClienteService       clienteService   = new ClienteService();
    private final CategoriaKartService categoriaService = new CategoriaKartService();
    private final KartService          kartService      = new KartService();
    private final CorridaService       corridaService   = new CorridaService();
    private final ReservaService       reservaService   = new ReservaService();

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        WebAuthUser user = (WebAuthUser) session.getAttribute("authUser");
        model.addAttribute("authUser", user);
        model.addAttribute("activePage", "dashboard");

        List<Reserva> reservas = reservaService.listarReservas();
        List<Corrida> corridas = corridaService.listarCorridas();
        LocalDate hoje = LocalDate.now();

        model.addAttribute("totalClientes",   clienteService.listarClientes().size());
        model.addAttribute("totalCategorias", categoriaService.listarCategorias().size());
        model.addAttribute("totalKarts",      kartService.listarKarts().size());
        model.addAttribute("totalCorridas",   corridas.size());
        model.addAttribute("totalReservas",   reservas.size());

        long pendentes = reservas.stream()
                .filter(r -> "PENDENTE".equals(r.getEstado().name())).count();
        model.addAttribute("reservasPendentes", pendentes);

        long corridasHoje = corridas.stream()
                .filter(c -> c.getDataHoraInicio().toLocalDate().equals(hoje)).count();
        long reservasHoje = reservas.stream()
                .filter(r -> r.getDataHoraInicio().toLocalDate().equals(hoje)).count();
        long confirmadas = reservas.stream()
                .filter(r -> "CONFIRMADA".equals(r.getEstado().name())
                          && r.getDataHoraInicio().toLocalDate().equals(hoje)).count();
        long canceladas = reservas.stream()
                .filter(r -> "CANCELADA".equals(r.getEstado().name())
                          && r.getDataHoraInicio().toLocalDate().equals(hoje)).count();

        model.addAttribute("corridasHoje",    corridasHoje);
        model.addAttribute("reservasHoje",    reservasHoje);
        model.addAttribute("confirmadasHoje", confirmadas);
        model.addAttribute("canceladasHoje",  canceladas);

        return "dashboard";
    }
}
