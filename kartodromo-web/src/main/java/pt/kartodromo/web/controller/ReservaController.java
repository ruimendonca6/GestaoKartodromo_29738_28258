package pt.kartodromo.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pt.kartodromo.core.bll.ClienteService;
import pt.kartodromo.core.bll.KartService;
import pt.kartodromo.core.bll.ReservaService;
import pt.kartodromo.core.model.enums.ReservaEstado;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/reservas")
public class ReservaController {

    private final ReservaService  service        = new ReservaService();
    private final ClienteService  clienteService = new ClienteService();
    private final KartService     kartService    = new KartService();

    @GetMapping
    public String listar(HttpSession session, Model model) {
        model.addAttribute("authUser",   session.getAttribute("authUser"));
        model.addAttribute("activePage", "reservas");
        model.addAttribute("reservas",   service.listarReservas());
        model.addAttribute("clientes",   clienteService.listarClientes());
        model.addAttribute("karts",      kartService.listarKarts());
        model.addAttribute("estados",    ReservaEstado.values());
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
                    ? ReservaEstado.valueOf(estado) : ReservaEstado.PENDENTE;
            service.criarReserva(clienteId, kartId, pistaNome,
                    LocalDateTime.parse(dataHoraInicio),
                    LocalDateTime.parse(dataHoraFim), est);
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
            service.atualizarReserva(id, clienteId, kartId, pistaNome,
                    LocalDateTime.parse(dataHoraInicio),
                    LocalDateTime.parse(dataHoraFim),
                    ReservaEstado.valueOf(estado));
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
}
