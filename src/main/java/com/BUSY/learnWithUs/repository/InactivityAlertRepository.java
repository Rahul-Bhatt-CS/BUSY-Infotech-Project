package com.BUSY.learnWithUs.repository;

import com.BUSY.learnWithUs.entity.InactivityAlert;
import com.BUSY.learnWithUs.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InactivityAlertRepository extends JpaRepository<InactivityAlert, Long> {
    List<InactivityAlert> findByDismissedAtIsNull();
    Optional<InactivityAlert> findByEnrollmentAndDismissedAtIsNull(Enrollment enrollment);
}
