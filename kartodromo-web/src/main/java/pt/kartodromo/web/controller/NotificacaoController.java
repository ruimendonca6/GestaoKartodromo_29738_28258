package pt.kartodromo.web.controller;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import pt.kartodromo.core.bll.NotificacaoService;
import pt.kartodromo.core.bll.ReservaService;
import pt.kartodromo.core.model.Reserva;
import pt.kartodromo.web.auth.WebAuthUser;

@Controller
@RequestMapping("/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService = new NotificacaoService();
    private final ReservaService reservaService = new ReservaService();

    @GetMapping
    public String listar(HttpSession session, Model model) {
        WebAuthUser user = (WebAuthUser) session.getAttribute("authUser");

        model.addAttribute("authUser", user);
        model.addAttribute("activePage", "notificacoes");

        if (user != null && user.isCliente()) {
            model.addAttribute("notificacoesCliente", notificacoesDoCliente(user));
            model.addAttribute("clienteView", true);
        } else {
            model.addAttribute("notificacoes", notificacaoService.listarTodas());
            model.addAttribute("clienteView", false);
        }

        return "notificacoes";
    }

    private List<NotificacaoClienteView> notificacoesDoCliente(WebAuthUser user) {
        List<NotificacaoClienteView> lista = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        List<Reserva> reservas = reservaService.listarReservas().stream()
                .filter(r -> pertenceAoUtilizador(r, user))
                .toList();

        for (Reserva r : reservas) {
            if (r.getEstado() == null) continue;

            String estado = r.getEstado().name();
            String data = r.getDataHoraInicio() != null
                    ? r.getDataHoraInicio().format(fmt)
                    : "sem data";

            if ("CONFIRMADA".equalsIgnoreCase(estado)) {
                lista.add(new NotificacaoClienteView(
                        "Reserva confirmada",
                        "A sua reserva na pista " + r.getPistaNome() + " está confirmada para " + data + ".",
                        "CONFIRMADA"
                ));
            }

            if ("PENDENTE".equalsIgnoreCase(estado)) {
                lista.add(new NotificacaoClienteView(
                        "Reserva pendente",
                        "A sua reserva na pista " + r.getPistaNome() + " encontra-se a aguardar confirmação.",
                        "PENDENTE"
                ));
            }

            if ("CANCELADA".equalsIgnoreCase(estado)) {
                lista.add(new NotificacaoClienteView(
                        "Reserva cancelada",
                        "A sua reserva na pista " + r.getPistaNome() + " foi cancelada.",
                        "CANCELADA"
                ));
            }
        }

        return lista;
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

    public static class NotificacaoClienteView {
        private final String titulo;
        private final String descricao;
        private final String tipo;

        public NotificacaoClienteView(String titulo, String descricao, String tipo) {
            this.titulo = titulo;
            this.descricao = descricao;
            this.tipo = tipo;
        }

        public String getTitulo() {
            return titulo;
        }

        public String getDescricao() {
            return descricao;
        }

        public String getTipo() {
            return tipo;
        }
    }
}