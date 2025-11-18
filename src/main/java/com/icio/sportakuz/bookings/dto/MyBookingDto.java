package com.icio.sportakuz.bookings.dto;

public record MyBookingDto(
        Long id,
        String activityName,
        String instructor,
        String date,
        String time,
        int duration,
        String room
) {}
