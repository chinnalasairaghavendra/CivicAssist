package com.civicassist.outbox;

import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

public record EventEnvelope(
        UUID eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        String correlationId,
        String aggregateType,
        String aggregateId,
        JsonNode payload
) {
}