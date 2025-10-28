package com.icio.sportakuz.classes.domain;

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

    public Long getId() {
        return id;
    }

    public ClassType getType() {
        return type;
    }

    public void setType(ClassType type) {
        this.type = type;
    }

    public Instructor getInstructor() {
        return instructor;
    }

    public void setInstructor(Instructor instructor) {
        this.instructor = instructor;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public OffsetDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;
    }

    public OffsetDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(OffsetDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public RecurrencePattern getRecurrencePattern() {
        return recurrencePattern;
    }

    public void setRecurrencePattern(RecurrencePattern recurrencePattern) {
        this.recurrencePattern = recurrencePattern;
    }

    public OffsetDateTime getRecurrenceUntil() {
        return recurrenceUntil;
    }

    public void setRecurrenceUntil(OffsetDateTime recurrenceUntil) {
        this.recurrenceUntil = recurrenceUntil;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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
