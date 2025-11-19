package com.icio.sportakuz.bookings.dto;

/**
 * DTO przekazywany do JS (lista) wykorzystywany w moich rezerwacjach (MyBookings).
 * @param id id rezerwacji
 * @param activityName nazwa zajęć
 * @param instructor imie i nazwisko instruktora
 * @param date data w formacie yyyy-mm-dd
 * @param time godzina startu (HH:mm)
 * @param duration czas trwania w minutach
 * @param room nazwa sali
 * @param substitutedFor imię i nazwisko instruktora zastępującego instruktora (null jeśli brak zastępstwa)
 * @param isSubstitution flaga czy jest zastępstwo
 */
public record MyBookingDto(
        Long id,
        String activityName,
        String instructor,
        String date,
        String time,
        int duration,
        String room,
        String substitutedFor,
        boolean isSubstitution
) {}
