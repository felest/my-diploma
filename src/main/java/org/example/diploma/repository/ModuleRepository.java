package org.example.diploma.repository;

import org.example.diploma.model.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ModuleRepository extends JpaRepository<Module, Long> {

    // findByUserId - поиск всех модулей по идентификатору пользователя
    // вход: userId - идентификатор пользователя (преподавателя)
    // выход: список модулей, созданных указанным пользователем
    // логика:
    //  - возвращает все модули, принадлежащие пользователю
    //  - используется для отображения модулей преподавателя в личном кабинете
    List<Module> findByUserId(Long userId);

    // existsByTitleAndUserId - проверка уникальности названия модуля для пользователя
    // вход:
    //   - title - название модуля для проверки
    //   - userId - идентификатор пользователя (преподавателя)
    // выход:
    //   - true - если модуль с таким названием уже существует у пользователя
    //   - false - если название уникально для данного пользователя
    // логика:
    //  - используется для валидации при создании и редактировании модулей
    //  - предотвращает создание дублирующихся модулей у одного преподавателя
    boolean existsByTitleAndUserId(String title, Long userId);
}