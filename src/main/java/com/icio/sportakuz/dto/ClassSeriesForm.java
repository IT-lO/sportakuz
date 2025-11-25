package com.icio.sportakuz.dto;

import com.icio.sportakuz.repo.RecurrencePattern;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Formularz tworzenia/edycji serii zajęć (ClassSeries).
 * Dane czasowe (data początkowa + lokalny start + czas trwania) pozwalają odtworzyć start/end pierwszego wystąpienia.
 */
@Getter
@Setter
public class ClassSeriesForm {
    @NotNull(message="Wybierz typ zajęć")
    private Long classTypeId;
    @NotNull(message="Wybierz instruktora")
    private Long instructorId;
    @NotNull(message="Wybierz salę")
    private Long roomId;

    @NotNull(message="Podaj datę startu")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message="Podaj godzinę startu")
    @DateTimeFormat(pattern="HH:mm")
    private LocalTime startTime;

    @NotNull(message="Podaj czas trwania")
    @Positive @Max(value=600, message="Max 600 minut")
    private Integer durationMinutes;

    @NotNull(message="Podaj datę końcową generowania")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate untilDate;

    @NotNull(message="Wybierz wzorzec")
    private RecurrencePattern recurrencePattern;

    @NotNull @Positive(message="Pojemność > 0")
    private Integer capacity;

    @Size(max=1000)
    private String note;

    private boolean active = true;
}

