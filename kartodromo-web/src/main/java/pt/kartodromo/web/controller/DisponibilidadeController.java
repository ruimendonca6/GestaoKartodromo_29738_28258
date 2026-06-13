package pt.kartodromo.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pt.kartodromo.core.bll.PistaService;
import pt.kartodromo.core.bll.ReservaService;
import pt.kartodromo.core.model.Reserva;
import pt.kartodromo.core.model.enums.ReservaEstado;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/disponibilidade")
public class DisponibilidadeController {

    private static final LocalTime ABERTURA = LocalTime.of(9, 0);
    private static final LocalTime FECHO    = LocalTime.of(22, 0);

    private final ReservaService reservaService = new ReservaService();
    private final PistaService   pistaService   = new PistaService();

    @GetMapping
    public String ver(@RequestParam(value = "data", required = false) String dataStr,
                      @RequestParam(value = "pista", required = false) String pistaNome,
                      HttpSession session, Model model) {
        LocalDate data = (dataStr != null && !dataStr.isBlank()) ? LocalDate.parse(dataStr) : LocalDate.now();

        List<Reserva> todas = reservaService.listarReservas();
        List<String> pistas = pistaService.listarPistas().stream()
                .map(pt.kartodromo.core.model.Pista::getNome).collect(Collectors.toList());

        String pistaFiltro = (pistaNome != null && !pistaNome.isBlank()) ? pistaNome :
                (pistas.isEmpty() ? null : pistas.get(0));

        List<Reserva> ocupadas = todas.stream()
                .filter(r -> r.getEstado() == ReservaEstado.CONFIRMADA || r.getEstado() == ReservaEstado.PENDENTE)
                .filter(r -> r.getDataHoraInicio().toLocalDate().equals(data))
                .filter(r -> pistaFiltro == null || r.getPistaNome().equals(pistaFiltro))
                .sorted((a, b) -> a.getDataHoraInicio().compareTo(b.getDataHoraInicio()))
                .collect(Collectors.toList());

        model.addAttribute("authUser",    session.getAttribute("authUser"));
        model.addAttribute("activePage",  "disponibilidade");
        model.addAttribute("data",        data);
        model.addAttribute("pistas",      pistas);
        model.addAttribute("pistaFiltro", pistaFiltro);
        model.addAttribute("ocupadas",    ocupadas);
        model.addAttribute("abertura",    ABERTURA);
        model.addAttribute("fecho",       FECHO);
        return "disponibilidade";
    }
}
