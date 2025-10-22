package org.example.diploma.service;

import org.example.diploma.model.User;
import org.example.diploma.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Конструктор UserService - внедрение зависимостей
    // вход:
    //   - userRepository - репозиторий для работы с пользователями
    //   - passwordEncoder - кодировщик паролей Spring Security
    // выход: созданный экземпляр UserService
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // validateRegistration - валидация данных регистрации пользователя
    // вход:
    //   - user - объект пользователя для валидации
    //   - bindingResult - объект для хранения ошибок валидации
    // выход:
    //   - true - если данные валидны
    //   - false - если есть ошибки валидации
    // логика:
    //  - проверяет уникальность имени пользователя
    //  - проверяет дополнительные ограничения через аннотации в модели User
    public boolean validateRegistration(User user, BindingResult bindingResult) {
        // проверка на уникальность имени пользователя
        if (userRepository.existsByUsername(user.getUsername())) {
            bindingResult.addError(new FieldError("user", "username", "Username already exists"));
            return false;
        }

        // дополнительные проверки (уже есть аннотации в модели)
        return !bindingResult.hasErrors();
    }

    // registerUser - регистрация нового пользователя
    // вход: user - объект пользователя для регистрации
    // выход: сохраненный пользователь с закодированным паролем
    // логика:
    //  - кодирует пароль пользователя перед сохранением
    //  - сохраняет пользователя в базу данных
    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // findByUsername - поиск пользователя по имени пользователя
    // вход: username - имя пользователя для поиска
    // выход: Optional<User> - пользователь, если найден
    // логика:
    //  - используется для аутентификации и поиска пользователей по логину
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // findUserById - поиск пользователя по идентификатору
    // вход: id - идентификатор пользователя
    // выход: Optional<User> - пользователь, если найден
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    // saveUser - сохранение пользователя в базу данных
    // вход: user - объект пользователя для сохранения
    // выход: сохраненный объект пользователя
    // примечание: не кодирует пароль (используется для обновления данных без изменения пароля)
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    // findByGroupId - поиск пользователей по идентификатору группы
    // вход: groupId - идентификатор группы
    // выход: список пользователей, принадлежащих указанной группе
    // логика:
    //  - используется для получения списка студентов в группе
    public List<User> findByGroupId(Long groupId) {
        return userRepository.findByGroupId(groupId);
    }

    // findStudentsWithoutGroup - поиск студентов без привязки к группе
    // вход: отсутствует
    // выход: список студентов, не привязанных к какой-либо группе
    // логика:
    //  - используется при добавлении студентов в группы
    public List<User> findStudentsWithoutGroup() {
        return userRepository.findByRoleAndGroupIsNull("STUDENT");
    }
}