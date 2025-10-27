package com.icio.sportakuz.domain;

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

    // get/set
}
