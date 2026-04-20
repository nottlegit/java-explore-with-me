package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.exception.AlreadyExistsException;
import ru.practicum.common.exception.NotFoundException;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationDto;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Transactional
    public CompilationDto createCompilation(NewCompilationDto dto) {
        log.info("Администратор создаёт подборку с названием '{}'", dto.getTitle());
        validateTitleIsUnique(dto.getTitle(), null);
        List<Event> events = resolveEvents(dto.getEvents());
        Compilation compilation = CompilationMapper.toCompilation(dto, events);
        Compilation savedCompilation = compilationRepository.save(compilation);
        log.info("Администратор создал подборку {}", savedCompilation.getId());
        return CompilationMapper.toCompilationDto(savedCompilation);
    }

    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationDto dto) {
        log.info("Администратор обновляет подборку {}", compId);
        Compilation compilation = getCompilationOrThrow(compId);
        if (dto.getTitle() != null) {
            validateTitleIsUnique(dto.getTitle(), compilation.getId());
        }
        List<Event> events = dto.getEvents() == null ? null : resolveEvents(dto.getEvents());
        CompilationMapper.applyUpdate(compilation, dto, events);
        Compilation updatedCompilation = compilationRepository.save(compilation);
        log.info("Администратор обновил подборку {}", compId);
        return CompilationMapper.toCompilationDto(updatedCompilation);
    }

    @Transactional
    public void deleteCompilation(Long compId) {
        log.info("Администратор удаляет подборку {}", compId);
        Compilation compilation = getCompilationOrThrow(compId);
        compilationRepository.delete(compilation);
    }

    @Transactional(readOnly = true)
    public CompilationDto getCompilation(Long compId) {
        log.info("Пользователь запрашивает подборку {}", compId);
        return CompilationMapper.toCompilationDto(getCompilationOrThrow(compId));
    }

    @Transactional(readOnly = true)
    public Collection<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        log.info("Пользователь запрашивает список подборок: pinned={}, from={}, size={}", pinned, from, size);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id"));
        Page<Compilation> page;

        if (pinned == null) {
            page = compilationRepository.findAll(pageable);
        } else {
            page = compilationRepository.findAllByPinned(pinned, pageable);
        }
        log.info("Пользователь получил {} подборок", page.getContent().size());
        return CompilationMapper.toCompilationDtoList(page.getContent());
    }

    private Compilation getCompilationOrThrow(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id=" + compId + " не найдена"));
    }

    private List<Event> resolveEvents(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Event> byId = eventRepository.findAllById(eventIds).stream()
                .collect(Collectors.toMap(Event::getId, event -> event, (left, right) -> left, LinkedHashMap::new));

        List<Event> events = new ArrayList<>();
        for (Long eventId : eventIds) {
            Event event = byId.get(eventId);
            if (event == null) {
                throw new NotFoundException("Событие с id=" + eventId + " не найдено");
            }
            events.add(event);
        }
        return events;
    }

    private void validateTitleIsUnique(String title, Long currentId) {
        if (title == null) {
            return;
        }
        boolean exists = compilationRepository.existsByTitle(title);
        if (!exists) {
            return;
        }
        Compilation existing = compilationRepository.findAll().stream()
                .filter(item -> title.equals(item.getTitle()))
                .findFirst()
                .orElse(null);
        if (existing != null && (currentId == null || !Objects.equals(existing.getId(), currentId))) {
            throw new AlreadyExistsException("Подборка с названием " + title + " уже существует");
        }
    }
}
