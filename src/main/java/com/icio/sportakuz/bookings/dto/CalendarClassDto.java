package com.icio.sportakuz.bookings.dto;

/**
 * DTO przekazywany do JS (lista).
 * Pola:
 * id				- identyfikator wystąpienia
 * name				- nazwa typu zajęć
 * day				- indeks dnia tygodnia (0=Pon)
 * date				- pełna data (yyyy-MM-dd) do filtrowania tygodni
 * time				- godzina startu (HH:mm)
 * duration			- czas trwania w minutach
 * room				- nazwa sali
 * instructor		- imię i nazwisko prowadzącego (aktualnie prowadzący)
 * spots			- zajęte/pojemność
 * level			- trudność (lub pusty string)
 * substitutedFor	- imię i nazwisko instruktora zastępowanego (null jeśli brak zastępstwa)
 * isSubstitution	- flaga czy zajęcia są prowadzone w zastępstwie
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
