package com.icio.sportakuz.classtypes.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO (Data Transfer Object) reprezentujący dane z formularza dodawania/edycji typu zajęć.
 * Zawiera walidację pól.
 */
public class ClassTypeForm {

    @NotBlank(message = "Nazwa typu zajęć jest wymagana.")
    @Size(max = 120, message = "Nazwa nie może przekraczać 120 znaków.")
    private String name;

    @Size(max = 5000, message = "Opis nie może przekraczać 5000 znaków.")
    private String description;

    @Min(value = 1, message = "Domyślny czas trwania musi być liczbą dodatnią.")
    private Integer defaultDurationMinutes; // Integer pozwala na wartość null (opcjonalność)

    @Size(max = 20, message = "Poziom trudności nie może przekraczać 20 znaków.")
    private String difficulty;

    // Gettery i Settery

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDefaultDurationMinutes() {
        return defaultDurationMinutes;
    }

    public void setDefaultDurationMinutes(Integer defaultDurationMinutes) {
        this.defaultDurationMinutes = defaultDurationMinutes;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
}