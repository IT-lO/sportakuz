package com.icio.sportakuz.classes.repo;

import com.icio.sportakuz.classes.domain.Booking;
import com.icio.sportakuz.classes.domain.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

/**
 * Repozytorium rezerwacji {@link Booking}. Zapewnia metody liczenia aktywnych rezerwacji
 * oraz sprawdzania czy użytkownik już posiada rezerwację w określonym statusie.
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /** Liczba aktywnych rezerwacji dla danego wystąpienia zajęć (REQUESTED/CONFIRMED/PAID). */
    @Query("""
           select count(b) from Booking b
           where b.clazz.id = :classId
             and b.status in (com.icio.sportakuz.classes.domain.BookingStatus.REQUESTED,
                              com.icio.sportakuz.classes.domain.BookingStatus.CONFIRMED,
                              com.icio.sportakuz.classes.domain.BookingStatus.PAID)
           """)
    long countActiveByClassId(@Param("classId") Long classId);

    /** Sprawdza czy istnieje rezerwacja użytkownika w jednym z podanych statusów. */
    boolean existsByClazz_IdAndUserNameAndStatusIn(
            Long classId, String userName, Collection<BookingStatus> statuses);
}
