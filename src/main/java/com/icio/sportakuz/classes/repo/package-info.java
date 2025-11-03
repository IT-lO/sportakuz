/**
 * Pakiet repozytoriow Spring Data dla encji związanych z zajęciami.
 * Interfejsy rozszerzają JpaRepository i dodają wyspecjalizowane metody wyszukiwania / zapytań.
 *
 * Przegląd:
 *  - RoomRepository – operacje na salach (Room), unikalne wyszukiwanie po nazwie.
 *  - InstructorRepository – operacje na instruktorach, wyszukiwanie po e-mailu.
 *  - ClassTypeRepository – operacje na typach zajęć, wyszukiwanie po nazwie.
 *  - ClassSeriesRepository – operacje na seriach cyklicznych (RecurrencePattern, daty).
 *  - ClassOccurrenceRepository – operacje na pojedynczych wystąpieniach, kolizje i listy najbliższych.
 *  - BookingRepository – operacje na rezerwacjach, liczenie aktywnych i sprawdzanie duplikatów.
 */
package com.icio.sportakuz.classes.repo;

