package com.icio.sportakuz.repo;

import com.icio.sportakuz.entity.ActivitySeries;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Repozytorium serii zajęć {@link ActivitySeries}. Umożliwia filtrowanie po wzorcu powtarzania
 * oraz wyszukiwanie aktywnych serii, których okres obowiązywania jeszcze trwa.
 */
public interface ActivitySeriesRepository extends JpaRepository<ActivitySeries, Long> {
    /** Zwraca serie o zadanym wzorcu powtarzania. */
    List<ActivitySeries> findByRecurrencePattern(RecurrencePattern pattern);
    /** Aktywne serie z datą końcową po wskazanej chwili. */
    List<ActivitySeries> findByActiveTrueAndRecurrenceUntilAfter(OffsetDateTime dt);
}
