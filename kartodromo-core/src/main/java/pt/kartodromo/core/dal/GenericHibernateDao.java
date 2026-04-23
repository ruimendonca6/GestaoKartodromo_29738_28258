package pt.kartodromo.core.dal;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import pt.kartodromo.core.config.HibernateUtil;

public abstract class GenericHibernateDao<T> implements GenericDao<T, Long> {

    private final Class<T> entityClass;

    protected GenericHibernateDao(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected SessionFactory getSessionFactory() {
        return HibernateUtil.getSessionFactory();
    }

    @Override
    public T save(T entity) {
        return executeInsideTransaction(session -> {
            session.persist(entity);
            return entity;
        });
    }

    @Override
    public T update(T entity) {
        return executeInsideTransaction(session -> session.merge(entity));
    }

    @Override
    public Optional<T> findById(Long id) {
        try (Session session = getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(entityClass, id));
        }
    }

    @Override
    public List<T> findAll() {
        try (Session session = getSessionFactory().openSession()) {
            return session.createQuery(
                    "from " + entityClass.getSimpleName() + " order by id",
                    entityClass
            ).getResultList();
        }
    }

    @Override
    public void deleteById(Long id) {
        executeInsideTransaction(session -> {
            T entity = session.get(entityClass, id);
            if (entity != null) {
                session.remove(entity);
            }
            return null;
        });
    }

    protected <R> R executeInsideTransaction(Function<Session, R> action) {
        Transaction transaction = null;

        try (Session session = getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            R result = action.apply(session);
            transaction.commit();
            return result;
        } catch (RuntimeException ex) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw ex;
        }
    }
}
