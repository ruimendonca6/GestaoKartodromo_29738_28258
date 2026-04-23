package pt.kartodromo.core.dal;

import java.util.List;
import java.util.Optional;
import org.hibernate.Session;
import pt.kartodromo.core.model.Cliente;

public class ClienteDao extends GenericHibernateDao<Cliente> {

    public ClienteDao() {
        super(Cliente.class);
    }

    public Optional<Cliente> findByEmail(String email) {
        try (Session session = getSessionFactory().openSession()) {
            List<Cliente> result = session.createQuery(
                    "from Cliente c where c.email = :email",
                    Cliente.class
            )
                    .setParameter("email", email)
                    .setMaxResults(1)
                    .getResultList();

            return result.stream().findFirst();
        }
    }

    public List<Cliente> findAllOrderByNome() {
        try (Session session = getSessionFactory().openSession()) {
            return session.createQuery(
                    "from Cliente c order by c.nome",
                    Cliente.class
            ).getResultList();
        }
    }
}
