package pt.kartodromo.core.dal;

import java.util.Optional;
import org.hibernate.Session;
import pt.kartodromo.core.model.CategoriaKart;

public class CategoriaKartDao extends GenericHibernateDao<CategoriaKart> {

    public CategoriaKartDao() {
        super(CategoriaKart.class);
    }

    public Optional<CategoriaKart> findByNome(String nome) {
        try (Session session = getSessionFactory().openSession()) {
            return session.createQuery(
                    "from CategoriaKart c where c.nome = :nome",
                    CategoriaKart.class
            )
                    .setParameter("nome", nome)
                    .uniqueResultOptional();
        }
    }
}
