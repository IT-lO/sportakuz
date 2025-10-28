// src/main/java/com/icio/sportakuz/classes/web/ClassOccurrenceForm.java
package com.icio.sportakuz.classes.web;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public class ClassOccurrenceForm {

    @NotNull(message = "Wybierz typ zajęć")
    private Long classTypeId;

    @NotNull(message = "Wybierz instruktora")
    private Long instructorId;

    @NotNull(message = "Wybierz salę")
    private Long roomId;

    @NotNull(message = "Podaj datę")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @NotNull(message = "Podaj godzinę rozpoczęcia")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull(message = "Podaj godzinę zakończenia")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @NotNull @Positive(message = "Pojemność musi być > 0")
    private Integer capacity;

    @Size(max = 1000)
    private String note;

    public Long getClassTypeId() { return classTypeId; }
    public void setClassTypeId(Long classTypeId) { this.classTypeId = classTypeId; }

    public Long getInstructorId() { return instructorId; }
    public void setInstructorId(Long instructorId) { this.instructorId = instructorId; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
