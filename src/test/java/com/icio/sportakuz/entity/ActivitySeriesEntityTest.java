package com.icio.sportakuz.entity;

import com.icio.sportakuz.repo.RecurrencePattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class ActivitySeriesEntityTest {

    @Test
    @DisplayName("Should correctly calculate end time based on start time and duration")
    void shouldCalculateEndTime() {
        // given
        ActivitySeries series = new ActivitySeries();
        OffsetDateTime start = OffsetDateTime.of(2023, 10, 1, 12, 0, 0, 0, ZoneOffset.UTC);
        int duration = 90;

        series.setStartTime(start);
        series.setDurationMinutes(duration);

        // when
        OffsetDateTime endTime = series.getEndTime();

        // then
        assertThat(endTime).isNotNull();
        assertThat(endTime).isEqualTo(start.plusMinutes(duration));
        assertThat(endTime.getHour()).isEqualTo(13);
        assertThat(endTime.getMinute()).isEqualTo(30);
    }

    @Test
    @DisplayName("Should return null end time if start time or duration is missing")
    void shouldReturnNullEndTimeIfDataMissing() {
        // given
        ActivitySeries seriesNoDuration = new ActivitySeries();
        seriesNoDuration.setStartTime(OffsetDateTime.now());

        ActivitySeries seriesNoStart = new ActivitySeries();
        seriesNoStart.setDurationMinutes(60);

        // when & then
        assertThat(seriesNoDuration.getEndTime()).isNull();
        assertThat(seriesNoStart.getEndTime()).isNull();
    }

    @Test
    @DisplayName("Should initialize with active = true by default")
    void shouldHaveDefaultActiveTrue() {
        // given
        ActivitySeries series = new ActivitySeries();

        // when & then
        assertThat(series.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should correctly link related entities (Room, User, Type)")
    void shouldLinkRelatedEntities() {
        // given
        ActivitySeries series = new ActivitySeries();

        Room room = new Room();
        room.setName("Sala A");

        User instructor = new User();
        instructor.setEmail("trener@test.pl");

        ActivityType type = new ActivityType();
        type.setActivityName("Pilates");

        // when
        series.setRoom(room);
        series.setInstructor(instructor);
        series.setType(type);
        series.setRecurrencePattern(RecurrencePattern.WEEKLY);

        // then
        assertThat(series.getRoom().getName()).isEqualTo("Sala A");
        assertThat(series.getInstructor().getEmail()).isEqualTo("trener@test.pl");
        assertThat(series.getType().getActivityName()).isEqualTo("Pilates");
        assertThat(series.getRecurrencePattern()).isEqualTo(RecurrencePattern.WEEKLY);
    }
}