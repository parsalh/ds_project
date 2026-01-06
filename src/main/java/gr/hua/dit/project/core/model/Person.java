package gr.hua.dit.project.core.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "person",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_person_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_person_email_address", columnNames = "email_address"),
                @UniqueConstraint(name = "uk_person_mobile_phone_number", columnNames = "mobile_phone_number")
        },
        indexes = {
                @Index(name = "idx_person_type", columnList = "type"),
                @Index(name = "idx_person_last_name", columnList = "last_name")
        }
)

public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @NotNull
    @NotBlank
    @Size(max = 50)
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @NotNull
    @NotBlank
    @Size(max = 100)
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotNull
    @NotBlank
    @Size(max = 100)
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @NotNull
    @NotBlank
    @Size(max = 18)
    @Column(name = "mobile_phone_number", nullable = false, length = 18)
    private String mobilePhoneNumber; // e164

    @NotNull
    @NotBlank
    @Size(max = 100)
    @Email
    @Column(name = "email_address", nullable = false, length = 100)
    private String emailAddress;

    @ElementCollection
    @CollectionTable(
            name = "person_addresses",
            joinColumns = @JoinColumn(name = "person_id")
    )
    private List<Address> addresses = new ArrayList<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private PersonType type;

    @NotNull
    @NotBlank
    @Size(min = 4, max = 24)
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Person() {}

    public Person(Long id,
                  String username,
                  String firstName,
                  String lastName,
                  String mobilePhoneNumber,
                  String emailAddress,
                  List<Address> addresses,
                  PersonType type,
                  String passwordHash,
                  Instant createdAt) {

        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mobilePhoneNumber = mobilePhoneNumber;
        this.emailAddress = emailAddress;
        this.addresses = addresses;
        this.type = type;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt; // not sure αν πρέπει να υπάρχει αυτό εδώ

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMobilePhoneNumber() {
        return mobilePhoneNumber;
    }

    public void setMobilePhoneNumber(String mobilePhoneNumber) {
        this.mobilePhoneNumber = mobilePhoneNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public PersonType getType() {
        return type;
    }

    public void setType(PersonType type) {
        this.type = type;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", mobilePhoneNumber='" + mobilePhoneNumber + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", addresses=" + addresses +
                ", type=" + type +
                '}';
    }
}
