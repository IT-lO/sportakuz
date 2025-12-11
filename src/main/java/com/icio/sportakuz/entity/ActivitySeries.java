package com.icio.sportakuz.entity;

import com.icio.sportakuz.repo.ActivityTypeRepository; // Importy zależne od Twojej struktury
import com.icio.sportakuz.repo.RecurrencePattern;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * Szablon (Seria) dla cyklicznych zajęć.
 * Nie reprezentuje konkretnego wydarzenia w kalendarzu, ale "przepis" na nie.
 * * Przykład: "Joga w każdy wtorek o 18:00 przez 60 minut".
 * Na podstawie tej encji generowane są rekordy w tabeli 'activities'.
 */
@Entity
@Table(name = "activity_series")
@Getter
@Setter
public class ActivitySeries {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Data i godzina PIERWSZEGO wystąpienia w serii.
     * Służy jako punkt odniesienia (kotwica) do wyliczania kolejnych dat.
     * Np. 2023-10-01 18:00:00 (to definiuje, że zajęcia są o 18:00).
     */
    @Column(name="default_start_time", nullable=false)
    private OffsetDateTime startTime;

    /**
     * Czas trwania w minutach.
     * To jest wartość domyślna, która będzie kopiowana do pola 'durationMinutes' w Activity.
     */
    @Column(name = "default_duration_minutes", nullable = false)
    private Integer durationMinutes;

    // --- Dane "szablonowe" (kopiowane do Activity) ---

    /** Domyślny typ zajęć dla tej serii. */
    @ManyToOne(optional=false)
    @JoinColumn(name="type_id", nullable=false)
    private ActivityType type;

    /** Domyślny instruktor. */
    @ManyToOne(optional=false)
    @JoinColumn(name="instructor_id", nullable=false)
    private User instructor;

    /** Domyślna sala. */
    @ManyToOne(optional=false)
    @JoinColumn(name="room_id", nullable=false)
    private Room room;

    /** Domyślna pojemność. */
    @Column(name = "default_capacity",nullable=false)
    private Integer capacity;

    // --- Logika powtarzalności ---

    /** * Wzorzec powtarzania (np. WEEKLY, DAILY).
     * Określa co ile czasu dodawać interwał do startTime.
     */
    @Enumerated(EnumType.STRING)
    @Column(name="recurrence_pattern", nullable=false, length=20)
    private RecurrencePattern recurrencePattern;

    /** * Data graniczna. System nie powinien generować Activity po tej dacie.
     */
    @Column(name="end_date", nullable=false)
    private OffsetDateTime recurrenceUntil;

    // --- Pola administracyjne ---

    /** Notatka dla całej serii (np. "Grupa zaawansowana"). */
    @Column(columnDefinition="text")
    private String note;

    /** * Czy seria jest aktywna?
     * Jeśli false -> skrypt generujący kolejne zajęcia powinien pominąć tę serię.
     */
    @Column(nullable=false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;

    public OffsetDateTime getEndTime() {
        if (this.startTime != null && this.durationMinutes != null) {
            return this.startTime.plusMinutes(this.durationMinutes);
        }
        return null;
    }
}