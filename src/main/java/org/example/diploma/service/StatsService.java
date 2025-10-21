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

    @Autowired
    public StatsService(AttemptRepository attemptRepository,  GroupRepository groupRepository) {
        this.attemptRepository = attemptRepository;
        this.groupRepository = groupRepository;
    }

    public List<StudentStatsDTO> getStudentStatsByGroup(Long groupId) {
        List<StudentStatsDTO> stats = attemptRepository.findStudentStatsByGroupId(groupId);

        // Дополняем статистику информацией о модулях
        stats.forEach(stat -> {
            Long modulesAttempted = attemptRepository.countModulesAttemptedByStudent(stat.getStudentId());
            stat.setTotalModules(modulesAttempted);
            // Здесь можно добавить логику для completedModules, если нужно
            stat.setCompletedModules(modulesAttempted); // временно
        });

        return stats;
    }

    public List<StudentDetailedStatsDTO> getDetailedStatsByStudent(Long studentId) {
        return attemptRepository.findDetailedStatsByStudentId(studentId);
    }

    public GroupStatsDTO getGroupStats(Long groupId) {
        Long totalAttempts = attemptRepository.countTotalAttemptsByGroupId(groupId);
        Long correctAttempts = attemptRepository.countCorrectAttemptsByGroupId(groupId);
        Double successRate = totalAttempts > 0 ?
                Math.round((correctAttempts.doubleValue() / totalAttempts.doubleValue()) * 100.0) : 0.0;

        return new GroupStatsDTO(totalAttempts, correctAttempts, successRate);
    }

    // DTO для статистики группы
    public static class GroupStatsDTO {
        private final Long totalAttempts;
        private final Long correctAttempts;
        private final Double successRate;

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


     //Получить статистику студентов с фильтрацией по группе
    public List<StudentStatsDTO> getStudentStatsByTeacher(Long teacherId, Long groupId) {
        if (groupId != null) {
            // Фильтрация по конкретной группе
            List<StudentStatsDTO> stats = attemptRepository.findStudentStatsByGroupId(groupId);
            // Дополняем статистику информацией о модулях
            stats.forEach(stat -> {
                Long modulesAttempted = attemptRepository.countModulesAttemptedByStudent(stat.getStudentId());
                stat.setTotalModules(modulesAttempted);
                stat.setCompletedModules(modulesAttempted); // временно
            });
            return stats;
        } else {
            // Все студенты преподавателя
            List<StudentStatsDTO> stats = attemptRepository.findStudentStatsByTeacherId(teacherId);
            // Дополняем статистику информацией о модулях
            stats.forEach(stat -> {
                Long modulesAttempted = attemptRepository.countModulesAttemptedByStudent(stat.getStudentId());
                stat.setTotalModules(modulesAttempted);
                stat.setCompletedModules(modulesAttempted); // временно
            });
            return stats;
        }
    }


     // Получить все группы преподавателя для фильтра
    public List<Group> getTeacherGroups(Long teacherId) {
        return groupRepository.findByTeacherId(teacherId);
    }
}
