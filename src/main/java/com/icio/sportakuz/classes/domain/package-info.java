/**
 * Pakiet domain zawiera model danych aplikacji dot. zarządzania zajęciami sportowymi.
 *
 * Encje:
 *  - Room - Sala (miejsce) realizacji zajęć.
 *  - Instructor - Osoba prowadząca zajęcia.
 *  - ClassType - Typ zajęć (np. Joga, Pilates) ze stałymi parametrami.
 *  - ClassSeries - Seria cyklicznych zajęć wg wzorca powtarzania.
 *  - ClassOccurrence - Konkretny termin pojedynczych zajęć.
 *  - Booking - Rezerwacja uczestnika na konkretne zajęcia.
 *
 * Enumy pomocnicze:
 *  - ClassStatus - Bieżący status wystąpienia zajęć.
 *  - BookingStatus - Status rezerwacji.
 *  - RecurrencePattern - Wzorzec powtarzania serii.
 *
 * Relacje:
 *  ClassSeries -> ClassType, Instructor, Room (ManyToOne)
 *  ClassOccurrence -> (opcjonalnie) ClassSeries, oraz ClassType, Instructor, Room (ManyToOne)
 *  Booking -> ClassOccurrence (ManyToOne)
 */
package com.icio.sportakuz.classes.domain;

