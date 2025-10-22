package org.example.diploma.service;

import org.example.diploma.dto.StudentStatsDTO;
import org.example.diploma.dto.StudentDetailedStatsDTO;
import org.example.diploma.model.Group;
import org.example.diploma.repository.AttemptRepository;
import org.example.diploma.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StatsService {
    private final AttemptRepository attemptRepository;
    private final GroupRepository groupRepository;

    // Конструктор StatsService - внедрение зависимостей
    // вход:
    //   - attemptRepository - репозиторий для работы с попытками
    //   - groupRepository - репозиторий для работы с группами
    // выход: созданный экземпляр StatsService
    @Autowired
    public StatsService(AttemptRepository attemptRepository,  GroupRepository groupRepository) {
        this.attemptRepository = attemptRepository;
        this.groupRepository = groupRepository;
    }

    // getStudentStatsByGroup - получение статистики студентов по группе
    // вход: groupId - идентификатор группы
    // выход: список DTO со статистикой студентов группы
    // логика:
    //  - получает базовую статистику студентов из репозитория
    //  - дополняет статистику информацией о количестве модулей, в которых студент делал попытки
    public List<StudentStatsDTO> getStudentStatsByGroup(Long groupId) {
        List<StudentStatsDTO> stats = attemptRepository.findStudentStatsByGroupId(groupId);

        // дополнить статистику информацией о модулях
        stats.forEach(stat -> {
            Long modulesAttempted = attemptRepository.countModulesAttemptedByStudent(stat.getStudentId());
            stat.setTotalModules(modulesAttempted);
            // здесь можно добавить логику для completedModules, если нужно
            stat.setCompletedModules(modulesAttempted); // временно
        });

        return stats;
    }

    // getDetailedStatsByStudent - получение детальной статистики по студенту
    // вход: studentId - идентификатор студента
    // выход: список DTO с детальной статистикой попыток студента
    // логика:
    //  - возвращает подробную информацию о каждой попытке студента
    //  - включает данные о модулях, вопросах, ответах и времени
    public List<StudentDetailedStatsDTO> getDetailedStatsByStudent(Long studentId) {
        return attemptRepository.findDetailedStatsByStudentId(studentId);
    }

    // getGroupStats - получение общей статистики по группе
    // вход: groupId - идентификатор группы
    // выход: DTO со статистикой группы
    // логика:
    //  - подсчитывает общее количество попыток в группе
    //  - подсчитывает количество правильных попыток
    //  - вычисляет процент успешности (success rate)
    public GroupStatsDTO getGroupStats(Long groupId) {
        Long totalAttempts = attemptRepository.countTotalAttemptsByGroupId(groupId);
        Long correctAttempts = attemptRepository.countCorrectAttemptsByGroupId(groupId);
        Double successRate = totalAttempts > 0 ?
                Math.round((correctAttempts.doubleValue() / totalAttempts.doubleValue()) * 100.0) : 0.0;

        return new GroupStatsDTO(totalAttempts, correctAttempts, successRate);
    }

    // GroupStatsDTO - DTO для статистики группы
    // назначение: передача агрегированной статистики по группе
    // содержит:
    //   - totalAttempts - общее количество попыток в группе
    //   - correctAttempts - количество правильных попыток
    //   - successRate - процент успешных попыток
    public static class GroupStatsDTO {
        private final Long totalAttempts;
        private final Long correctAttempts;
        private final Double successRate;

        // Конструктор GroupStatsDTO
        // вход:
        //   - totalAttempts - общее количество попыток
        //   - correctAttempts - количество правильных попыток
        //   - successRate - процент успешности
        public GroupStatsDTO(Long totalAttempts, Long correctAttempts, Double successRate) {
            this.totalAttempts = totalAttempts;
            this.correctAttempts = correctAttempts;
            this.successRate = successRate;
        }

        // геттеры
        public Long getTotalAttempts() { return totalAttempts; }
        public Long getCorrectAttempts() { return correctAttempts; }
        public Double getSuccessRate() { return successRate; }
    }

    // getStudentStatsByTeacher - получение статистики студентов преподавателя с фильтрацией по группе
    // вход:
    //   - teacherId - идентификатор преподавателя
    //   - groupId - идентификатор группы для фильтрации (может быть null)
    // выход: список DTO со статистикой студентов
    // логика:
    //  - если groupId указан, возвращает статистику студентов только этой группы
    //  - если groupId не указан, возвращает статистику всех студентов преподавателя
    //  - дополняет статистику информацией о модулях
    public List<StudentStatsDTO> getStudentStatsByTeacher(Long teacherId, Long groupId) {
        if (groupId != null) {
            // фильтрация по конкретной группе
            List<StudentStatsDTO> stats = attemptRepository.findStudentStatsByGroupId(groupId);
            // дополнить статистику информацией о модулях
            stats.forEach(stat -> {
                Long modulesAttempted = attemptRepository.countModulesAttemptedByStudent(stat.getStudentId());
                stat.setTotalModules(modulesAttempted);
                stat.setCompletedModules(modulesAttempted); // временно
            });
            return stats;
        } else {
            // все студенты преподавателя
            List<StudentStatsDTO> stats = attemptRepository.findStudentStatsByTeacherId(teacherId);
            // дополнить статистику информацией о модулях
            stats.forEach(stat -> {
                Long modulesAttempted = attemptRepository.countModulesAttemptedByStudent(stat.getStudentId());
                stat.setTotalModules(modulesAttempted);
                stat.setCompletedModules(modulesAttempted); // временно
            });
            return stats;
        }
    }

    // getTeacherGroups - получение всех групп преподавателя для фильтра
    // вход: teacherId - идентификатор преподавателя
    // выход: список групп, принадлежащих преподавателю
    // логика:
    //  - используется для построения выпадающего списка групп в интерфейсе
    public List<Group> getTeacherGroups(Long teacherId) {
        return groupRepository.findByTeacherId(teacherId);
    }
}
