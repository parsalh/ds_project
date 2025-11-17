package gr.hua.dit.project.core.model;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

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

    @Column(name = "username", nullable = false, length = 20)
    private String username;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "mobile_phone_number", nullable = false, length = 18)
    private String mobilePhoneNumber; // e164

    @Column(name = "email_address", nullable = false, length = 100)
    private String emailAddress;

    @Column(name = "address", nullable = false, length = 100)
    private String address; //ισως να το κανω list idk

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private PersonType type;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Person() {}

    public Person(
                  String username,
                  String firstName,
                  String lastName,
                  String mobilePhoneNumber,
                  String emailAddress,
                  String address,
                  PersonType type,
                  String passwordHash,
                  Instant createdAt) {

        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mobilePhoneNumber = mobilePhoneNumber;
        this.emailAddress = emailAddress;
        this.address = address;
        this.type = type;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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
                ", address='" + address + '\'' +
                ", type=" + type +
                ", passwordHash='" + passwordHash + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

}
