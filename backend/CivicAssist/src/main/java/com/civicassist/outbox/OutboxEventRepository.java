package com.civicassist.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent,Long> {
    List<OutboxEvent> findTop100ByStatusAndNextAttemptAtLessThanEqualOrderByIdAsc(
            OutboxEventStatus status,
            Instant currentTime
    );
}
