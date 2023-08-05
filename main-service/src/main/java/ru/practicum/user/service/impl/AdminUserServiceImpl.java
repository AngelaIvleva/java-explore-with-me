package ru.practicum.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.exception.ConflictException;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.user.service.AdminUserService;
import ru.practicum.util.ObjectCheckExistence;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.user.mapper.UserMapper.USER_MAPPER;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository repository;
    private final ObjectCheckExistence checkExistence;

    @Override
    public List<UserDto> getUsers(List<Long> ids, Pageable pageable) {
        Page<User> users;

        if (ids != null) {
            log.info("Запрошен список пользователей по id");
            users = repository.findByIdIn(ids, pageable);
        } else {
            log.info("Запрошен список пользователей по параметрам");
            users = repository.findAll(pageable);
        }

        return users.stream()
                .map(USER_MAPPER::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto createUser(UserShortDto userShortDto) {
        validateUsernameExists(userShortDto.getName());

        User user = repository.save(USER_MAPPER.toUser(userShortDto));
        log.info("Создан пользователь {}, email {}, id {}", user.getName(), user.getEmail(), user.getId());
        return USER_MAPPER.toUserDto(user);
    }

    @Override
    public void deleteUser(Long userId) {
        checkExistence.checkUser(userId);
        log.info("Удаление пользователя с id {}", userId);
        repository.deleteById(userId);
    }

    private void validateUsernameExists(String username) {
        if (repository.findByName(username).isPresent()) {
            log.info("Пользователь с именем {} уже существует", username);
            throw new ConflictException(String.format("Пользователь с именем %s уже существует", username));
        }
    }
}
