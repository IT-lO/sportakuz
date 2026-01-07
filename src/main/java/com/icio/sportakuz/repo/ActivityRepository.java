package com.icio.sportakuz.repo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import com.icio.sportakuz.entity.Activity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Repozytorium pojedynczych wystąpień zajęć {@link Activity}.
 * Oferuje zestaw metod do wyszukiwania wg statusu, zakresów czasowych oraz kolizji w salach i u instruktorów.
 * Zawiera również zapytania do pobierania najbliższych (widocznych) zajęć dla strony głównej.
 */
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    /** Wystąpienia o podanym statusie mieszczące się w przedziale czasu. */
    List<Activity> findByStatusAndStartTimeBetween(
            ClassStatus status, OffsetDateTime from, OffsetDateTime to);

    /** Przyszłe wystąpienia danego instruktora posortowane rosnąco po czasie rozpoczęcia. */
    List<Activity> findByInstructor_IdAndStartTimeAfterOrderByStartTimeAsc(
            Long instructorId, OffsetDateTime since);

    /** Kolizje w sali (nakładające się przedziały czasowe) – status różny od CANCELLED. */
    @Query("""
           select c from Activity c
           where c.room.id = :roomId
             and c.status <> com.icio.sportakuz.repo.ClassStatus.CANCELLED
             and c.startTime < :end
             and c.endTime   > :start
           """)
    List<Activity> findOverlappingInRoom(@Param("roomId") Long roomId,
                                         @Param("start") OffsetDateTime start,
                                         @Param("end") OffsetDateTime end);

    /** Kolizje czasowe instruktora. */
    @Query("""
           select c from Activity c
           where c.instructor.id = :instructorId
             and c.status <> com.icio.sportakuz.repo.ClassStatus.CANCELLED
             and c.startTime < :end
             and c.endTime   > :start
           """)
    List<Activity> findOverlappingForInstructor(@Param("instructorId") Long instructorId,
                                                @Param("start") OffsetDateTime start,
                                                @Param("end") OffsetDateTime end);

    /** Wszystkie wystąpienia posortowane rosnąco po czasie rozpoczęcia. */
    List<Activity> findAllByOrderByStartTimeAsc();

        /** Wyszukiwanie po typie lub instruktorze; sortowane po starcie. */
        @Query("""
          select c from Activity c
          where (:likePattern is null or :likePattern = ''
            or lower(c.type.activityName) like :likePattern
            or lower(c.instructor.firstName) like :likePattern
            or lower(c.instructor.lastName) like :likePattern)
          order by c.startTime asc
          """)
        List<Activity> searchOrderByStartTimeAsc(@Param("likePattern") String likePattern);

    /** Liczba kolidujących wystąpień w sali. */
    @Query("""
           select count(c) from Activity c
           where c.room.id = :roomId
             and c.status <> com.icio.sportakuz.repo.ClassStatus.CANCELLED
             and c.startTime < :end
             and c.endTime   > :start
           """)
    long countOverlappingInRoom(@Param("roomId") Long roomId,
                                @Param("start") OffsetDateTime start,
                                @Param("end") OffsetDateTime end);

    /** Liczba kolidujących wystąpień u instruktora. */
    @Query("""
           select count(c) from Activity c
           where c.instructor.id = :instructorId
             and c.status <> com.icio.sportakuz.repo.ClassStatus.CANCELLED
             and c.startTime < :end
             and c.endTime   > :start
           """)
    long countOverlappingForInstructor(@Param("instructorId") Long instructorId,
                                       @Param("start") OffsetDateTime start,
                                       @Param("end") OffsetDateTime end);

    /** Najbliższe planowane zajęcia od chwili 'now'. */
    @Query("""
           select c from Activity c

           where c.status = com.icio.sportakuz.repo.ClassStatus.PLANNED
             and c.startTime >= :now
           order by c.startTime asc
           """)
    List<Activity> findNextPlanned(@Param("now") OffsetDateTime now, Pageable pageable);

    /** Lista wszystkich przyszłych planowanych zajęć od chwili 'now'. */
    @Query("""
           select c from Activity c
           where c.status = com.icio.sportakuz.repo.ClassStatus.PLANNED
             and c.startTime > :now
           order by c.startTime asc
           """)
    List<Activity> findUpcoming(@Param("now") OffsetDateTime now);

    /** Najbliższe widoczne (nieanulowane) zajęcia z możliwością ograniczenia liczby rekordów. */
    @Query("""
           select c from Activity c
           where c.status <> com.icio.sportakuz.repo.ClassStatus.CANCELLED
             and c.endTime >= :now
           order by c.startTime asc
           """)
    List<Activity> findNextVisible(@Param("now") OffsetDateTime now, Pageable pageable);

    /** Sprawdza czy istnieje wystąpienie w danej serii dokładnie o wskazanym starcie. */
    boolean existsBySeries_IdAndStartTime(Long seriesId, OffsetDateTime startTime);

    /** Wszystkie wystąpienia powiązane z daną serią. */
    List<Activity> findBySeries_Id(Long seriesId);

    /** Pobiera wystąpienie zajęć po ID. Blokuje wykonanie metody na czas transakcji.*/
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Activity c where c.id = :id")
    Activity findByIdForUpdate(@Param("id") Long id);

    /**
     * Zlicza zajęcia przeprowadzone przez instruktora w zadanym przedziale czasu.
     * Warunki:
     * 1. Instruktor ma podany email.
     * 2. Status NIE jest CANCELLED.
     * 3. Czas zakończenia mieści się w przedziale (np. ostatnie 30 dni).
     */
    @Query("""
           select count(a) from Activity a
           where a.instructor.email = :email
             and a.status <> com.icio.sportakuz.repo.ClassStatus.CANCELLED
             and a.endTime between :from and :to
           """)
    long countCompletedByInstructor(@Param("email") String email,
                                    @Param("from") OffsetDateTime from,
                                    @Param("to") OffsetDateTime to);

    /**
     * Pobiera nadchodzące zajęcia TYLKO dla danego instruktora (przydatne do widoku "Nadchodzące").
     */
    @Query("""
           select a from Activity a
           where a.instructor.email = :email
             and a.status <> com.icio.sportakuz.repo.ClassStatus.CANCELLED
             and a.endTime >= :now
           order by a.startTime asc
           """)
    List<Activity> findUpcomingForInstructor(@Param("email") String email,
                                             @Param("now") OffsetDateTime now,
                                             Pageable pageable);
}
