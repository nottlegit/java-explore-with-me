package ru.practicum.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.service.UserService;

import java.util.Collection;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminUserController {
    private final UserService userService;

    @GetMapping
    public Collection<UserDto> getUsers(
            @RequestParam(name = "ids", required = false) List<Long> ids,
            @RequestParam(name = "from", defaultValue = "0") Integer from,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return userService.getUsers(ids, from, size);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }

    @PostMapping
    public UserDto createUser(@Valid @RequestBody UserDto userDto) {
        return userService.createUser(userDto);
    }
}
