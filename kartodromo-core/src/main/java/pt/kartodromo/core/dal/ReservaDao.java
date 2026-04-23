package pt.kartodromo.core.dal;

import java.util.List;
import org.hibernate.Session;
import pt.kartodromo.core.model.Reserva;
import pt.kartodromo.core.model.enums.ReservaEstado;

public class ReservaDao extends GenericHibernateDao<Reserva> {

    public ReservaDao() {
        super(Reserva.class);
    }

    public long countReservasAtivasByCorridaId(Long corridaId) {
        try (Session session = getSessionFactory().openSession()) {
            Long total = session.createQuery(
                    "select count(r.id) from Reserva r where r.corrida.id = :corridaId and r.estado = :estado",
                    Long.class
            )
                    .setParameter("corridaId", corridaId)
                    .setParameter("estado", ReservaEstado.ATIVA)
                    .uniqueResult();

            return total == null ? 0L : total;
        }
    }

    public boolean existsReservaAtiva(Long clienteId, Long corridaId) {
        try (Session session = getSessionFactory().openSession()) {
            Long total = session.createQuery(
                    "select count(r.id) from Reserva r where r.cliente.id = :clienteId and r.corrida.id = :corridaId and r.estado = :estado",
                    Long.class
            )
                    .setParameter("clienteId", clienteId)
                    .setParameter("corridaId", corridaId)
                    .setParameter("estado", ReservaEstado.ATIVA)
                    .uniqueResult();

            return total != null && total > 0;
        }
    }

    public List<Reserva> findAtivasByClienteId(Long clienteId) {
        try (Session session = getSessionFactory().openSession()) {
            return session.createQuery(
                    "select r from Reserva r "
                    + "join fetch r.cliente "
                    + "join fetch r.corrida "
                    + "where r.cliente.id = :clienteId and r.estado = :estado "
                    + "order by r.dataReserva desc",
                    Reserva.class
            )
                    .setParameter("clienteId", clienteId)
                    .setParameter("estado", ReservaEstado.ATIVA)
                    .getResultList();
        }
    }

    public List<Reserva> findByCorridaId(Long corridaId) {
        try (Session session = getSessionFactory().openSession()) {
            return session.createQuery(
                    "select r from Reserva r "
                    + "join fetch r.cliente "
                    + "join fetch r.corrida "
                    + "where r.corrida.id = :corridaId "
                    + "order by r.dataReserva",
                    Reserva.class
            )
                    .setParameter("corridaId", corridaId)
                    .getResultList();
        }
    }
}
