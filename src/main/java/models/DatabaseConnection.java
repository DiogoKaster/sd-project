package models;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.util.List;

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
            } else if (returnClass.equals(Recruiter.class)) {
                Recruiter updatedRecruiter = (Recruiter) updatedObject;
                Recruiter existingRecruiter = session.get(Recruiter.class, updatedRecruiter.getId());

                if (existingRecruiter != null) {
                    if (updatedRecruiter.getName() != null) {
                        existingRecruiter.setName(updatedRecruiter.getName());
                    }
                    if (updatedRecruiter.getEmail() != null) {
                        existingRecruiter.setEmail(updatedRecruiter.getEmail());
                    }
                    if (updatedRecruiter.getPassword() != null) {
                        existingRecruiter.setPassword(updatedRecruiter.getPassword());
                    }
                    if (updatedRecruiter.getIndustry() != null) {
                        existingRecruiter.setIndustry(updatedRecruiter.getIndustry());
                    }
                    if (updatedRecruiter.getDescription() != null) {
                        existingRecruiter.setDescription(updatedRecruiter.getDescription());
                    }

                    session.update(existingRecruiter);

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

    public <T> T verifyLogin(String email, String password, Class<T> entityClass) {
        try (Session session = factory.openSession()) {
            return session.createQuery("FROM " + entityClass.getSimpleName() + " WHERE email = :email AND password = :password", entityClass)
                    .setParameter("email", email)
                    .setParameter("password", password)
                    .uniqueResult();
        } catch (Exception e) {
            System.out.println("[LOG]: Erro na verificação de login.");
            return null;
        }
    }

    public <T> T selectByName(String name, Class<T> entityClass) {
        try (Session session = factory.openSession()) {
            return session.createQuery("FROM " + entityClass.getSimpleName() + " WHERE name = :name", entityClass)
                    .setParameter("name", name)
                    .uniqueResult();
        } catch (Exception e) {
            System.out.println("[LOG]: Erro na seleção do objeto pelo nome.");
            return null;
        }
    }

    public <T> T selectWithSkills(int id, Class<T> returnClass) {
        try (Session session = factory.openSession()) {
            String hql = "FROM Candidate c LEFT JOIN FETCH c.candidateSkills cs LEFT JOIN FETCH cs.skill WHERE c.id = :id";
            return session.createQuery(hql, returnClass)
                    .setParameter("id", id)
                    .uniqueResult();
        } catch (Exception e) {
            System.out.println("[LOG]: Erro na seleção do objeto com habilidades.");
            return null;
        }
    }

    public <T> T selectWithJobs(int id, Class<T> returnClass) {
        try (Session session = factory.openSession()) {
            String hql = "FROM Recruiter r LEFT JOIN FETCH r.jobs rj LEFT JOIN FETCH rj.skill WHERE r.id = :id";
            return session.createQuery(hql, returnClass)
                    .setParameter("id", id)
                    .uniqueResult();
        } catch (Exception e) {
            System.out.println("[LOG]: Erro na seleção do objeto com habilidades.");
            return null;
        }
    }

    public List<Job> selectAllJobs() {
        try (Session session = factory.openSession()) {
            String hql = "FROM Job";
            return session.createQuery(hql, Job.class).list();
        } catch (Exception e) {
            System.out.println("[LOG]: Erro na seleção de todos os trabalhos.");
            return null;
        }
    }

    public List<Candidate> selectWithSkills() {
        try (Session session = factory.openSession()) {
            String hql = "FROM Candidate c LEFT JOIN FETCH c.candidateSkills";
            return session.createQuery(hql, Candidate.class).list();
        } catch (Exception e) {
            System.out.println("[LOG]: Erro na seleção do candidato com skills.");
            return null;
        }
    }

    public <T> T selectWithChosen(int id, Class<T> returnClass) {
        try (Session session = factory.openSession()) {
            String hql;
            if (returnClass == Candidate.class) {
                hql = "FROM Candidate c LEFT JOIN FETCH c.chosenCandidates cc LEFT JOIN FETCH cc.recruiter WHERE c.id = :id";
            } else if (returnClass == Recruiter.class) {
                hql = "FROM Recruiter r LEFT JOIN FETCH r.chosenCandidates cc LEFT JOIN FETCH cc.candidate WHERE r.id = :id";
            } else {
                throw new IllegalArgumentException("Classe desconhecida: " + returnClass.getName());
            }
            return session.createQuery(hql, returnClass)
                    .setParameter("id", id)
                    .uniqueResult();
        } catch (Exception e) {
            System.out.println("[LOG]: Erro na seleção do objeto com relacionamentos.");
            return null;
        }
    }
}
