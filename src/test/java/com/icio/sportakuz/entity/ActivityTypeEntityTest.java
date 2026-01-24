package com.icio.sportakuz.entity;

import com.icio.sportakuz.repo.DifficultyLevel;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityTypeEntityTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should build valid object using Lombok Builder")
    void shouldBuildValidObject() {
        // given
        DifficultyLevel level = DifficultyLevel.ADVANCED;

        // when
        ActivityType type = ActivityType.builder()
                .activityName("Crossfit")
                .duration(60)
                .description("Intensywny trening")
                .difficulty(level)
                .build();

        // then
        assertThat(type.getActivityName()).isEqualTo("Crossfit");
        assertThat(type.getDuration()).isEqualTo(60);
        assertThat(type.getDifficulty()).isEqualTo(level);

        var violations = validator.validate(type);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should detect invalid activity name (Blank)")
    void shouldDetectInvalidName() {
        // given
        ActivityType type = ActivityType.builder()
                .activityName("")
                .duration(30)
                .build();

        // when
        var violations = validator.validate(type);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("activityName")
                        && v.getMessage().contains("wymagana"));
    }

    @Test
    @DisplayName("Should detect invalid duration (Too short)")
    void shouldDetectInvalidDuration() {
        // given
        ActivityType type = ActivityType.builder()
                .activityName("Joga")
                .duration(0)
                .build();

        // when
        var violations = validator.validate(type);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("duration");
    }

    @Test
    @DisplayName("Should detect null duration")
    void shouldDetectNullDuration() {
        // given
        ActivityType type = ActivityType.builder()
                .activityName("Joga")
                .duration(null)
                .build();

        // when
        var violations = validator.validate(type);

        // then
        assertThat(violations).isNotEmpty();
    }
}