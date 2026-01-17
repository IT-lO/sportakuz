package com.icio.sportakuz.repo;

import com.icio.sportakuz.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Repozytorium rezerwacji {@link Booking}. Zapewnia metody liczenia aktywnych rezerwacji
 * oraz sprawdzania czy użytkownik już posiada rezerwację w określonym statusie.
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /** Liczba aktywnych rezerwacji dla danego wystąpienia zajęć (REQUESTED/CONFIRMED/PAID). */
    @Query("""
           select count(b) from Booking b
           where b.activity.id = :classId
             and b.status in (com.icio.sportakuz.repo.BookingStatus.REQUESTED,
                              com.icio.sportakuz.repo.BookingStatus.CONFIRMED,
                              com.icio.sportakuz.repo.BookingStatus.PAID)
           """)
    long countActiveByClassId(@Param("classId") Long classId);

    /** Sprawdza czy istnieje rezerwacja użytkownika w jednym z podanych statusów. */
    boolean existsByActivity_IdAndUserNameAndStatusIn(
            Long classId, String userName, Collection<BookingStatus> statuses);

    /** Pobiera pierwszą rezerwację użytkownika w jednym z podanych statusów. */
    Booking findFirstByActivity_IdAndUserNameAndStatusIn(
      Long classId, String userName, Collection<BookingStatus> statuses);

    /** Pobiera wszystkie rezerwację użytkownika. */
    List<Booking> findAllByUserNameAndActivity_EndTimeAfter(String userName, OffsetDateTime endTime);

    /** Pobiera rezerwacje po ID. */
    List<Booking> findFirstById(long id);

    /**
     * Zlicza aktywne (przyszłe) rezerwacje użytkownika.
     * Warunki:
     * 1. Zgadza się nazwa użytkownika.
     * 2. Data rozpoczęcia zajęć jest w przyszłości (> now).
     * 3. Status rezerwacji NIE jest CANCELLED (odrzucona/anulowana).
     */
    @Query("SELECT COUNT(b) FROM Booking b " +
            "WHERE b.userName = :userName " +
            "AND b.activity.startTime > :now " +
            "AND b.status != 'CANCELLED'")
    long countActiveBookings(@Param("userName") String userName,
                             @Param("now") OffsetDateTime now);

    /**
     * Zlicza odbyte zajęcia w zadanym przedziale czasu (np. ostatnie 30 dni).
     * Warunki:
     * 1. Zgadza się nazwa użytkownika.
     * 2. Zajęcia zakończyły się w przedziale (od, do).
     * 3. Status rezerwacji NIE jest CANCELLED.
     * Opcjonalnie: można dodać warunek AND b.activity.status = 'DONE', jeśli instruktorzy oznaczają obecność.
     */
    @Query("SELECT COUNT(b) FROM Booking b " +
            "WHERE b.userName = :userName " +
            "AND b.activity.endTime BETWEEN :fromDate AND :toDate " +
            "AND b.status != 'CANCELLED'")
    long countCompletedBookings(@Param("userName") String userName,
                                @Param("fromDate") OffsetDateTime fromDate,
                                @Param("toDate") OffsetDateTime toDate);

    /** Wszystkie aktywne rezerwacje dla danego wystąpienia zajęć. */
    List<Booking> findAllByActivity_IdAndStatusIn(Long activityId, Collection<BookingStatus> statuses);
}
