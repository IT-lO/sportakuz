package com.icio.sportakuz.classtypes;

public enum DifficultyLevel {
    BEGINNER("Dla początkujących"),
    INTERMEDIATE("Średniozaawansowany"),
    ADVANCED("Zaawansowany"),
    ALL_LEVELS("Wszystkie poziomy");

    private final String displayName;

    DifficultyLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}