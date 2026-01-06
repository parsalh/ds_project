package gr.hua.dit.project.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Objects;

/**
 * Client Entity.
 */
@Entity
@Table(
        name = "client",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_client_name",columnNames = "name")
        }
)
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotNull
    @NotBlank
    @Size(max=100)
    @Pattern(regexp = "[a-zA-Z][a-zA-Z0-9_]")
    // TODO REGEX validation
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotNull
    @NotBlank
    @Size(max=255)
    @Column(name = "secret", nullable = false,length = 255)
    private String secret;

    @Column(name = "permissions_csv", nullable = false, length = 255)
    private String permissionsCsv;

    public Client(Long id, String name, String secret, String permissionsCsv) {
        this.id = id;
        this.name = name;
        this.secret = secret;
        this.permissionsCsv = permissionsCsv;
    }

    public Client() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getPermissionsCsv() {
        return permissionsCsv;
    }

    public void setPermissionsCsv(String permissionsCsv) {
        this.permissionsCsv = permissionsCsv;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return Objects.equals(name, client.name)
                && Objects.equals(secret, client.secret)
                && Objects.equals(permissionsCsv, client.permissionsCsv);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, secret, permissionsCsv);
    }

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", secret='" + secret + '\'' +
                ", permissionsCsv='" + permissionsCsv + '\'' +
                '}';
    }
}
