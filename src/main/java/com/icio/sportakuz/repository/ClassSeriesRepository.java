package com.icio.sportakuz.repository;

import com.icio.sportakuz.domain.ClassSeries;
import com.icio.sportakuz.domain.RecurrencePattern;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface ClassSeriesRepository extends JpaRepository<ClassSeries, Long> {
    List<ClassSeries> findByRecurrencePattern(RecurrencePattern pattern);
    List<ClassSeries> findByActiveTrueAndRecurrenceUntilAfter(OffsetDateTime dt);
}
