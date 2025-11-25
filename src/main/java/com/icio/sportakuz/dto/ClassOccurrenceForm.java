package com.icio.sportakuz.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
/**
 * Formularz tworzenia/edycji pojedynczego wystąpienia zajęć (ClassOccurrence).
 * Zawiera identyfikatory encji powiązanych oraz dane czasowe używane do rekonstrukcji
 * OffsetDateTime (po stronie kontrolera). Walidacje Bean Validation gwarantują podstawową
 * poprawność danych przed mapowaniem na encję.
 */
@Getter
@Setter
public class ClassOccurrenceForm {

    /** Id typu zajęć (ClassType). */
    @NotNull(message = "Wybierz typ zajęć")
    private Long classTypeId;

    /** Id instruktora prowadzącego. */
    @NotNull(message = "Wybierz instruktora")
    private Long instructorId;

    /** Id sali w której odbywają się zajęcia. */
    @NotNull(message = "Wybierz salę")
    private Long roomId;

    /** Data kalendarzowa zajęć (strefa czasu zostanie dodana przy mapowaniu). */
    @NotNull(message = "Podaj datę")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    /** Lokalny czas rozpoczęcia (HH:mm). */
    @NotNull(message = "Podaj godzinę rozpoczęcia")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime startTime;

    /** Czas trwania w minutach (dodatni, maksymalnie 600 min). */
    @NotNull(message = "Podaj czas trwania w minutach")
    @Positive(message = "Czas trwania musi być > 0")
    @Max(value = 600, message = "Czas trwania nie może przekraczać 600 minut") // 10h safeguard
    private Integer durationMinutes;

    /** Pojemność (liczba miejsc) – nadpisywana do maksimum sali, jeśli formularz poda większą. */
    @NotNull @Positive(message = "Pojemność musi być > 0")
    private Integer capacity;

    /** Opcjonalna notatka organizacyjna (limit znaków w walidacji). */
    @Size(max = 1000)
    private String note;
}
