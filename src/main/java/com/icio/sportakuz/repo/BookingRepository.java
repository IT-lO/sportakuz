package com.icio.sportakuz.repo;

import com.icio.sportakuz.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
           where b.clazz.id = :classId
             and b.status in (com.icio.sportakuz.repo.BookingStatus.REQUESTED,
                              com.icio.sportakuz.repo.BookingStatus.CONFIRMED,
                              com.icio.sportakuz.repo.BookingStatus.PAID)
           """)
    long countActiveByClassId(@Param("classId") Long classId);

    /** Sprawdza czy istnieje rezerwacja użytkownika w jednym z podanych statusów. */
    boolean existsByClazz_IdAndUserNameAndStatusIn(
            Long classId, String userName, Collection<BookingStatus> statuses);

    /** Pobiera pierwszą rezerwację użytkownika w jednym z podanych statusów. */
    Booking findFirstByClazz_IdAndUserNameAndStatusIn(
      Long classId, String userName, Collection<BookingStatus> statuses);

    /** Pobiera wszystkie rezerwację użytkownika. */
    List<Booking> findAllByUserName(String userName);

    /** Pobiera rezerwacje po ID. */
    List<Booking> findFirstById(long id);
}
