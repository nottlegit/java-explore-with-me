package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.common.exception.AlreadyExistsException;
import ru.practicum.common.exception.NotFoundException;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserDto createUser(UserDto userDto) {
        log.info("Создание пользователя email={}", userDto == null ? null : userDto.getEmail());
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new AlreadyExistsException("Пользователь с email " + userDto.getEmail() + " уже существует");
        }
        User saved = userRepository.save(UserMapper.toUser(userDto));
        log.debug("Пользователь создан id={}", saved.getId());
        return UserMapper.toUserDto(saved);
    }

    public User getUserOrThrow(Long userId) {
        log.trace("Получение пользователя по id={}", userId);
        return userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    public Collection<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        log.info("Получение пользователей: количествоId={}, from={}, size={}",
                ids == null ? 0 : ids.size(), from, size);
        List<User> users = ids == null || ids.isEmpty()
                ? userRepository.findAll()
                : userRepository.findAllById(ids);
        List<UserDto> result = users.stream()
                .sorted((left, right) -> left.getId().compareTo(right.getId()))
                .skip(defaultOffset(from))
                .limit(defaultLimit(size))
                .map(UserMapper::toUserDto)
                .toList();
        log.debug("Возвращено {} пользователей", result.size());
        return result;
    }

    public void deleteUser(Long userId) {
        log.info("Удаление пользователя id={}", userId);
        userRepository.deleteById(userId);
    }

    private Integer defaultOffset(Integer from) {
        return from == null || from < 0 ? 0 : from;
    }

    private long defaultLimit(Integer size) {
        return size == null || size <= 0 ? 10L : size;
    }
}
