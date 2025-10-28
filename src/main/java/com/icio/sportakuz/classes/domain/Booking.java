package com.icio.sportakuz.classes.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "bookings",
        indexes = {
                @Index(name="idx_bookings_class", columnList="class_id"),
                @Index(name="idx_bookings_status", columnList="status")
        })
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false)
    @JoinColumn(name="class_id", nullable=false)
    private ClassOccurrence clazz;

    @Column(name="user_name", nullable=false, length=100)
    private String userName;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=16)
    private BookingStatus status;

    @Column(name="created_at", nullable=false, insertable=false, updatable=false)
    private OffsetDateTime createdAt;

    @Column(name="cancelled_at")
    private OffsetDateTime cancelledAt;

    public Long getId() {
        return id;
    }

    public ClassOccurrence getClazz() {
        return clazz;
    }

    public void setClazz(ClassOccurrence clazz) {
        this.clazz = clazz;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(OffsetDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }
}
