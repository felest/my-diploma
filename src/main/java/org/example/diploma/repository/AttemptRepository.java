package org.example.diploma.repository;

import org.example.diploma.dto.StudentDetailedStatsDTO;
import org.example.diploma.dto.StudentStatsDTO;
import org.example.diploma.model.Attempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {
    List<Attempt> findByUserId(Long userId);
    List<Attempt> findByUserIdAndExerciseModuleId(Long userId, Long moduleId);
    boolean existsByUserIdAndExerciseIdAndIsCorrectTrue(Long userId, Long exerciseId);

    @Query("SELECT COUNT(a) FROM Attempt a WHERE a.user.id = :userId AND a.isCorrect = true")
    Long countCorrectAttemptsByUserId(Long userId);

    @Query("SELECT COUNT(a) FROM Attempt a WHERE a.user.id = :userId")
    Long countTotalAttemptsByUserId(Long userId);

    @Query("SELECT new org.example.diploma.dto.StudentStatsDTO(" +
            "u.id, u.username, u.group.id, u.group.name, COUNT(a), SUM(CASE WHEN a.isCorrect = true THEN 1 ELSE 0 END), MAX(a.attemptTime)) " +
            "FROM User u LEFT JOIN Attempt a ON u.id = a.user.id " +
            "WHERE u.group.id = :groupId AND u.role = 'STUDENT' " +
            "GROUP BY u.id, u.username, u.group.id, u.group.name")
    List<StudentStatsDTO> findStudentStatsByGroupId(@Param("groupId") Long groupId);

    // Детальная статистика по студенту
    @Query("SELECT new org.example.diploma.dto.StudentDetailedStatsDTO(" +
            "a.id, m.title, e.question, a.selectedAnswer, e.correctAnswer, a.isCorrect, a.attemptTime, a.timeSpentSeconds) " +
            "FROM Attempt a " +
            "JOIN a.exercise e " +
            "JOIN e.module m " +
            "WHERE a.user.id = :studentId " +
            "ORDER BY a.attemptTime DESC")
    List<StudentDetailedStatsDTO> findDetailedStatsByStudentId(@Param("studentId") Long studentId);

    // метод для получения статистики всех студентов преподавателя
    @Query("SELECT new org.example.diploma.dto.StudentStatsDTO(" +
            "u.id, u.username, u.group.id, u.group.name, COUNT(a), SUM(CASE WHEN a.isCorrect = true THEN 1 ELSE 0 END), MAX(a.attemptTime)) " +
            "FROM User u LEFT JOIN Attempt a ON u.id = a.user.id " +
            "WHERE u.group.teacher.id = :teacherId AND u.role = 'STUDENT' " +
            "GROUP BY u.id, u.username, u.group.id, u.group.name")
    List<StudentStatsDTO> findStudentStatsByTeacherId(@Param("teacherId") Long teacherId);

    // Общая статистика по группе
    @Query("SELECT COUNT(a) FROM Attempt a WHERE a.user.group.id = :groupId")
    Long countTotalAttemptsByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT COUNT(a) FROM Attempt a WHERE a.user.group.id = :groupId AND a.isCorrect = true")
    Long countCorrectAttemptsByGroupId(@Param("groupId") Long groupId);

    // Статистика по модулям для студента
    @Query("SELECT COUNT(DISTINCT e.module.id) FROM Attempt a JOIN a.exercise e WHERE a.user.id = :studentId")
    Long countModulesAttemptedByStudent(@Param("studentId") Long studentId);
}
