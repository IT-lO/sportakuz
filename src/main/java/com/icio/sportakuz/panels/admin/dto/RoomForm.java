package com.icio.sportakuz.panels.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO (Data Transfer Object) reprezentujący dane z formularza dodawania/edycji sali.
 * Zawiera walidację pól.
 */
public class RoomForm {

    @NotBlank(message = "Nazwa sali jest wymagana.")
    @Size(max = 120, message = "Nazwa nie może przekraczać 120 znaków.")
    private String name;

    @Size(max = 255, message = "Lokalizacja nie może przekraczać 255 znaków.")
    private String location;

    @NotNull(message = "Pojemność jest wymagana.")
    @Min(value = 1, message = "Pojemność musi wynosić co najmniej 1.")
    private Integer capacity;

    private boolean active = true;

    // Gettery i Settery

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}