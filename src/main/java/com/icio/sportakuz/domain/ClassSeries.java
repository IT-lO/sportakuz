package com.icio.sportakuz.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "class_series")
public class ClassSeries {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="type_id", nullable=false)
    private ClassType type;

    @ManyToOne(optional=false) @JoinColumn(name="instructor_id", nullable=false)
    private Instructor instructor;

    @ManyToOne(optional=false) @JoinColumn(name="room_id", nullable=false)
    private Room room;

    @Column(name="start_time", nullable=false)
    private OffsetDateTime startTime;

    @Column(name="end_time", nullable=false)
    private OffsetDateTime endTime;

    @Column(nullable=false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(name="recurrence_pattern", nullable=false, length=20)
    private RecurrencePattern recurrencePattern;

    @Column(name="recurrence_until", nullable=false)
    private OffsetDateTime recurrenceUntil;

    @Column(columnDefinition="text")
    private String note;

    @Column(nullable=false)
    private boolean active = true;

    @Column(name="created_at", nullable=false, insertable=false, updatable=false)
    private OffsetDateTime createdAt;

    // get/set
}
