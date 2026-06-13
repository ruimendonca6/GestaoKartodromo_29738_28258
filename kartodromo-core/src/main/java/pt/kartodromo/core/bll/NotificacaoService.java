package pt.kartodromo.core.bll;

import pt.kartodromo.core.dal.CorridaDao;
import pt.kartodromo.core.dal.KartDao;
import pt.kartodromo.core.dal.ManutencaoDao;
import pt.kartodromo.core.dal.ReservaDao;
import pt.kartodromo.core.model.Corrida;
import pt.kartodromo.core.model.Kart;
import pt.kartodromo.core.model.Manutencao;
import pt.kartodromo.core.model.Reserva;
import pt.kartodromo.core.model.enums.KartEstado;
import pt.kartodromo.core.model.enums.ReservaEstado;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NotificacaoService {

    public enum TipoNotificacao {
        KART_MANUTENCAO,
        RESERVA_PROXIMA,
        CORRIDA_PROXIMA
    }

    public record Notificacao(
            TipoNotificacao tipo,
            String titulo,
            String detalhe,
            String tempo
    ) {}

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private final KartDao kartDao = new KartDao();
    private final ManutencaoDao manutencaoDao = new ManutencaoDao();
    private final ReservaDao reservaDao = new ReservaDao();
    private final CorridaDao corridaDao = new CorridaDao();

    public List<Notificacao> listarTodas() {
        List<Notificacao> lista = new ArrayList<>();
        LocalDateTime agora = LocalDateTime.now();

        // Karts em manutencao
        for (Kart k : kartDao.findAll()) {
            if (k.getEstado() == KartEstado.EM_MANUTENCAO) {
                List<Manutencao> em = manutencaoDao.findByKartId(k.getId());
                String desc = em.isEmpty() ? "" : " - " + em.get(0).getDescricao();
                lista.add(new Notificacao(
                        TipoNotificacao.KART_MANUTENCAO,
                        "Kart #" + k.getNumero() + " em manutencao",
                        k.getCategoria().getDescricao() + desc,
                        ""
                ));
            }
        }

        // Reservas proximas (proximas 2 horas)
        LocalDateTime limite2h = agora.plusHours(2);
        for (Reserva r : reservaDao.findAll()) {
            if (r.getEstado() == ReservaEstado.CONFIRMADA
                    && r.getDataHoraInicio().isAfter(agora)
                    && r.getDataHoraInicio().isBefore(limite2h)) {
                lista.add(new Notificacao(
                        TipoNotificacao.RESERVA_PROXIMA,
                        "Reserva proxima: " + r.getCliente().getNome(),
                        "Kart #" + r.getKart().getNumero() + " | " + r.getPistaNome(),
                        r.getDataHoraInicio().format(FMT)
                ));
            }
        }

        // Corridas proximas (proximos 30 minutos)
        LocalDateTime limite30m = agora.plusMinutes(30);
        for (Corrida c : corridaDao.findAll()) {
            if (c.getDataHoraInicio().isAfter(agora)
                    && c.getDataHoraInicio().isBefore(limite30m)) {
                lista.add(new Notificacao(
                        TipoNotificacao.CORRIDA_PROXIMA,
                        "Corrida a iniciar: " + c.getLayoutNome(),
                        c.getCategoria().getDescricao() + " | " + c.getVagasMaximas() + " vagas",
                        c.getDataHoraInicio().format(FMT)
                ));
            }
        }

        return lista;
    }

    public long contarAtivas() {
        return listarTodas().size();
    }
}
