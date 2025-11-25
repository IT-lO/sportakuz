package com.icio.sportakuz.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * Reprezentuje instruktora prowadzącego zajęcia.
 * Przechowuje dane kontaktowe, krótkie bio oraz status aktywności.
 */
@Entity
@Table(name = "instructors",
        uniqueConstraints = @UniqueConstraint(name = "uk_instructors_email", columnNames = "email"))
@Getter
@Setter
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
    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;
}
