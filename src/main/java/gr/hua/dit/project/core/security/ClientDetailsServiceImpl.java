package gr.hua.dit.project.core.security;

import gr.hua.dit.project.core.model.Client;
import gr.hua.dit.project.core.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link ClientDetailsService}
 */
@Service
public class ClientDetailsServiceImpl implements ClientDetailsService {

    private final ClientRepository clientRepository;

    public ClientDetailsServiceImpl(final ClientRepository clientRepository) {
        if (clientRepository == null) throw new NullPointerException();
        this.clientRepository = clientRepository;
    }

    @Override
    public Optional<ClientDetails> authenticate(final String id, final String secret) {
        if (id == null || secret == null) throw new NullPointerException();
        if (id.isBlank() || secret.isBlank()) throw new IllegalArgumentException();

        final Client client = this.clientRepository.findByName(id).orElse(null);
        if (client == null) {
            return Optional.empty(); // client does not exist.
        }

        if (Objects.equals(client.getSecret(), secret)) {
            // TODO better and more secure implementation.
            // ClientDetails.id - map - Client.name
            // ClientDetails.secret - map - Client.secret
            // ClientDetails.roles - map - Client.permissionsCsv (comma separated values)
            final ClientDetails clientDetails = new ClientDetails(
                    client.getName(),
                    client.getSecret(),
                    client.getPermissionsCsv() == null
                            ? Collections.emptySet()
                            : Arrays.stream(client.getPermissionsCsv().split(","))
                                .map(String::strip)
                                .collect(Collectors.toSet()));
            return Optional.of(clientDetails);

        } else {
            return Optional.empty();
        }

    }
}
