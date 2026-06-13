package pt.kartodromo.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pt.kartodromo.core.bll.CorridaService;
import pt.kartodromo.core.bll.ReservaService;
import pt.kartodromo.core.model.Corrida;
import pt.kartodromo.core.model.Reserva;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/calendario")
public class CalendarioController {

    private final ReservaService reservaService = new ReservaService();
    private final CorridaService corridaService = new CorridaService();

    @GetMapping
    public String ver(@RequestParam(value = "data", required = false) String dataStr,
                      HttpSession session, Model model) {
        LocalDate data = (dataStr != null && !dataStr.isBlank()) ? LocalDate.parse(dataStr) : LocalDate.now();

        List<Reserva> reservasDia = reservaService.listarReservas().stream()
                .filter(r -> r.getDataHoraInicio().toLocalDate().equals(data))
                .sorted((a, b) -> a.getDataHoraInicio().compareTo(b.getDataHoraInicio()))
                .collect(Collectors.toList());

        List<Corrida> corridasDia = corridaService.listarCorridasPorDia(data).stream()
                .sorted((a, b) -> a.getDataHoraInicio().compareTo(b.getDataHoraInicio()))
                .collect(Collectors.toList());

        model.addAttribute("authUser",    session.getAttribute("authUser"));
        model.addAttribute("activePage",  "calendario");
        model.addAttribute("data",        data);
        model.addAttribute("anterior",    data.minusDays(1));
        model.addAttribute("proximo",     data.plusDays(1));
        model.addAttribute("reservas",    reservasDia);
        model.addAttribute("corridas",    corridasDia);
        return "calendario";
    }
}
