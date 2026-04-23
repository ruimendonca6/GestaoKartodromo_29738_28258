package pt.kartodromo.core.dal;

import java.util.List;
import java.util.Optional;
import org.hibernate.Session;
import pt.kartodromo.core.model.Kart;

public class KartDao extends GenericHibernateDao<Kart> {

    public KartDao() {
        super(Kart.class);
    }

    public Optional<Kart> findByNumero(Integer numero) {
        try (Session session = getSessionFactory().openSession()) {
            List<Kart> result = session.createQuery(
                    "from Kart k where k.numero = :numero",
                    Kart.class
            )
                    .setParameter("numero", numero)
                    .setMaxResults(1)
                    .getResultList();

            return result.stream().findFirst();
        }
    }

    public List<Kart> findDisponiveis() {
        try (Session session = getSessionFactory().openSession()) {
            return session.createQuery(
                    "from Kart k where k.disponivel = true order by k.numero",
                    Kart.class
            ).getResultList();
        }
    }

    public List<Kart> findByCategoriaId(Long categoriaId) {
        try (Session session = getSessionFactory().openSession()) {
            return session.createQuery(
                    "from Kart k where k.categoria.id = :categoriaId order by k.numero",
                    Kart.class
            )
                    .setParameter("categoriaId", categoriaId)
                    .getResultList();
        }
    }
}
