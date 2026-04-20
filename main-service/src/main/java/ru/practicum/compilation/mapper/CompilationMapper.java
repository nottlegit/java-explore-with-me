package ru.practicum.compilation.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;

import java.util.List;

@UtilityClass
public class CompilationMapper {
    public Compilation toCompilation(NewCompilationDto dto, List<Event> events) {
        if (dto == null) {
            return null;
        }

        return Compilation.builder()
                .title(dto.getTitle())
                .pinned(Boolean.TRUE.equals(dto.getPinned()))
                .events(events == null ? List.of() : events)
                .build();
    }

    public void applyUpdate(Compilation compilation, UpdateCompilationDto dto, List<Event> events) {
        if (compilation == null || dto == null) {
            return;
        }

        if (dto.getTitle() != null) {
            compilation.setTitle(dto.getTitle());
        }
        if (dto.getPinned() != null) {
            compilation.setPinned(dto.getPinned());
        }
        if (events != null) {
            compilation.setEvents(events);
        }
    }

    public CompilationDto toCompilationDto(Compilation compilation) {
        if (compilation == null) {
            return null;
        }

        List<EventShortDto> events = compilation.getEvents() == null
                ? List.of()
                : compilation.getEvents().stream().map(EventMapper::toEventShortDto).toList();

        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(events)
                .build();
    }

    public List<CompilationDto> toCompilationDtoList(List<Compilation> compilations) {
        return compilations == null ? List.of() : compilations.stream().map(CompilationMapper::toCompilationDto).toList();
    }
}
