package com.icio.sportakuz.bookings.dto;

/**
 * DTO przekazywany do JS (lista) wykorzystywany w kalendarzu.
 * @param id identyfikator wystąpienia zajęć
 * @param name nazwa typu zajęć
 * @param day indeks dnia tygodnia (0=Pon)
 * @param date pełna data (yyyy-MM-dd) do filtrowania tygodni
 * @param time godzina startu (HH:mm)
 * @param duration czas trwania w minutach
 * @param room nazwa sali
 * @param instructor imię i nazwisko prowadzącego
 * @param spots miejsca zajęte/pojemność
 * @param level trudność zajęć
 * @param substitutedFor imię i nazwisko instruktora zastępującego instruktora (null jeśli brak zastępstwa)
 * @param isSubstitution flaga czy jest zastępstwo
 */
public record CalendarClassDto(Long id,
							   String name,
							   int day,
							   String date,
							   String time,
							   int duration,
							   String room,
							   String instructor,
							   String spots,
							   String level,
							   String substitutedFor,
							   boolean isSubstitution) {}
