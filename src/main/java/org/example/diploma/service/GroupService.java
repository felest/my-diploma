package org.example.diploma.service;

import org.example.diploma.model.Group;
import org.example.diploma.model.Module;
import org.example.diploma.model.User;
import org.example.diploma.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final UserService userService;

    // Конструктор GroupService - внедрение зависимостей
    // вход:
    //   - groupRepository - репозиторий для работы с группами
    //   - userService - сервис для работы с пользователями
    // выход: созданный экземпляр GroupService
    @Autowired
    public GroupService(GroupRepository groupRepository, UserService userService) {
        this.groupRepository = groupRepository;
        this.userService = userService;
    }

    // getTeacherGroups - получение всех групп преподавателя
    // вход: teacherId - идентификатор преподавателя
    // выход: список групп, принадлежащих указанному преподавателю
    public List<Group> getTeacherGroups(Long teacherId) {
        return groupRepository.findByTeacherId(teacherId);
    }

    // getGroupById - получение группы по идентификатору
    // вход: id - идентификатор группы
    // выход: Optional<Group> - группа, если найдена
    public Optional<Group> getGroupById(Long id) {
        return groupRepository.findById(id);
    }

    // saveGroup - сохранение группы в базу данных
    // вход: group - объект группы для сохранения
    // выход: сохраненный объект группы
    public Group saveGroup(Group group) {
        return groupRepository.save(group);
    }

    // deleteGroup - удаление группы по идентификатору
    // вход: id - идентификатор группы для удаления
    // выход: void
    // логика:
    //  - удаляет группу из базы данных
    //  - студенты, привязанные к группе, остаются в системе, но теряют привязку к группе
    public void deleteGroup(Long id) {
        groupRepository.deleteById(id);
    }

    // isGroupNameUniqueForTeacher - проверка уникальности названия группы для преподавателя
    // вход:
    //   - name - название группы для проверки
    //   - teacherId - идентификатор преподавателя
    // выход:
    //   - true - если название уникально для данного преподавателя
    //   - false - если группа с таким названием уже существует у преподавателя
    // логика:
    //  - используется для валидации при создании и редактировании групп
    public boolean isGroupNameUniqueForTeacher(String name, Long teacherId) {
        return !groupRepository.existsByNameAndTeacherId(name, teacherId);
    }

    // assignModuleToGroup - назначение модуля группе
    // вход:
    //   - groupId - идентификатор группы
    //   - module - модуль для назначения группе
    // выход: void
    // логика:
    //  - добавляет модуль в список назначенных модулей группы
    //  - проверяет, что модуль еще не назначен группе
    public void assignModuleToGroup(Long groupId, Module module) {
        Optional<Group> groupOptional = getGroupById(groupId);
        if (groupOptional.isPresent()) {
            Group group = groupOptional.get();
            if (!group.getAssignedModules().contains(module)) {
                group.getAssignedModules().add(module);
                groupRepository.save(group);
            }
        }
    }

    // removeModuleFromGroup - удаление модуля из группы
    // вход:
    //   - groupId - идентификатор группы
    //   - moduleId - идентификатор модуля для удаления
    // выход: void
    // логика:
    //  - удаляет модуль из списка назначенных модулей группы
    //  - фильтрует список модулей, оставляя только те, у которых ID не совпадает с удаляемым
    public void removeModuleFromGroup(Long groupId, Long moduleId) {
        Optional<Group> groupOptional = getGroupById(groupId);
        if (groupOptional.isPresent()) {
            Group group = groupOptional.get();
            group.getAssignedModules().removeIf(module -> module.getId().equals(moduleId));
            groupRepository.save(group);
        }
    }

    // addStudentToGroup - добавление студента в группу
    // вход:
    //   - groupId - идентификатор группы
    //   - studentId - идентификатор студента для добавления
    // выход: void
    // логика:
    //  - проверяет, что пользователь существует и имеет роль STUDENT
    //  - устанавливает группу для студента
    //  - сохраняет изменения в базе данных
    public void addStudentToGroup(Long groupId, Long studentId) {
        Optional<Group> groupOptional = getGroupById(groupId);
        Optional<User> studentOptional = userService.findUserById(studentId);

        if (groupOptional.isPresent() && studentOptional.isPresent()) {
            Group group = groupOptional.get();
            User student = studentOptional.get();

            // проверить, является ли пользователь студентом
            if ("STUDENT".equals(student.getRole())) {
                student.setGroup(group);
                userService.saveUser(student);
            }
        }
    }

    // removeStudentFromGroup - удаление студента из группы
    // вход: studentId - идентификатор студента для удаления из группы
    // выход: void
    // логика:
    //  - устанавливает значение null для поля group у студента
    //  - сохраняет изменения в базе данных
    public void removeStudentFromGroup(Long studentId) {
        Optional<User> studentOptional = userService.findUserById(studentId);
        if (studentOptional.isPresent()) {
            User student = studentOptional.get();
            student.setGroup(null);
            userService.saveUser(student);
        }
    }
}
