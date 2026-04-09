package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserDto createUser(UserDto userDto) {
        if (userRepository.existsByName(userDto.getName())) {
            throw new AlreadyExistsException("User with name " + userDto.getName() + " already exists");
        }
        return UserMapper.toUserDto(userRepository.save(UserMapper.toUser(userDto)));
    }

    public User getUserOrThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User with id " + userId + " not found"));
    }

    public Collection<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        if (ids == null || ids.isEmpty()) {
            return UserMapper.toUserDtoList(userRepository.findAll());
        }
        return UserMapper.toUserDtoList(userRepository.findAllById(ids));
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public Collection<User> getUsersByIds(List<Long> ids) {
        return userRepository.findAllById(ids);
    }
}
