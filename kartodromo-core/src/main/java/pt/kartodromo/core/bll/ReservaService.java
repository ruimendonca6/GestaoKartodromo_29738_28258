package pt.kartodromo.core.bll;

import java.time.LocalDateTime;
import java.util.List;

import pt.kartodromo.core.dal.ClienteDao;
import pt.kartodromo.core.dal.CorridaDao;
import pt.kartodromo.core.dal.ReservaDao;
import pt.kartodromo.core.model.CategoriaKart;
import pt.kartodromo.core.model.Cliente;
import pt.kartodromo.core.model.Corrida;
import pt.kartodromo.core.model.Reserva;
import pt.kartodromo.core.model.enums.ReservaEstado;

public class ReservaService {

    private final ClienteDao clienteDao;
    private final CorridaDao corridaDao;
    private final ReservaDao reservaDao;

    public ReservaService() {
        this(new ClienteDao(), new CorridaDao(), new ReservaDao());
    }

    public ReservaService(
            ClienteDao clienteDao,
            CorridaDao corridaDao,
            ReservaDao reservaDao
    ) {
        this.clienteDao = clienteDao;
        this.corridaDao = corridaDao;
        this.reservaDao = reservaDao;
    }

    public Reserva reservarCorrida(Long clienteId, Long corridaId) {

        Cliente cliente = clienteDao.findById(clienteId)
                .orElseThrow(() -> new BusinessException(
                "Cliente nao encontrado: " + clienteId
        ));

        Corrida corrida = corridaDao.findById(corridaId)
                .orElseThrow(() -> new BusinessException(
                "Corrida nao encontrada: " + corridaId
        ));

        if (corrida.getDataHoraInicio().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Nao e possivel reservar corrida que ja ocorreu.");
        }

        if (reservaDao.existsReservaAtiva(clienteId, corridaId)) {
            throw new BusinessException(
                    "Cliente ja possui reserva ativa para esta corrida."
            );
        }

        long reservasAtivas = reservaDao.countReservasAtivasByCorridaId(corridaId);

        if (reservasAtivas >= corrida.getVagasMaximas()) {
            throw new BusinessException(
                    "Nao existem vagas disponiveis para a corrida."
            );
        }

        validarElegibilidade(cliente, corrida.getCategoria());

        Reserva reserva = new Reserva(
                cliente,
                corrida,
                LocalDateTime.now(),
                ReservaEstado.ATIVA
        );

        return reservaDao.save(reserva);
    }

    public Reserva cancelarReserva(Long reservaId) {

        Reserva reserva = reservaDao.findById(reservaId)
                .orElseThrow(() -> new BusinessException(
                "Reserva nao encontrada: " + reservaId
        ));

        if (reserva.getEstado() == ReservaEstado.CANCELADA) {
            throw new BusinessException(
                    "A reserva ja se encontra cancelada."
            );
        }

        reserva.setEstado(ReservaEstado.CANCELADA);

        return reservaDao.update(reserva);
    }

    public List<Reserva> listarReservasAtivasCliente(Long clienteId) {
        return reservaDao.findAtivasByClienteId(clienteId);
    }

    private void validarElegibilidade(
            Cliente cliente,
            CategoriaKart categoria
    ) {

        if (cliente.getIdadeAtual() < categoria.getIdadeMinima()) {
            throw new BusinessException(
                    "Cliente nao cumpre idade minima para esta categoria. Minimo: "
                    + categoria.getIdadeMinima()
            );
        }

        if (cliente.getNivelExperiencia() < categoria.getExperienciaMinima()) {
            throw new BusinessException(
                    "Cliente nao cumpre experiencia minima para esta categoria. Minimo: "
                    + categoria.getExperienciaMinima()
            );
        }
    }
}
