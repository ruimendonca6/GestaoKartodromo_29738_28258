package pt.kartodromo.core.dal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.Session;
import pt.kartodromo.core.model.Corrida;

public class CorridaDao extends GenericHibernateDao<Corrida> {

    public CorridaDao() {
        super(Corrida.class);
    }

    public List<Corrida> findByDia(LocalDate dia) {
        LocalDateTime inicio = dia.atStartOfDay();
        LocalDateTime fim = dia.plusDays(1).atStartOfDay();

        try (Session session = getSessionFactory().openSession()) {
            return session.createQuery(
                    "from Corrida c where c.dataHoraInicio >= :inicio and c.dataHoraInicio < :fim order by c.dataHoraInicio",
                    Corrida.class
            )
                    .setParameter("inicio", inicio)
                    .setParameter("fim", fim)
                    .getResultList();
        }
    }

    public List<Corrida> findByCategoriaId(Long categoriaId) {
        try (Session session = getSessionFactory().openSession()) {
            return session.createQuery(
                    "from Corrida c where c.categoria.id = :categoriaId order by c.dataHoraInicio",
                    Corrida.class
            )
                    .setParameter("categoriaId", categoriaId)
                    .getResultList();
        }
    }
}
