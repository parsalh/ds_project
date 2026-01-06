package gr.hua.dit.project.core.repository;

import gr.hua.dit.project.core.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for {@link Client} entity.
 */
public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByName(final String name);

}
