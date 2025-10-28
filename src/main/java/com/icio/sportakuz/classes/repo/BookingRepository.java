package com.icio.sportakuz.classes.repo;

import com.icio.sportakuz.classes.domain.Booking;
import com.icio.sportakuz.classes.domain.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // liczba aktywnych rezerwacji dla zajęć (REQUESTED/CONFIRMED/PAID)
    @Query("""
           select count(b) from Booking b
           where b.clazz.id = :classId
             and b.status in (com.icio.sportakuz.classes.domain.BookingStatus.REQUESTED,
                              com.icio.sportakuz.classes.domain.BookingStatus.CONFIRMED,
                              com.icio.sportakuz.classes.domain.BookingStatus.PAID)
           """)
    long countActiveByClassId(Long classId);

    boolean existsByClazz_IdAndUserNameAndStatusIn(
            Long classId, String userName, Collection<BookingStatus> statuses);
}
