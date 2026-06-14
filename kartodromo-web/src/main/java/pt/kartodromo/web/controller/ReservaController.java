package pt.kartodromo.web.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import pt.kartodromo.core.bll.ClienteService;
import pt.kartodromo.core.bll.KartService;
import pt.kartodromo.core.bll.ReservaService;
import pt.kartodromo.core.model.Reserva;
import pt.kartodromo.core.model.enums.ReservaEstado;
import pt.kartodromo.web.auth.WebAuthUser;

@Controller
@RequestMapping("/reservas")
public class ReservaController {

    private final ReservaService service = new ReservaService();
    private final ClienteService clienteService = new ClienteService();
    private final KartService kartService = new KartService();

    @GetMapping
    public String listar(HttpSession session, Model model) {
        WebAuthUser user = (WebAuthUser) session.getAttribute("authUser");

        List<Reserva> reservas = service.listarReservas();

        if (user != null && user.isCliente()) {
            reservas = reservas.stream()
                    .filter(r -> pertenceAoUtilizador(r, user))
                    .toList();
        }

        model.addAttribute("authUser", user);
        model.addAttribute("activePage", "reservas");
        model.addAttribute("reservas", reservas);
        model.addAttribute("clientes", clienteService.listarClientes());
        model.addAttribute("karts", kartService.listarKarts());
        model.addAttribute("estados", ReservaEstado.values());

        return "reservas";
    }

    @PostMapping("/criar")
    public String criar(@RequestParam("clienteId") Long clienteId,
                        @RequestParam("kartId") Long kartId,
                        @RequestParam("pistaNome") String pistaNome,
                        @RequestParam("dataHoraInicio") String dataHoraInicio,
                        @RequestParam("dataHoraFim") String dataHoraFim,
                        @RequestParam(value = "estado", required = false) String estado,
                        RedirectAttributes ra) {
        try {
            ReservaEstado est = (estado != null && !estado.isBlank())
                    ? ReservaEstado.valueOf(estado)
                    : ReservaEstado.PENDENTE;

            service.criarReserva(
                    clienteId,
                    kartId,
                    pistaNome,
                    LocalDateTime.parse(dataHoraInicio),
                    LocalDateTime.parse(dataHoraFim),
                    est
            );

            ra.addFlashAttribute("sucesso", "Reserva criada.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/reservas";
    }

    @PostMapping("/atualizar")
    public String atualizar(@RequestParam("id") Long id,
                            @RequestParam("clienteId") Long clienteId,
                            @RequestParam("kartId") Long kartId,
                            @RequestParam("pistaNome") String pistaNome,
                            @RequestParam("dataHoraInicio") String dataHoraInicio,
                            @RequestParam("dataHoraFim") String dataHoraFim,
                            @RequestParam("estado") String estado,
                            RedirectAttributes ra) {
        try {
            service.atualizarReserva(
                    id,
                    clienteId,
                    kartId,
                    pistaNome,
                    LocalDateTime.parse(dataHoraInicio),
                    LocalDateTime.parse(dataHoraFim),
                    ReservaEstado.valueOf(estado)
            );

            ra.addFlashAttribute("sucesso", "Reserva atualizada.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/reservas";
    }

    @PostMapping("/remover")
    public String remover(@RequestParam("id") Long id, RedirectAttributes ra) {
        try {
            service.eliminarReserva(id);
            ra.addFlashAttribute("sucesso", "Reserva eliminada.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/reservas";
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