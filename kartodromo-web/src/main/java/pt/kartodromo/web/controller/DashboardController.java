package pt.kartodromo.web.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import pt.kartodromo.core.bll.CategoriaKartService;
import pt.kartodromo.core.bll.ClienteService;
import pt.kartodromo.core.bll.CorridaService;
import pt.kartodromo.core.bll.KartService;
import pt.kartodromo.core.bll.ReservaService;
import pt.kartodromo.core.model.Corrida;
import pt.kartodromo.core.model.Reserva;
import pt.kartodromo.web.auth.WebAuthUser;

@Controller
public class DashboardController {

    private final ClienteService clienteService = new ClienteService();
    private final CategoriaKartService categoriaService = new CategoriaKartService();
    private final KartService kartService = new KartService();
    private final CorridaService corridaService = new CorridaService();
    private final ReservaService reservaService = new ReservaService();

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        WebAuthUser user = (WebAuthUser) session.getAttribute("authUser");

        model.addAttribute("authUser", user);
        model.addAttribute("activePage", "dashboard");

        LocalDate hoje = LocalDate.now();

        List<Reserva> todasReservas = reservaService.listarReservas();
        List<Corrida> todasCorridas = corridaService.listarCorridas();

        List<Reserva> reservasVisiveis = todasReservas;

        if (user != null && user.isCliente()) {
            reservasVisiveis = todasReservas.stream()
                    .filter(r -> pertenceAoUtilizador(r, user))
                    .toList();
        }

        long totalClientes;
        long totalCategorias;
        long totalKarts;
        long totalCorridas;
        long totalReservas;

        if (user != null && user.isCliente()) {
            totalClientes = 1;
            totalCategorias = 0;
            totalKarts = 0;
            totalCorridas = 0;
            totalReservas = reservasVisiveis.size();
        } else {
            totalClientes = clienteService.listarClientes().size();
            totalCategorias = categoriaService.listarCategorias().size();
            totalKarts = kartService.listarKarts().size();
            totalCorridas = todasCorridas.size();
            totalReservas = todasReservas.size();
        }

        long reservasPendentes = reservasVisiveis.stream()
                .filter(r -> r.getEstado() != null)
                .filter(r -> "PENDENTE".equalsIgnoreCase(r.getEstado().name()))
                .count();

        long corridasHoje = todasCorridas.stream()
                .filter(c -> c.getDataHoraInicio() != null)
                .filter(c -> c.getDataHoraInicio().toLocalDate().equals(hoje))
                .count();

        if (user != null && user.isCliente()) {
            corridasHoje = 0;
        }

        long reservasHoje = reservasVisiveis.stream()
                .filter(r -> r.getDataHoraInicio() != null)
                .filter(r -> r.getDataHoraInicio().toLocalDate().equals(hoje))
                .count();

        long confirmadasHoje = reservasVisiveis.stream()
                .filter(r -> r.getEstado() != null)
                .filter(r -> "CONFIRMADA".equalsIgnoreCase(r.getEstado().name()))
                .filter(r -> r.getDataHoraInicio() != null)
                .filter(r -> r.getDataHoraInicio().toLocalDate().equals(hoje))
                .count();

        long canceladasHoje = reservasVisiveis.stream()
                .filter(r -> r.getEstado() != null)
                .filter(r -> "CANCELADA".equalsIgnoreCase(r.getEstado().name()))
                .filter(r -> r.getDataHoraInicio() != null)
                .filter(r -> r.getDataHoraInicio().toLocalDate().equals(hoje))
                .count();

        String dataFormatada = hoje.format(
                DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy")
        );

        model.addAttribute("dataFormatada", dataFormatada);

        model.addAttribute("totalClientes", totalClientes);
        model.addAttribute("totalCategorias", totalCategorias);
        model.addAttribute("totalKarts", totalKarts);
        model.addAttribute("totalCorridas", totalCorridas);
        model.addAttribute("totalReservas", totalReservas);
        model.addAttribute("reservasPendentes", reservasPendentes);

        model.addAttribute("corridasHoje", corridasHoje);
        model.addAttribute("reservasHoje", reservasHoje);
        model.addAttribute("confirmadasHoje", confirmadasHoje);
        model.addAttribute("canceladasHoje", canceladasHoje);

        model.addAttribute("clienteView", user != null && user.isCliente());

        return "dashboard";
    }

    private boolean pertenceAoUtilizador(Reserva reserva, WebAuthUser user) {
        if (reserva == null || reserva.getCliente() == null || user == null) return false;

        String username = normalizar(user.getUsername());
        String email = normalizar(user.getEmail());

        String nomeCliente = normalizar(reserva.getCliente().getNome());
        String emailCliente = normalizar(reserva.getCliente().getEmail());

        return nomeCliente.equals(username)
                || emailCliente.equals(email)
                || emailCliente.equals(username)
                || nomeCliente.equals(email);
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase();
    }
}