package com.icio.sportakuz.repo;

import com.icio.sportakuz.entity.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActivityTypeRepository extends JpaRepository<ActivityType, Integer> {

    Optional<ActivityType> findByActivityName(String activityName);

    boolean existsByActivityName(String activityName);
}