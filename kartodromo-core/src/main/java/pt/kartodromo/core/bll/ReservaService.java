package pt.kartodromo.core.bll;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import pt.kartodromo.core.dal.ClienteDao;
import pt.kartodromo.core.dal.KartDao;
import pt.kartodromo.core.dal.ReservaDao;
import pt.kartodromo.core.model.Cliente;
import pt.kartodromo.core.model.Kart;
import pt.kartodromo.core.model.Reserva;
import pt.kartodromo.core.model.enums.ReservaEstado;

public class ReservaService {

    private static final LocalTime HORA_ABERTURA = LocalTime.of(9, 0);
    private static final LocalTime HORA_FECHO = LocalTime.of(22, 0);

    private final ClienteDao clienteDao;
    private final KartDao kartDao;
    private final ReservaDao reservaDao;

    public ReservaService() {
        this(new ClienteDao(), new KartDao(), new ReservaDao());
    }

    public ReservaService(
            ClienteDao clienteDao,
            KartDao kartDao,
            ReservaDao reservaDao
    ) {
        this.clienteDao = clienteDao;
        this.kartDao = kartDao;
        this.reservaDao = reservaDao;
    }

    public Reserva criarReserva(
            Long clienteId,
            Long kartId,
            String pistaNome,
            LocalDateTime dataHoraInicio,
            LocalDateTime dataHoraFim,
            ReservaEstado estado
    ) {
        ReservaEstado estadoFinal = estado == null ? ReservaEstado.PENDENTE : estado;

        Cliente cliente = obterCliente(clienteId);
        Kart kart = obterKart(kartId);
        String pistaNormalizada = validarDadosReserva(kart, pistaNome, dataHoraInicio, dataHoraFim);
        validarDisponibilidade(kart.getId(), pistaNormalizada, dataHoraInicio, dataHoraFim, null, estadoFinal);

        Reserva reserva = new Reserva(
                cliente,
                kart,
                pistaNormalizada,
                dataHoraInicio,
                dataHoraFim,
                LocalDateTime.now(),
                estadoFinal
        );

        return reservaDao.save(reserva);
    }

    public Reserva atualizarReserva(
            Long reservaId,
            Long clienteId,
            Long kartId,
            String pistaNome,
            LocalDateTime dataHoraInicio,
            LocalDateTime dataHoraFim,
            ReservaEstado estado
    ) {
        if (estado == null) {
            throw new BusinessException("Estado da reserva e obrigatorio.");
        }

        Reserva reserva = obterReservaPorId(reservaId);
        Cliente cliente = obterCliente(clienteId);
        Kart kart = obterKart(kartId);
        String pistaNormalizada = validarDadosReserva(kart, pistaNome, dataHoraInicio, dataHoraFim);
        validarDisponibilidade(kart.getId(), pistaNormalizada, dataHoraInicio, dataHoraFim, reservaId, estado);

        reserva.setCliente(cliente);
        reserva.setKart(kart);
        reserva.setPistaNome(pistaNormalizada);
        reserva.setDataHoraInicio(dataHoraInicio);
        reserva.setDataHoraFim(dataHoraFim);
        reserva.setEstado(estado);

        return reservaDao.update(reserva);
    }

    public void eliminarReserva(Long reservaId) {
        if (reservaId == null) {
            throw new BusinessException("Reserva invalida.");
        }
        if (reservaDao.findById(reservaId).isEmpty()) {
            throw new BusinessException("Reserva nao encontrada: " + reservaId);
        }
        reservaDao.deleteById(reservaId);
    }

    public Reserva obterReservaPorId(Long reservaId) {
        return reservaDao.findById(reservaId)
                .orElseThrow(() -> new BusinessException("Reserva nao encontrada: " + reservaId));
    }

    public List<Reserva> listarReservas() {
        return reservaDao.findAllWithDetails();
    }

    private Cliente obterCliente(Long clienteId) {
        return clienteDao.findById(clienteId)
                .orElseThrow(() -> new BusinessException("Cliente nao encontrado: " + clienteId));
    }

    private Kart obterKart(Long kartId) {
        return kartDao.findById(kartId)
                .orElseThrow(() -> new BusinessException("Kart nao encontrado: " + kartId));
    }

    private String validarDadosReserva(
            Kart kart,
            String pistaNome,
            LocalDateTime dataHoraInicio,
            LocalDateTime dataHoraFim
    ) {
        if (!kart.isDisponivel()) {
            throw new BusinessException("Kart selecionado nao esta disponivel para reserva.");
        }

        if (pistaNome == null || pistaNome.isBlank()) {
            throw new BusinessException("Pista e obrigatoria.");
        }

        if (dataHoraInicio == null || dataHoraFim == null) {
            throw new BusinessException("Data/hora de inicio e fim sao obrigatorias.");
        }

        if (!dataHoraFim.isAfter(dataHoraInicio)) {
            throw new BusinessException("Data/hora de fim deve ser posterior ao inicio.");
        }

        if (dataHoraInicio.isBefore(LocalDateTime.now())) {
            throw new BusinessException("Reserva deve ser criada para um horario futuro.");
        }

        if (!dataHoraInicio.toLocalDate().equals(dataHoraFim.toLocalDate())) {
            throw new BusinessException("Reserva deve iniciar e terminar no mesmo dia.");
        }

        if (dataHoraInicio.toLocalTime().isBefore(HORA_ABERTURA)
                || dataHoraFim.toLocalTime().isAfter(HORA_FECHO)) {
            throw new BusinessException(
                    "Horario invalido. Funcionamento do kartodromo: "
                    + HORA_ABERTURA + " - " + HORA_FECHO + "."
            );
        }

        return pistaNome.trim();
    }

    private void validarDisponibilidade(
            Long kartId,
            String pistaNome,
            LocalDateTime dataHoraInicio,
            LocalDateTime dataHoraFim,
            Long reservaIgnorarId,
            ReservaEstado estado
    ) {
        if (estado == ReservaEstado.CANCELADA) {
            return;
        }

        if (reservaDao.existsOverlappingByKart(
                kartId,
                dataHoraInicio,
                dataHoraFim,
                reservaIgnorarId
        )) {
            throw new BusinessException("Kart indisponivel no periodo pretendido.");
        }

        if (reservaDao.existsOverlappingByPista(
                pistaNome.toLowerCase(),
                dataHoraInicio,
                dataHoraFim,
                reservaIgnorarId
        )) {
            throw new BusinessException("Pista indisponivel no periodo pretendido.");
        }
    }
}
