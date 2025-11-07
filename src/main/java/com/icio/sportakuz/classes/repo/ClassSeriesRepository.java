package com.icio.sportakuz.classes.repo;

import com.icio.sportakuz.classes.domain.ClassSeries;
import com.icio.sportakuz.classes.domain.RecurrencePattern;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Repozytorium serii zajęć {@link ClassSeries}. Umożliwia filtrowanie po wzorcu powtarzania
 * oraz wyszukiwanie aktywnych serii, których okres obowiązywania jeszcze trwa.
 */
public interface ClassSeriesRepository extends JpaRepository<ClassSeries, Long> {
    /** Zwraca serie o zadanym wzorcu powtarzania. */
    List<ClassSeries> findByRecurrencePattern(RecurrencePattern pattern);
    /** Aktywne serie z datą końcową po wskazanej chwili. */
    List<ClassSeries> findByActiveTrueAndRecurrenceUntilAfter(OffsetDateTime dt);
}
