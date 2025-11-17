package com.icio.sportakuz.classes.repo;

import com.icio.sportakuz.classes.domain.ClassOccurrence;
import com.icio.sportakuz.classes.domain.ClassStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Repozytorium pojedynczych wystąpień zajęć {@link ClassOccurrence}.
 * Oferuje zestaw metod do wyszukiwania wg statusu, zakresów czasowych oraz kolizji w salach i u instruktorów.
 * Zawiera również zapytania do pobierania najbliższych (widocznych) zajęć dla strony głównej.
 */
public interface ClassOccurrenceRepository extends JpaRepository<ClassOccurrence, Long> {

    /** Wystąpienia o podanym statusie mieszczące się w przedziale czasu. */
    List<ClassOccurrence> findByStatusAndStartTimeBetween(
            ClassStatus status, OffsetDateTime from, OffsetDateTime to);

    /** Przyszłe wystąpienia danego instruktora posortowane rosnąco po czasie rozpoczęcia. */
    List<ClassOccurrence> findByInstructor_IdAndStartTimeAfterOrderByStartTimeAsc(
            Long instructorId, OffsetDateTime since);

    /** Kolizje w sali (nakładające się przedziały czasowe) – status różny od CANCELLED. */
    @Query("""
           select c from ClassOccurrence c
           where c.room.id = :roomId
             and c.status <> com.icio.sportakuz.classes.domain.ClassStatus.CANCELLED
             and c.startTime < :end
             and c.endTime   > :start
           """)
    List<ClassOccurrence> findOverlappingInRoom(@Param("roomId") Long roomId,
                                                @Param("start") OffsetDateTime start,
                                                @Param("end") OffsetDateTime end);

    /** Kolizje czasowe instruktora. */
    @Query("""
           select c from ClassOccurrence c
           where c.instructor.id = :instructorId
             and c.status <> com.icio.sportakuz.classes.domain.ClassStatus.CANCELLED
             and c.startTime < :end
             and c.endTime   > :start
           """)
    List<ClassOccurrence> findOverlappingForInstructor(@Param("instructorId") Long instructorId,
                                                       @Param("start") OffsetDateTime start,
                                                       @Param("end") OffsetDateTime end);

    /** Wszystkie wystąpienia posortowane rosnąco po czasie rozpoczęcia. */
    List<ClassOccurrence> findAllByOrderByStartTimeAsc();

    /** Liczba kolidujących wystąpień w sali. */
    @Query("""
           select count(c) from ClassOccurrence c
           where c.room.id = :roomId
             and c.status <> com.icio.sportakuz.classes.domain.ClassStatus.CANCELLED
             and c.startTime < :end
             and c.endTime   > :start
           """)
    long countOverlappingInRoom(@Param("roomId") Long roomId,
                                @Param("start") OffsetDateTime start,
                                @Param("end") OffsetDateTime end);

    /** Liczba kolidujących wystąpień u instruktora. */
    @Query("""
           select count(c) from ClassOccurrence c
           where c.instructor.id = :instructorId
             and c.status <> com.icio.sportakuz.classes.domain.ClassStatus.CANCELLED
             and c.startTime < :end
             and c.endTime   > :start
           """)
    long countOverlappingForInstructor(@Param("instructorId") Long instructorId,
                                       @Param("start") OffsetDateTime start,
                                       @Param("end") OffsetDateTime end);

    /** Najbliższe planowane zajęcia od chwili 'now'. */
    @Query("""
           select c from ClassOccurrence c
           where c.status = com.icio.sportakuz.classes.domain.ClassStatus.PLANNED
             and c.startTime >= :now
           order by c.startTime asc
           """)
    List<ClassOccurrence> findNextPlanned(@Param("now") OffsetDateTime now, Pageable pageable);

    /** Lista wszystkich przyszłych planowanych zajęć od chwili 'now'. */
    @Query("""
           select c from ClassOccurrence c
           where c.status = com.icio.sportakuz.classes.domain.ClassStatus.PLANNED
             and c.startTime > :now
           order by c.startTime asc
           """)
    List<ClassOccurrence> findUpcoming(@Param("now") OffsetDateTime now);

    /** Najbliższe widoczne (nieanulowane) zajęcia z możliwością ograniczenia liczby rekordów. */
    @Query("""
           select c from ClassOccurrence c
           where c.status <> com.icio.sportakuz.classes.domain.ClassStatus.CANCELLED
             and c.endTime >= :now
           order by c.startTime asc
           """)
    List<ClassOccurrence> findNextVisible(@Param("now") OffsetDateTime now, Pageable pageable);

    /** Sprawdza czy istnieje wystąpienie w danej serii dokładnie o wskazanym starcie. */
    boolean existsBySeries_IdAndStartTime(Long seriesId, OffsetDateTime startTime);

    /** Wszystkie wystąpienia powiązane z daną serią. */
    List<ClassOccurrence> findBySeries_Id(Long seriesId);
}
