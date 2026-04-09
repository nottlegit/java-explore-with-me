package ru.practicum.user.mapper;

import lombok.experimental.UtilityClass;
import org.springframework.stereotype.Component;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Component
@UtilityClass
public class UserMapper {

    public User toUser(UserDto userDto) {
        if (userDto == null) {
            return null;
        }

        return User.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }

    public UserDto toUserDto(User user) {
        if (user == null) {
            return null;
        }

        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public UserShortDto toUserShortDto(User user) {
        if (user == null) {
            return null;
        }

        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }

    public List<UserDto> toUserDtoList(List<User> usersList) {
        if (usersList == null) {
            return null;
        }

        return usersList.stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    public List<UserShortDto> toUserShortDtoList(List<User> usersList) {
        if (usersList == null) {
            return null;
        }

        return usersList.stream()
                .map(UserMapper::toUserShortDto)
                .collect(Collectors.toList());
    }
}

