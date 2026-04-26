package pt.kartodromo.core.dal;

import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.Session;
import pt.kartodromo.core.model.Reserva;
import pt.kartodromo.core.model.enums.ReservaEstado;

public class ReservaDao extends GenericHibernateDao<Reserva> {

    public ReservaDao() {
        super(Reserva.class);
    }

    public List<Reserva> findAllWithDetails() {
        try (Session session = getSessionFactory().openSession()) {
            return session.createQuery(
                    "select r from Reserva r "
                    + "join fetch r.cliente "
                    + "join fetch r.kart "
                    + "order by r.dataHoraInicio, r.id",
                    Reserva.class
            )
                    .getResultList();
        }
    }

    public boolean existsOverlappingByKart(
            Long kartId,
            LocalDateTime dataHoraInicio,
            LocalDateTime dataHoraFim,
            Long reservaIgnorarId
    ) {
        try (Session session = getSessionFactory().openSession()) {
            Long total = session.createQuery(
                    "select count(r.id) from Reserva r "
                    + "where r.kart.id = :kartId "
                    + "and r.estado <> :estadoCancelada "
                    + "and r.dataHoraInicio < :dataHoraFim "
                    + "and r.dataHoraFim > :dataHoraInicio "
                    + "and (:reservaIgnorarId is null or r.id <> :reservaIgnorarId)",
                    Long.class
            )
                    .setParameter("kartId", kartId)
                    .setParameter("estadoCancelada", ReservaEstado.CANCELADA)
                    .setParameter("dataHoraInicio", dataHoraInicio)
                    .setParameter("dataHoraFim", dataHoraFim)
                    .setParameter("reservaIgnorarId", reservaIgnorarId)
                    .uniqueResult();

            return total != null && total > 0;
        }
    }

    public boolean existsOverlappingByPista(
            String pistaNomeNormalizado,
            LocalDateTime dataHoraInicio,
            LocalDateTime dataHoraFim,
            Long reservaIgnorarId
    ) {
        try (Session session = getSessionFactory().openSession()) {
            Long total = session.createQuery(
                    "select count(r.id) from Reserva r "
                    + "where lower(r.pistaNome) = :pistaNome "
                    + "and r.estado <> :estadoCancelada "
                    + "and r.dataHoraInicio < :dataHoraFim "
                    + "and r.dataHoraFim > :dataHoraInicio "
                    + "and (:reservaIgnorarId is null or r.id <> :reservaIgnorarId)",
                    Long.class
            )
                    .setParameter("pistaNome", pistaNomeNormalizado)
                    .setParameter("estadoCancelada", ReservaEstado.CANCELADA)
                    .setParameter("dataHoraInicio", dataHoraInicio)
                    .setParameter("dataHoraFim", dataHoraFim)
                    .setParameter("reservaIgnorarId", reservaIgnorarId)
                    .uniqueResult();

            return total != null && total > 0;
        }
    }
}
