package com.icio.sportakuz.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "classes")
public class ClassOccurrence {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name="series_id")
    private ClassSeries series;

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
    @Column(nullable=false, length=16)
    private ClassStatus status = ClassStatus.PLANNED;

    @Column(columnDefinition="text")
    private String note;

    @Column(name="created_at", nullable=false, insertable=false, updatable=false)
    private OffsetDateTime createdAt;

    @Column(name="updated_at", nullable=false, insertable=false, updatable=false)
    private OffsetDateTime updatedAt;

    // get/set
}
