package ru.practicum.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.ParticipationRequest;

import java.util.Collection;
import java.util.List;

@UtilityClass
public class ParticipationRequestMapper {
    public ParticipationRequestDto toDto(ParticipationRequest request) {
        if (request == null) {
            return null;
        }

        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEvent() != null ? request.getEvent().getId() : null)
                .requester(request.getRequester() != null ? request.getRequester().getId() : null)
                .status(request.getStatus())
                .build();
    }

    public List<ParticipationRequestDto> toDtoList(Collection<ParticipationRequest> requests) {
        return requests == null ? List.of() : requests.stream().map(ParticipationRequestMapper::toDto).toList();
    }
}
