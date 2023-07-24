package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.exception.ConflictException;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.util.ObjectCheckExistence;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.user.mapper.UserMapper.USER_MAPPER;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements ru.practicum.user.service.UserService {

    private final UserRepository repository;
    private final ObjectCheckExistence checkExistence;

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        PageRequest pageable = PageRequest.of(from / size, size);
        if (ids != null) {
            log.info("Запрошен список пользователей по id");
            return repository.findByIdIn(ids, pageable).stream()
                    .map(USER_MAPPER::toUserDto)
                    .collect(Collectors.toList());
        } else {
            log.info("Запрошен список пользователей по параметрам");
            return repository.findAll(pageable).stream()
                    .map(USER_MAPPER::toUserDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public UserDto createUser(UserShortDto userShortDto) {
        Optional<User> userOpt = repository.findByName(userShortDto.getName());
        if (userOpt.isPresent()) {
            log.info("Пользователь с именем {} уже существует", userShortDto.getName());
            throw new ConflictException(String.format("Пользователь с именем %s уже существует",
                    userShortDto.getName()));
        }
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
}
