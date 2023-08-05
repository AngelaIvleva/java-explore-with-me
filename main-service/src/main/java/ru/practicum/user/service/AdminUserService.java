package ru.practicum.user.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;

import java.util.List;

public interface AdminUserService {

    List<UserDto> getUsers(List<Long> ids, Pageable pageable);

    UserDto createUser(UserShortDto userShortDto);

    void deleteUser(Long userId);
}
