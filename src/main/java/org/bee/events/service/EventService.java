package org.bee.events.service;

import org.bee.events.dto.EventEntity;
import org.bee.events.domain.EventRequestDto;
import org.bee.events.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional
    public void processAndSaveEvent(EventRequestDto dto) {
        // Map DTO structural elements safely to database Columns
        EventEntity entity = new EventEntity(
                dto.eventId(),
                dto.version(),
                dto.timestamp(),
                dto.payload().userId(),
                dto.payload().email()
        );

        eventRepository.save(entity);
    }
}
