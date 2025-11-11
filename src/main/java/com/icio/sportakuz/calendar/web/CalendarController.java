package com.icio.sportakuz.calendar.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Prosty kontroler wyświetlający stronę kalendarza zajęć.
 * Aktualnie logika jest czysto front-endowa (JS generuje widok i rezerwacje klienta).
 * W przyszłości można dodać:
 *  - pobieranie wystąpień zajęć (ClassOccurrence) z bazy i serializację do JSON,
 *  - endpointy REST do rezerwacji,
 *  - filtrowanie po typie / instruktorze.
 */
@Controller
@RequestMapping("/calendar")
public class CalendarController {

    /** GET /calendar – główny widok kalendarza. */
    @GetMapping
    public String calendarRoot(Model model) {
        model.addAttribute("pageTitle", "Kalendarz Zajęć Sportowych");
        return "calendar/calendar"; // templates/calendar/calendar.html
    }
    
}
