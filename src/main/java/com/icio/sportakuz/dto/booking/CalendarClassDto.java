package com.icio.sportakuz.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

///**
// * DTO przekazywany do JS (lista) wykorzystywany w kalendarzu.
// * @param id identyfikator wystąpienia zajęć
// * @param name nazwa typu zajęć
// * @param day indeks dnia tygodnia (0=Pon)
// * @param date pełna data (yyyy-MM-dd) do filtrowania tygodni
// * @param time godzina startu (HH:mm)
// * @param duration czas trwania w minutach
// * @param room nazwa sali
// * @param instructor imię i nazwisko prowadzącego
// * @param spots miejsca zajęte/pojemność
// * @param level trudność zajęć
// * @param substitutedFor imię i nazwisko instruktora zastępującego instruktora (null jeśli brak zastępstwa)
// * @param isSubstitution flaga czy jest zastępstwo
// */
@Getter
@Setter
@AllArgsConstructor
public class CalendarClassDto {

    private Long id;
    private String name;
    private int day;
    private String date;
    private String time;
    private int duration;
    private String room;
    private String instructor;
    private String spots;
    private String level;
    private String substitutedFor;
    private boolean isSubstitution;
}