package pt.kartodromo.core.dal;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import pt.kartodromo.core.config.HibernateUtil;

public abstract class GenericHibernateDao<T> {

    private final Class<T> entityClass;

    protected GenericHibernateDao(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected SessionFactory getSessionFactory() {
        return HibernateUtil.getSessionFactory();
    }

    public T save(T entity) {
        Transaction tx = null;

        try (Session session = getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            session.persist(entity);

            tx.commit();

            return entity;

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }

            throw new RuntimeException("Erro ao guardar entidade.", e);
        }
    }

    public T update(T entity) {
        Transaction tx = null;

        try (Session session = getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            T mergedEntity = session.merge(entity);

            tx.commit();

            return mergedEntity;

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }

            throw new RuntimeException("Erro ao atualizar entidade.", e);
        }
    }

    public void delete(T entity) {
        Transaction tx = null;

        try (Session session = getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            T managedEntity = session.contains(entity)
                    ? entity
                    : session.merge(entity);

            session.remove(managedEntity);

            tx.commit();

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }

            throw new RuntimeException("Erro ao remover entidade.", e);
        }
    }

    public void deleteById(Long id) {
        Transaction tx = null;

        try (Session session = getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            T entity = session.get(entityClass, id);

            if (entity != null) {
                session.remove(entity);
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }

            throw new RuntimeException("Erro ao remover entidade por ID.", e);
        }
    }

    public Optional<T> findById(Long id) {
        try (Session session = getSessionFactory().openSession()) {
            return Optional.ofNullable(
                    session.get(entityClass, id)
            );
        }
    }

    public List<T> findAll() {
        try (Session session = getSessionFactory().openSession()) {
            return session.createQuery(
                    "from " + entityClass.getSimpleName(),
                    entityClass
            ).getResultList();
        }
    }
}