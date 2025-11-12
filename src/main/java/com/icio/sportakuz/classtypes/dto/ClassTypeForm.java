package com.icio.sportakuz.classtypes.dto;

import com.icio.sportakuz.classtypes.DifficultyLevel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ClassTypeForm {

    @NotBlank(message = "Nazwa typu zajęć jest wymagana.")
    @Size(max = 120, message = "Nazwa nie może przekraczać 120 znaków.")
    private String name;

    @Size(max = 5000, message = "Opis nie może przekraczać 5000 znaków.")
    private String description;

    @NotNull(message = "Czas zajęć jest wymagany.")
    @Min(value = 1, message = "Domyślny czas trwania musi być liczbą dodatnią.")
    private Integer defaultDurationMinutes;

    @NotNull(message = "Musisz wybrać poziom trudności.")
    private DifficultyLevel difficulty; // Używamy Enuma

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

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(DifficultyLevel difficulty) {
        this.difficulty = difficulty;
    }
}