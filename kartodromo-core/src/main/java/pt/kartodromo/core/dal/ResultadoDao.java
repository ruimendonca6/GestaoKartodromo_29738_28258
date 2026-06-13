package pt.kartodromo.core.dal;

import java.util.List;

import org.hibernate.Session;

import pt.kartodromo.core.model.Resultado;

public class ResultadoDao extends GenericHibernateDao<Resultado> {

    public ResultadoDao() {
        super(Resultado.class);
    }

    public List<Resultado> findByCorrida(String nomeCorrida) {
        try (Session session = getSessionFactory().openSession()) {
            return session.createQuery(
                    "from Resultado r where r.nomeCorrida = :nome order by r.posicao asc",
                    Resultado.class
            ).setParameter("nome", nomeCorrida).getResultList();
        }
    }

    public List<Resultado> findMelhorVoltaPorPiloto() {
        try (Session session = getSessionFactory().openSession()) {
            return session.createQuery(
                    "from Resultado r where r.tempoMs = (select min(r2.tempoMs) from Resultado r2 where r2.nomePiloto = r.nomePiloto) order by r.tempoMs asc",
                    Resultado.class
            ).getResultList();
        }
    }

    public List<Resultado> findClassificacaoGeral() {
        try (Session session = getSessionFactory().openSession()) {
            return session.createQuery(
                    "from Resultado r order by r.posicao asc, r.tempoMs asc",
                    Resultado.class
            ).getResultList();
        }
    }
}
