package pt.kartodromo.core.config;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import pt.kartodromo.core.model.CategoriaKart;
import pt.kartodromo.core.model.Cliente;
import pt.kartodromo.core.model.Corrida;
import pt.kartodromo.core.model.Kart;
import pt.kartodromo.core.model.Reserva;

public final class HibernateUtil {

    private static final SessionFactory SESSION_FACTORY = buildSessionFactory();

    private HibernateUtil() {
    }

    private static SessionFactory buildSessionFactory() {
        StandardServiceRegistry registry = null;

        try {
            registry = new StandardServiceRegistryBuilder()
                    .configure()
                    .build();

            MetadataSources sources = new MetadataSources(registry)
                    .addAnnotatedClass(Cliente.class)
                    .addAnnotatedClass(CategoriaKart.class)
                    .addAnnotatedClass(Corrida.class)
                    .addAnnotatedClass(Kart.class)
                    .addAnnotatedClass(Reserva.class);

            Metadata metadata = sources.getMetadataBuilder().build();
            return metadata.getSessionFactoryBuilder().build();

        } catch (Exception ex) {
            if (registry != null) {
                StandardServiceRegistryBuilder.destroy(registry);
            }
            throw new IllegalStateException("Erro ao inicializar o Hibernate.", ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return SESSION_FACTORY;
    }

    public static void shutdown() {
        if (SESSION_FACTORY != null && !SESSION_FACTORY.isClosed()) {
            SESSION_FACTORY.close();
        }
    }
}
