package com.icio.sportakuz.entity;

import com.icio.sportakuz.repo.BookingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BookingEntityTest {

    @Test
    @DisplayName("Should correctly set and retrieve basic fields")
    void shouldSetAndGetFields() {
        // given
        Booking booking = new Booking();
        String userName = "jan.kowalski";
        BookingStatus status = BookingStatus.CONFIRMED;

        // when
        booking.setUserName(userName);
        booking.setStatus(status);

        // then
        assertThat(booking.getUserName()).isEqualTo(userName);
        assertThat(booking.getStatus()).isEqualTo(status);
    }

    @Test
    @DisplayName("Should correctly associate Activity with Booking")
    void shouldSetActivityRelationship() {
        // given
        Booking booking = new Booking();
        Activity activity = new Activity();

        // when
        booking.setActivity(activity);

        // then
        assertThat(booking.getActivity()).isNotNull();
        assertThat(booking.getActivity()).isEqualTo(activity);
    }

    @Test
    @DisplayName("Should allow manual setting of dates (ignoring DB logic)")
    void shouldSetDatesManually() {
        // given
        Booking booking = new Booking();
        OffsetDateTime now = OffsetDateTime.now();

        // when
        booking.setCreatedAt(now);
        booking.setCancelledAt(now.plusHours(1));

        // then
        assertThat(booking.getCreatedAt()).isEqualTo(now);
        assertThat(booking.getCancelledAt()).isAfter(now);
    }
}