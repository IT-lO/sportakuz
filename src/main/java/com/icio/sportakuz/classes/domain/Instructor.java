package com.icio.sportakuz.classes.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

/**
 * Reprezentuje instruktora prowadzącego zajęcia.
 * Przechowuje dane kontaktowe, krótkie bio oraz status aktywności.
 */
@Entity
@Table(name = "instructors",
        uniqueConstraints = @UniqueConstraint(name = "uk_instructors_email", columnNames = "email"))
public class Instructor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Imię instruktora. */
    @Column(name="first_name", nullable=false, length=100)
    private String firstName;

    /** Nazwisko instruktora. */
    @Column(name="last_name", nullable=false, length=100)
    private String lastName;

    /** Unikalny adres e-mail używany do logiki kontaktowej. */
    @Column(nullable=false, length=255)
    private String email;

    /** Opcjonalny numer telefonu. */
    @Column(length=40)
    private String phone;

    /** Dłuższy opis kompetencji / doświadczenia. */
    @Column(columnDefinition = "text")
    private String bio;

    /** Czy instruktor jest aktywny (może być przypisywany do nowych zajęć). */
    @Column(nullable=false)
    private boolean active = true;

    /** Timestamp utworzenia rekordu (ustawiany przez DB). */
    @Column(name="created_at", nullable=false, insertable=false, updatable=false)
    private OffsetDateTime createdAt;

    public Long getId() {
        return id;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
