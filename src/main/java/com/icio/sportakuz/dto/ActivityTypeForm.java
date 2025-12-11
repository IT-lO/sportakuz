package com.icio.sportakuz.dto;

import com.icio.sportakuz.repo.DifficultyLevel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ActivityTypeForm {

    @NotBlank(message = "Nazwa aktywności jest wymagana.")
    @Size(max = 120, message = "Nazwa nie może przekraczać 120 znaków.")
    private String activityName;

    @NotNull(message = "Czas trwania jest wymagany.")
    @Min(value = 1, message = "Czas trwania musi wynosić co najmniej 1 minutę.")
    private Integer duration;

    @Size(max = 1000, message = "Opis nie może przekraczać 1000 znaków.")
    private String description;

    @NotNull(message = "Musisz wybrać poziom trudności.")
    private DifficultyLevel difficulty;
}