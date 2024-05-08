package models;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class DatabaseConnection {
    private final SessionFactory factory;

    private DatabaseConnection() {
        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure().build();
        factory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
    }

    private static final class InstanceHolder {
        private static final DatabaseConnection instance = new DatabaseConnection();
    }

    public static DatabaseConnection getInstance() {
        return InstanceHolder.instance;
    }

    public <T> T insert(Object object, Class<T> returnClass) {
        try (Session session = factory.openSession()) {
            session.beginTransaction();
            session.persist(object);
            session.getTransaction().commit();
            System.out.println("[LOG]: Objeto inserido.");

            return returnClass.cast(object);
        } catch (Exception e) {
            System.out.println("[LOG]: Erro na inserção do objeto.");
            return null;
        }
    }

    public <T> T update(Object updatedObject, Class<T> returnClass) {
        try (Session session = factory.openSession()) {
            Transaction transaction = session.beginTransaction();

            if (updatedObject == null) {
                System.out.println("[LOG]: Objeto inválido para atualização.");
                return null;
            }

            if (returnClass.equals(Candidate.class)) {
                Candidate updatedCandidate = (Candidate) updatedObject;
                Candidate existingCandidate = session.get(Candidate.class, updatedCandidate.getId());

                if (existingCandidate != null) {
                    if (updatedCandidate.getName() != null) {
                        existingCandidate.setName(updatedCandidate.getName());
                    }
                    if (updatedCandidate.getEmail() != null) {
                        existingCandidate.setEmail(updatedCandidate.getEmail());
                    }
                    if (updatedCandidate.getPassword() != null) {
                        existingCandidate.setPassword(updatedCandidate.getPassword());
                    }

                    session.update(existingCandidate);

                    transaction.commit();
                    System.out.println("[LOG]: Objeto atualizado com sucesso.");
                } else {
                    System.out.println("[LOG]: Objeto não encontrado no banco de dados.");
                }
            } else {
                session.update(updatedObject);
                transaction.commit();
                System.out.println("[LOG]: Objeto atualizado com sucesso.");
            }

            return returnClass.cast(updatedObject);
        } catch (Exception e) {
            System.out.println("[LOG]: Erro na atualização do objeto.");
            return null;
        }
    }


    public <T> T select(int id, Class<T> returnClass){
        try (Session session = factory.openSession()) {
            return session.find(returnClass, id);
        } catch (Exception e) {
            System.out.println("[LOG]: Erro na seleção do objeto.");
        }
        return null;
    }

    public <T> void delete(int id, Class<T> entityClass) {
        try (Session session = factory.openSession()) {
            Transaction transaction = session.beginTransaction();
            T objectToDelete = session.get(entityClass, id);
            if (objectToDelete != null) {
                session.delete(objectToDelete);
                transaction.commit();
            } else {
                System.out.println("[LOG]: Objeto não encontrado com o ID fornecido.");
            }
        } catch (Exception e) {
            System.out.println("[LOG]: Erro na exclusão do objeto.");
        }
    }

    public Candidate verifyLogin(String email, String password) {
        Session session = factory.openSession();
            return session.createQuery("FROM Candidate WHERE email = :email AND password = :password", Candidate.class)
                    .setParameter("email", email)
                    .setParameter("password", password)
                    .uniqueResult();
    }
}
