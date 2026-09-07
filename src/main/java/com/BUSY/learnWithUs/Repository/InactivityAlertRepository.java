package com.BUSY.learnWithUs.Repository;

import com.BUSY.learnWithUs.Entity.InactivityAlert;
import com.BUSY.learnWithUs.Entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InactivityAlertRepository extends JpaRepository<InactivityAlert, Long> {
    List<InactivityAlert> findByDismissedAtIsNullOrderByTriggeredAtAsc();
    long countByDismissedAtIsNull();
}
