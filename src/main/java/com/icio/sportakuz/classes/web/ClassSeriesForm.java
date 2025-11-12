package com.icio.sportakuz.classes.web;

import com.icio.sportakuz.classes.domain.RecurrencePattern;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Formularz tworzenia/edycji serii zajęć (ClassSeries).
 * Dane czasowe (data początkowa + lokalny start + czas trwania) pozwalają odtworzyć start/end pierwszego wystąpienia.
 */
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

    public Long getClassTypeId() { return classTypeId; }
    public void setClassTypeId(Long classTypeId) { this.classTypeId = classTypeId; }
    public Long getInstructorId() { return instructorId; }
    public void setInstructorId(Long instructorId) { this.instructorId = instructorId; }
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public LocalDate getUntilDate() { return untilDate; }
    public void setUntilDate(LocalDate untilDate) { this.untilDate = untilDate; }
    public RecurrencePattern getRecurrencePattern() { return recurrencePattern; }
    public void setRecurrencePattern(RecurrencePattern recurrencePattern) { this.recurrencePattern = recurrencePattern; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}

