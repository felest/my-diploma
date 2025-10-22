package org.example.diploma.repository;

import org.example.diploma.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // findByUsername - поиск пользователя по имени пользователя
    // вход: username - имя пользователя для поиска
    // выход: Optional<User> - пользователь, если найден
    // логика:
    //  - используется для аутентификации и поиска пользователя по логину
    //  - возвращает Optional для безопасной обработки случая, когда пользователь не найден
    Optional<User> findByUsername(String username);

    // findByGroupId - поиск пользователей по идентификатору группы
    // вход: groupId - идентификатор группы
    // выход: список пользователей, принадлежащих указанной группе
    // логика:
    //  - возвращает всех студентов, которые состоят в указанной группе
    //  - используется для отображения списка студентов в группе
    List<User> findByGroupId(Long groupId);

    // findByRoleAndGroupIsNull - поиск пользователей по роли без привязки к группе
    // вход: role - роль пользователя (например, "STUDENT")
    // выход: список пользователей с указанной ролью, не привязанных к какой-либо группе
    // логика:
    //  - используется для поиска студентов, которых можно добавить в группу
    //  - помогает при распределении студентов по группам
    List<User> findByRoleAndGroupIsNull(String role);

    // existsByUsername - проверка существования пользователя с указанным именем
    // вход: username - имя пользователя для проверки
    // выход:
    //   - true - если пользователь с таким именем существует
    //   - false - если пользователь с таким именем не существует
    // логика:
    //  - используется для валидации при регистрации новых пользователей
    //  - предотвращает создание пользователей с одинаковыми именами
    boolean existsByUsername(String username);
}