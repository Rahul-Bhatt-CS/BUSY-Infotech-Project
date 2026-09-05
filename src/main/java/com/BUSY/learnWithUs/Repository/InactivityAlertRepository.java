package com.BUSY.learnWithUs.Repository;

import com.BUSY.learnWithUs.Entity.InactivityAlert;
import com.BUSY.learnWithUs.Entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InactivityAlertRepository extends JpaRepository<InactivityAlert, Long> {
    List<InactivityAlert> findByDismissedAtIsNull();
    Optional<InactivityAlert> findByEnrollmentAndDismissedAtIsNull(Enrollment enrollment);
}
