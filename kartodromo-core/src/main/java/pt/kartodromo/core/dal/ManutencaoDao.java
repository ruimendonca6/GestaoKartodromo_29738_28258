package pt.kartodromo.core.dal;

import java.time.LocalDate;
import java.util.List;

import org.hibernate.Session;

import pt.kartodromo.core.model.Manutencao;

public class ManutencaoDao extends GenericHibernateDao<Manutencao> {

    public ManutencaoDao() {
        super(Manutencao.class);
    }

    public List<Manutencao> findByKartId(Long kartId) {
        try (Session session = getSessionFactory().openSession()) {
            return session.createQuery(
                    "from Manutencao m where m.kart.id = :kartId order by m.dataEntrada desc",
                    Manutencao.class
            ).setParameter("kartId", kartId).getResultList();
        }
    }

    public List<Manutencao> findEmCurso() {
        try (Session session = getSessionFactory().openSession()) {
            return session.createQuery(
                    "from Manutencao m where m.concluida = false order by m.dataEntrada desc",
                    Manutencao.class
            ).getResultList();
        }
    }

    public List<Manutencao> findProximasRevisoes(LocalDate ate) {
        try (Session session = getSessionFactory().openSession()) {
            return session.createQuery(
                    "from Manutencao m where m.proximaRevisao is not null and m.proximaRevisao <= :ate order by m.proximaRevisao asc",
                    Manutencao.class
            ).setParameter("ate", ate).getResultList();
        }
    }
}
