package org.example.diploma.repository;

import org.example.diploma.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {

    // findByTeacherId - поиск всех групп по идентификатору преподавателя
    // вход: teacherId - идентификатор преподавателя
    // выход: список групп, принадлежащих указанному преподавателю
    // логика:
    //  - возвращает все группы, созданные преподавателем
    List<Group> findByTeacherId(Long teacherId);

    // findGroupsByModuleId - поиск групп, которым назначен указанный модуль
    // вход: moduleId - идентификатор модуля
    // выход: список групп, в которые назначен указанный модуль
    // логика:
    //  - выполняет JOIN между таблицами Group и assignedModules
    //  - возвращает группы, связанные с модулем через отношение many-to-many
    @Query("SELECT g FROM Group g JOIN g.assignedModules m WHERE m.id = :moduleId")
    List<Group> findGroupsByModuleId(Long moduleId);

    // existsByNameAndTeacherId - проверка уникальности названия группы для преподавателя
    // вход:
    //   - name - название группы для проверки
    //   - teacherId - идентификатор преподавателя
    // выход:
    //   - true - если группа с таким названием уже существует у преподавателя
    //   - false - если название уникально для данного преподавателя
    // логика:
    //  - используется для валидации при создании новых групп
    boolean existsByNameAndTeacherId(String name, Long teacherId);
}