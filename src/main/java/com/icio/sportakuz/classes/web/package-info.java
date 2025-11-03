/**
 * Pakiet classes.web przeznaczony na kontrolery warstwy prezentacji obsługujące moduł "zajęcia".
 * Docelowo trafią tu endpointy:
 *  - GET /classes          : lista wystąpień (widok list.html)
 *  - GET /classes/new      : formularz tworzenia (widok new.html)
 *  - POST /classes         : zapis nowego wystąpienia
 *  - GET /classes/{id}/edit: formularz edycji (widok edit.html)
 *  - POST /classes/{id}    : zapis zmian
 *  - POST /classes/{id}/delete : usunięcie / anulowanie
 *
 * Widoki Thymeleaf znajdują się w resources/templates/classes/*.html.
 * Formularze korzystają z pól: classTypeId, instructorId, roomId, date, startTime, durationMinutes, capacity, note.
 *
 */
package com.icio.sportakuz.classes.web;

