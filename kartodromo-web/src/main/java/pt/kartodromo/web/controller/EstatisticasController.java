package pt.kartodromo.web.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import pt.kartodromo.core.bll.CorridaService;
import pt.kartodromo.core.bll.PistaService;
import pt.kartodromo.core.bll.ReservaService;
import pt.kartodromo.core.model.Corrida;
import pt.kartodromo.core.model.Pista;
import pt.kartodromo.core.model.Reserva;
import pt.kartodromo.core.model.enums.ReservaEstado;

@Controller
@RequestMapping("/estatisticas")
public class EstatisticasController {

    private final ReservaService reservaService = new ReservaService();
    private final CorridaService corridaService = new CorridaService();
    private final PistaService pistaService = new PistaService();

    @GetMapping
    public String ver(
            @RequestParam(value = "periodo", required = false, defaultValue = "semana") String periodo,
            HttpSession session,
            Model model) {

        LocalDate hoje = LocalDate.now();
        LocalDate inicio;
        LocalDate fim;

        switch (periodo) {
            case "mes" -> {
                inicio = hoje.with(TemporalAdjusters.firstDayOfMonth());
                fim = hoje.with(TemporalAdjusters.lastDayOfMonth());
            }
            case "ano" -> {
                inicio = hoje.with(TemporalAdjusters.firstDayOfYear());
                fim = hoje.with(TemporalAdjusters.lastDayOfYear());
            }
            default -> {
                periodo = "semana";
                inicio = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                fim = inicio.plusDays(6);
            }
        }

        LocalDate inicioFinal = inicio;
        LocalDate fimFinal = fim;

        List<Reserva> reservas = reservaService.listarReservas()
                .stream()
                .filter(r -> r.getDataHoraInicio() != null)
                .filter(r -> {
                    LocalDate data = r.getDataHoraInicio().toLocalDate();
                    return !data.isBefore(inicioFinal) && !data.isAfter(fimFinal);
                })
                .collect(Collectors.toList());

        List<Corrida> corridas = corridaService.listarCorridas()
                .stream()
                .filter(c -> c.getDataHoraInicio() != null)
                .filter(c -> {
                    LocalDate data = c.getDataHoraInicio().toLocalDate();
                    return !data.isBefore(inicioFinal) && !data.isAfter(fimFinal);
                })
                .collect(Collectors.toList());

        List<Pista> pistas = pistaService.listarPistas();

        long totalReservas = reservas.size();

        long confirmadas = reservas.stream()
                .filter(r -> r.getEstado() == ReservaEstado.CONFIRMADA)
                .count();

        long canceladas = reservas.stream()
                .filter(r -> r.getEstado() == ReservaEstado.CANCELADA)
                .count();

        long pendentes = reservas.stream()
                .filter(r -> r.getEstado() == ReservaEstado.PENDENTE)
                .count();

        long totalCorridas = corridas.size();

        BigDecimal receita = reservas.stream()
                .filter(r -> r.getEstado() == ReservaEstado.CONFIRMADA)
                .map(this::valorReserva)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double taxaCancelamento =
                totalReservas == 0
                        ? 0
                        : (canceladas * 100.0) / totalReservas;

        List<ReceitaGrafico> receitaGrafico =
                criarReceitaGrafico(periodo, inicio, fim, reservas);

        List<OcupacaoPista> ocupacaoPistas =
                criarOcupacaoPistas(pistas, reservas, inicio, fim);

        model.addAttribute("authUser", session.getAttribute("authUser"));
        model.addAttribute("activePage", "estatisticas");

        model.addAttribute("periodo", periodo);
        model.addAttribute("inicio", inicio);
        model.addAttribute("fim", fim);

        model.addAttribute("receita", receita);
        model.addAttribute("totalReservas", totalReservas);
        model.addAttribute("confirmadas", confirmadas);
        model.addAttribute("canceladas", canceladas);
        model.addAttribute("pendentes", pendentes);
        model.addAttribute("totalCorridas", totalCorridas);
        model.addAttribute("taxaCancelamento", taxaCancelamento);
        model.addAttribute("totalPistas", pistas.size());

        model.addAttribute("reservas", reservas);
        model.addAttribute("corridas", corridas);
        model.addAttribute("receitaGrafico", receitaGrafico);
        model.addAttribute("ocupacaoPistas", ocupacaoPistas);

        return "estatisticas";
    }

    private BigDecimal valorReserva(Reserva reserva) {
        try {
            if (reserva.getKart() != null
                    && reserva.getKart().getCategoria() != null
                    && reserva.getKart().getCategoria().getPrecoBase() != null) {

                return reserva.getKart().getCategoria().getPrecoBase();
            }
        } catch (Exception ignored) {
        }

        return BigDecimal.ZERO;
    }

    private long minutosReserva(Reserva reserva) {
        try {
            if (reserva.getDataHoraInicio() != null
                    && reserva.getDataHoraFim() != null) {

                return java.time.Duration.between(
                        reserva.getDataHoraInicio(),
                        reserva.getDataHoraFim()
                ).toMinutes();
            }
        } catch (Exception ignored) {
        }

        return 0;
    }

    private List<ReceitaGrafico> criarReceitaGrafico(
            String periodo,
            LocalDate inicio,
            LocalDate fim,
            List<Reserva> reservas) {

        List<ReceitaGrafico> dados = new ArrayList<>();

        if ("ano".equals(periodo)) {
            String[] meses = {
                    "jan", "fev", "mar", "abr", "mai", "jun",
                    "jul", "ago", "set", "out", "nov", "dez"
            };

            for (int i = 1; i <= 12; i++) {
                int mes = i;

                BigDecimal valor = reservas.stream()
                        .filter(r -> r.getEstado() == ReservaEstado.CONFIRMADA)
                        .filter(r -> r.getDataHoraInicio().getMonthValue() == mes)
                        .map(this::valorReserva)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                dados.add(new ReceitaGrafico(meses[i - 1], valor));
            }

            return dados;
        }

        if ("mes".equals(periodo)) {
            LocalDate semanaInicio = inicio;
            int semana = 1;

            while (!semanaInicio.isAfter(fim)) {
                LocalDate semanaFim = semanaInicio.plusDays(6);

                if (semanaFim.isAfter(fim)) {
                    semanaFim = fim;
                }

                LocalDate si = semanaInicio;
                LocalDate sf = semanaFim;

                BigDecimal valor = reservas.stream()
                        .filter(r -> r.getEstado() == ReservaEstado.CONFIRMADA)
                        .filter(r -> {
                            LocalDate data = r.getDataHoraInicio().toLocalDate();
                            return !data.isBefore(si) && !data.isAfter(sf);
                        })
                        .map(this::valorReserva)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                dados.add(new ReceitaGrafico("Sem " + semana, valor));

                semanaInicio = semanaInicio.plusDays(7);
                semana++;
            }

            return dados;
        }

        String[] dias = {
                "segunda", "terça", "quarta", "quinta",
                "sexta", "sábado", "domingo"
        };

        for (int i = 0; i < 7; i++) {
            LocalDate dia = inicio.plusDays(i);

            BigDecimal valor = reservas.stream()
                    .filter(r -> r.getEstado() == ReservaEstado.CONFIRMADA)
                    .filter(r -> r.getDataHoraInicio().toLocalDate().equals(dia))
                    .map(this::valorReserva)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            dados.add(new ReceitaGrafico(dias[i], valor));
        }

        return dados;
    }

    private List<OcupacaoPista> criarOcupacaoPistas(
            List<Pista> pistas,
            List<Reserva> reservas,
            LocalDate inicio,
            LocalDate fim) {

        long diasPeriodo =
                java.time.temporal.ChronoUnit.DAYS.between(inicio, fim) + 1;

        long minutosDisponiveis =
                diasPeriodo * 13 * 60;

        List<OcupacaoPista> resultado = new ArrayList<>();

        for (Pista pista : pistas) {
            List<Reserva> reservasPista = reservas.stream()
                    .filter(r -> r.getPistaNome() != null)
                    .filter(r -> r.getPistaNome().equalsIgnoreCase(pista.getNome()))
                    .filter(r -> r.getEstado() == ReservaEstado.CONFIRMADA)
                    .collect(Collectors.toList());

            long minutosOcupados = reservasPista.stream()
                    .mapToLong(this::minutosReserva)
                    .sum();

            double taxa =
                    minutosDisponiveis == 0
                            ? 0
                            : (minutosOcupados * 100.0) / minutosDisponiveis;

            resultado.add(
                    new OcupacaoPista(
                            pista.getNome(),
                            reservasPista.size(),
                            minutosOcupados,
                            minutosDisponiveis,
                            taxa
                    )
            );
        }

        return resultado;
    }

    public static class ReceitaGrafico {

        private final String label;
        private final BigDecimal valor;
        private final String valorFormatado;
        private final int altura;

        public ReceitaGrafico(String label, BigDecimal valor) {
            this.label = label;
            this.valor = valor == null ? BigDecimal.ZERO : valor;
            this.valorFormatado =
                    this.valor.setScale(0, RoundingMode.HALF_UP) + "€";

            this.altura =
                    this.valor.compareTo(BigDecimal.ZERO) > 0
                            ? 130
                            : 2;
        }

        public String getLabel() {
            return label;
        }

        public BigDecimal getValor() {
            return valor;
        }

        public String getValorFormatado() {
            return valorFormatado;
        }

        public int getAltura() {
            return altura;
        }

        public boolean isTemValor() {
            return valor.compareTo(BigDecimal.ZERO) > 0;
        }
    }

    public static class OcupacaoPista {

        private final String pista;
        private final int reservas;
        private final long minutosOcupados;
        private final long minutosDisponiveis;
        private final double taxa;

        public OcupacaoPista(
                String pista,
                int reservas,
                long minutosOcupados,
                long minutosDisponiveis,
                double taxa) {

            this.pista = pista;
            this.reservas = reservas;
            this.minutosOcupados = minutosOcupados;
            this.minutosDisponiveis = minutosDisponiveis;
            this.taxa = taxa;
        }

        public String getPista() {
            return pista;
        }

        public int getReservas() {
            return reservas;
        }

        public long getMinutosOcupados() {
            return minutosOcupados;
        }

        public long getMinutosDisponiveis() {
            return minutosDisponiveis;
        }

        public double getTaxa() {
            return taxa;
        }
    }
}