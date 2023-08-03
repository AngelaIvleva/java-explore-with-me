package ru.practicum.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.service.impl.AdminUserServiceImpl;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminUserController {

    private final AdminUserServiceImpl userService;

    @GetMapping
    public List<UserDto> get(@RequestParam(value = "ids", required = false) List<Long> ids,
                             @PositiveOrZero @RequestParam(value = "from", defaultValue = "0",
                                     required = false) int from,
                             @Positive @RequestParam(value = "size", defaultValue = "10",
                                     required = false) int size) {
        return userService.getUsers(ids, PageRequest.of(from / size, size));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody UserShortDto userShortDto) {
        return userService.createUser(userShortDto);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("userId") Long userId) {
        userService.deleteUser(userId);
    }
}
