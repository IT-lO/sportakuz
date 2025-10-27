package com.icio.sportakuz.repository;

import com.icio.sportakuz.domain.ClassOccurrence;
import com.icio.sportakuz.domain.ClassStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;

public interface ClassOccurrenceRepository extends JpaRepository<ClassOccurrence, Long> {

    List<ClassOccurrence> findByStatusAndStartTimeBetween(
            ClassStatus status, OffsetDateTime from, OffsetDateTime to);

    List<ClassOccurrence> findByInstructor_IdAndStartTimeAfterOrderByStartTimeAsc(
            Long instructorId, OffsetDateTime since);

    // wykrywanie kolizji w sali (przy dodawaniu/edycji)
    @Query("""
           select c from ClassOccurrence c
           where c.room.id = :roomId
             and c.startTime < :end
             and c.endTime   > :start
           """)
    List<ClassOccurrence> findOverlappingInRoom(Long roomId, OffsetDateTime start, OffsetDateTime end);
}
