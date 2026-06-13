package pt.kartodromo.core.dal;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;

import pt.kartodromo.core.model.Utilizador;

public class UtilizadorDao extends GenericHibernateDao<Utilizador> {

    public UtilizadorDao() {
        super(Utilizador.class);
    }

    public Optional<Utilizador> findByUsername(String username) {
        try (Session session = getSessionFactory().openSession()) {
            List<Utilizador> result =
                    session.createQuery(
                            "from Utilizador u where u.username = :username",
                            Utilizador.class
                    )
                    .setParameter("username", username)
                    .setMaxResults(1)
                    .getResultList();

            return result.stream().findFirst();
        }
    }

    public Optional<Utilizador> findByEmail(String email) {
        try (Session session = getSessionFactory().openSession()) {
            List<Utilizador> result =
                    session.createQuery(
                            "from Utilizador u where u.email = :email",
                            Utilizador.class
                    )
                    .setParameter("email", email)
                    .setMaxResults(1)
                    .getResultList();

            return result.stream().findFirst();
        }
    }

    public List<Utilizador> findAllOrderByUsername() {
        try (Session session = getSessionFactory().openSession()) {
            return session.createQuery(
                    "from Utilizador u order by u.username",
                    Utilizador.class
            ).getResultList();
        }
    }
}