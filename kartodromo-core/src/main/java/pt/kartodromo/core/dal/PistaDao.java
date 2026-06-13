package pt.kartodromo.core.dal;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;

import pt.kartodromo.core.model.Pista;

public class PistaDao extends GenericHibernateDao<Pista> {

    public PistaDao() {
        super(Pista.class);
    }

    public Optional<Pista> findByNome(String nome) {
        try (Session session = getSessionFactory().openSession()) {
            return session.createQuery("from Pista p where p.nome = :nome", Pista.class)
                    .setParameter("nome", nome)
                    .setMaxResults(1)
                    .getResultList()
                    .stream().findFirst();
        }
    }

    public List<Pista> findAtivas() {
        try (Session session = getSessionFactory().openSession()) {
            return session.createQuery("from Pista p where p.ativa = true order by p.nome", Pista.class)
                    .getResultList();
        }
    }
}
