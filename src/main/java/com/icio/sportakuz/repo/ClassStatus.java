package com.icio.sportakuz.repo;

/**
 * Status pojedynczego wystąpienia zajęć (ClassOccurrence) w cyklu życia.
 * PLANNED - utworzone, jeszcze nie otwarte do zapisów
 * OPEN - dostępne do rezerwacji
 * CANCELLED - odwołane
 * FINISHED - zakończone (archiwalne)
 */
public enum ClassStatus {PLANNED, OPEN, CANCELLED, FINISHED}
