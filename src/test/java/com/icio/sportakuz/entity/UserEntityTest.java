package com.icio.sportakuz.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {

    @Test
    @DisplayName("Should initialize user with default active status as true")
    void shouldHaveDefaultActiveTrue() {
        // given
        User user = new User();

        // when & then
        assertThat(user.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should correctly set and retrieve field values")
    void shouldSetAndGetFields() {
        // given
        User user = new User();
        String email = "test@example.com";
        String bio = "To jest opis użytkownika.";

        // when
        user.setEmail(email);
        user.setBio(bio);
        user.setFirstName("Jan");
        user.setActive(false);

        // then
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getBio()).isEqualTo(bio);
        assertThat(user.getFirstName()).isEqualTo("Jan");
        assertThat(user.isActive()).isFalse();
    }
}