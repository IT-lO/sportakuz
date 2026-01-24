package com.icio.sportakuz.entity;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class RoomEntityTest {

    @Test
    void shouldHaveDefaultActiveTrue() {
        // when
        Room room = new Room();

        // then
        assertThat(room.isActive()).isTrue();
    }

    @Test
    void shouldSetAndGetValues() {
        // given
        Room room = new Room();

        // when
        room.setName("Test Room");
        room.setCapacity(100);
        room.setLocation("Basement");
        room.setActive(false);

        // then
        assertThat(room.getName()).isEqualTo("Test Room");
        assertThat(room.getCapacity()).isEqualTo(100);
        assertThat(room.getLocation()).isEqualTo("Basement");
        assertThat(room.isActive()).isFalse();
    }
}