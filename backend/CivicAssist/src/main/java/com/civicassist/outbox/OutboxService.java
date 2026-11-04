package com.civicassist.outbox;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final JsonMapper objectMapper;

    public OutboxService(
            OutboxEventRepository outboxEventRepository,
            JsonMapper objectMapper
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void recordEvent(
            String aggregateType,
            String aggregateId,
            String eventType,
            String correlationId,
            Object eventPayload
    ) {
        UUID eventId = UUID.randomUUID();
        Instant occurredAt = Instant.now();

        JsonNode payload = objectMapper.valueToTree(eventPayload);

        EventEnvelope envelope = new EventEnvelope(
                eventId,
                eventType,
                1,
                occurredAt,
                correlationId,
                aggregateType,
                aggregateId,
                payload
        );

        JsonNode envelopeJson = objectMapper.valueToTree(envelope);

        OutboxEvent outboxEvent = new OutboxEvent();

        outboxEvent.setEventId(eventId);
        outboxEvent.setAggregateType(aggregateType);
        outboxEvent.setAggregateId(aggregateId);
        outboxEvent.setEventType(eventType);
        outboxEvent.setEventVersion(1);
        outboxEvent.setPayload(envelopeJson);
        outboxEvent.setStatus(OutboxEventStatus.PENDING);

        outboxEventRepository.save(outboxEvent);
    }
}