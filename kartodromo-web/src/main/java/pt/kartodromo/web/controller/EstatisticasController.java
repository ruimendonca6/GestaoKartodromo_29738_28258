package pt.kartodromo.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pt.kartodromo.core.bll.CorridaService;
import pt.kartodromo.core.bll.ReservaService;
import pt.kartodromo.core.model.Corrida;
import pt.kartodromo.core.model.Reserva;
import pt.kartodromo.core.model.enums.ReservaEstado;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/estatisticas")
public class EstatisticasController {

    private final ReservaService reservaService = new ReservaService();
    private final CorridaService corridaService = new CorridaService();

    @GetMapping
    public String ver(@RequestParam(value = "periodo", required = false, defaultValue = "semana") String periodo,
                      HttpSession session, Model model) {
        LocalDate hoje = LocalDate.now();
        LocalDate inicio, fim;

        switch (periodo) {
            case "mes" -> {
                inicio = hoje.with(TemporalAdjusters.firstDayOfMonth());
                fim    = hoje.with(TemporalAdjusters.lastDayOfMonth());
            }
            case "ano" -> {
                inicio = hoje.with(TemporalAdjusters.firstDayOfYear());
                fim    = hoje.with(TemporalAdjusters.lastDayOfYear());
            }
            default -> {
                inicio = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                fim    = inicio.plusDays(6);
            }
        }

        List<Reserva> reservas = reservaService.listarReservas().stream()
                .filter(r -> {
                    LocalDate d = r.getDataHoraInicio().toLocalDate();
                    return !d.isBefore(inicio) && !d.isAfter(fim);
                }).collect(Collectors.toList());

        List<Corrida> corridas = corridaService.listarCorridas().stream()
                .filter(c -> {
                    LocalDate d = c.getDataHoraInicio().toLocalDate();
                    return !d.isBefore(inicio) && !d.isAfter(fim);
                }).collect(Collectors.toList());

        long totalReservas     = reservas.size();
        long confirmadas       = reservas.stream().filter(r -> r.getEstado() == ReservaEstado.CONFIRMADA).count();
        long canceladas        = reservas.stream().filter(r -> r.getEstado() == ReservaEstado.CANCELADA).count();
        long pendentes         = reservas.stream().filter(r -> r.getEstado() == ReservaEstado.PENDENTE).count();
        long totalCorridas     = corridas.size();
        BigDecimal receita     = reservas.stream()
                .filter(r -> r.getEstado() == ReservaEstado.CONFIRMADA)
                .map(r -> r.getKart().getCategoria().getPrecoBase())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("authUser",       session.getAttribute("authUser"));
        model.addAttribute("activePage",     "estatisticas");
        model.addAttribute("periodo",        periodo);
        model.addAttribute("inicio",         inicio);
        model.addAttribute("fim",            fim);
        model.addAttribute("totalReservas",  totalReservas);
        model.addAttribute("confirmadas",    confirmadas);
        model.addAttribute("canceladas",     canceladas);
        model.addAttribute("pendentes",      pendentes);
        model.addAttribute("totalCorridas",  totalCorridas);
        model.addAttribute("receita",        receita);
        model.addAttribute("reservas",       reservas);
        return "estatisticas";
    }
}
