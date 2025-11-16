package com.icio.sportakuz.rooms.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO (Data Transfer Object) reprezentujący dane z formularza dodawania/edycji sali.
 * Zawiera walidację pól.
 */
@Getter
@Setter
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
}