package ru.practicum.event.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.Event;
import ru.practicum.location.dto.LocationDto;
import ru.practicum.location.model.Location;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;

@UtilityClass
public class EventMapper {
    public Event toEvent(NewEventDto dto, Category category, User initiator) {
        if (dto == null) {
            return null;
        }

        return Event.builder()
                .annotation(dto.getAnnotation())
                .category(category)
                .createdOn(null)
                .description(dto.getDescription())
                .eventDate(dto.getEventDate())
                .initiator(initiator)
                .location(toLocation(dto.getLocation()))
                .paid(dto.getPaid())
                .participantLimit(dto.getParticipantLimit())
                .publishedOn(null)
                .requestModeration(dto.getRequestModeration())
                .state(null)
                .title(dto.getTitle())
                .build();
    }

    public EventDto toEventDto(Event event) {
        if (event == null) {
            return null;
        }

        return EventDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(toCategoryDto(event.getCategory()))
                .confirmedRequests(0)
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .location(toLocationDto(event.getLocation()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(0L)
                .build();
    }

    public EventShortDto toEventShortDto(Event event) {
        if (event == null) {
            return null;
        }

        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(toCategoryDto(event.getCategory()))
                .confirmedRequests(0)
                .eventDate(event.getEventDate())
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(0L)
                .build();
    }

    public Location toLocation(LocationDto dto) {
        if (dto == null) {
            return null;
        }
        return Location.builder()
                .lat(dto.getLat())
                .lon(dto.getLon())
                .build();
    }

    private CategoryDto toCategoryDto(Category category) {
        return CategoryMapper.toCategoryDto(category);
    }

    private LocationDto toLocationDto(Location location) {
        if (location == null) {
            return null;
        }
        return LocationDto.builder()
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }
}
