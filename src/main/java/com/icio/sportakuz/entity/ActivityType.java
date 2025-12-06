package com.icio.sportakuz.entity;

import com.icio.sportakuz.repo.DifficultyLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "types_of_activity")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Nazwa aktywności jest wymagana")
    @Column(name = "activity_name", nullable = false, length = 120)
    private String activityName;

    @NotNull(message = "Czas trwania jest wymagany")
    @Min(value = 1, message = "Czas trwania musi wynosić co najmniej 1 minutę")
    @Column(name = "duration", nullable = false)
    private Integer duration;

    /**
     * Opis aktywności.
     * Zgodnie z tabelą jest to 'varchar [null]', więc pole nie jest wymagane.
     */
    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty")
    private DifficultyLevel difficulty;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}