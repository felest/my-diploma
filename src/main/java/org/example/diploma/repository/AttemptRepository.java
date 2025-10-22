package org.example.diploma.repository;

import org.example.diploma.dto.StudentDetailedStatsDTO;
import org.example.diploma.dto.StudentStatsDTO;
import org.example.diploma.model.Attempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {

    // findByUserId - поиск всех попыток пользователя
    // вход: userId - идентификатор пользователя
    // выход: список попыток пользователя
    List<Attempt> findByUserId(Long userId);

    // findByUserIdAndExerciseModuleId - поиск попыток пользователя по конкретному модулю
    // вход:
    //   - userId - идентификатор пользователя
    //   - moduleId - идентификатор модуля
    // выход: список попыток пользователя в указанном модуле
    List<Attempt> findByUserIdAndExerciseModuleId(Long userId, Long moduleId);

    // existsByUserIdAndExerciseIdAndIsCorrectTrue - проверка существования правильной попытки
    // вход:
    //   - userId - идентификатор пользователя
    //   - exerciseId - идентификатор упражнения
    // выход: true - если у пользователя есть правильная попытка для данного упражнения, иначе false
    boolean existsByUserIdAndExerciseIdAndIsCorrectTrue(Long userId, Long exerciseId);

    // countCorrectAttemptsByUserId - подсчет количества правильных попыток пользователя
    // вход: userId - идентификатор пользователя
    // выход: количество правильных попыток пользователя
    @Query("SELECT COUNT(a) FROM Attempt a WHERE a.user.id = :userId AND a.isCorrect = true")
    Long countCorrectAttemptsByUserId(Long userId);

    // countTotalAttemptsByUserId - подсчет общего количества попыток пользователя
    // вход: userId - идентификатор пользователя
    // выход: общее количество попыток пользователя
    @Query("SELECT COUNT(a) FROM Attempt a WHERE a.user.id = :userId")
    Long countTotalAttemptsByUserId(Long userId);

    // findStudentStatsByGroupId - получение статистики студентов по группе
    // вход: groupId - идентификатор группы
    // выход: список DTO со статистикой студентов группы
    // возвращаемые данные для каждого студента:
    //   - идентификатор студента
    //   - имя пользователя
    //   - идентификатор группы
    //   - название группы
    //   - общее количество попыток
    //   - количество правильных попыток
    //   - время последней попытки
    @Query("SELECT new org.example.diploma.dto.StudentStatsDTO(" +
            "u.id, u.username, u.group.id, u.group.name, COUNT(a), SUM(CASE WHEN a.isCorrect = true THEN 1 ELSE 0 END), MAX(a.attemptTime)) " +
            "FROM User u LEFT JOIN Attempt a ON u.id = a.user.id " +
            "WHERE u.group.id = :groupId AND u.role = 'STUDENT' " +
            "GROUP BY u.id, u.username, u.group.id, u.group.name")
    List<StudentStatsDTO> findStudentStatsByGroupId(@Param("groupId") Long groupId);

    // findDetailedStatsByStudentId - получение детальной статистики по студенту
    // вход: studentId - идентификатор студента
    // выход: список DTO с детальной статистикой попыток студента
    // возвращаемые данные для каждой попытки:
    //   - идентификатор попытки
    //   - название модуля
    //   - вопрос упражнения
    //   - выбранный ответ
    //   - правильный ответ
    //   - флаг правильности
    //   - время попытки
    //   - затраченное время в секундах
    @Query("SELECT new org.example.diploma.dto.StudentDetailedStatsDTO(" +
            "a.id, m.title, e.question, a.selectedAnswer, e.correctAnswer, a.isCorrect, a.attemptTime, a.timeSpentSeconds) " +
            "FROM Attempt a " +
            "JOIN a.exercise e " +
            "JOIN e.module m " +
            "WHERE a.user.id = :studentId " +
            "ORDER BY a.attemptTime DESC")
    List<StudentDetailedStatsDTO> findDetailedStatsByStudentId(@Param("studentId") Long studentId);

    // findStudentStatsByTeacherId - получение статистики студентов преподавателя
    // вход: teacherId - идентификатор преподавателя
    // выход: список DTO со статистикой студентов всех групп преподавателя
    // возвращаемые данные для каждого студента:
    //   - идентификатор студента
    //   - имя пользователя
    //   - идентификатор группы
    //   - название группы
    //   - общее количество попыток
    //   - количество правильных попыток
    //   - время последней попытки
    @Query("SELECT new org.example.diploma.dto.StudentStatsDTO(" +
            "u.id, u.username, u.group.id, u.group.name, COUNT(a), SUM(CASE WHEN a.isCorrect = true THEN 1 ELSE 0 END), MAX(a.attemptTime)) " +
            "FROM User u LEFT JOIN Attempt a ON u.id = a.user.id " +
            "WHERE u.group.teacher.id = :teacherId AND u.role = 'STUDENT' " +
            "GROUP BY u.id, u.username, u.group.id, u.group.name")
    List<StudentStatsDTO> findStudentStatsByTeacherId(@Param("teacherId") Long teacherId);

    // countTotalAttemptsByGroupId - подсчет общего количества попыток в группе
    // вход: groupId - идентификатор группы
    // выход: общее количество попыток всех студентов группы
    @Query("SELECT COUNT(a) FROM Attempt a WHERE a.user.group.id = :groupId")
    Long countTotalAttemptsByGroupId(@Param("groupId") Long groupId);

    // countCorrectAttemptsByGroupId - подсчет количества правильных попыток в группе
    // вход: groupId - идентификатор группы
    // выход: количество правильных попыток всех студентов группы
    @Query("SELECT COUNT(a) FROM Attempt a WHERE a.user.group.id = :groupId AND a.isCorrect = true")
    Long countCorrectAttemptsByGroupId(@Param("groupId") Long groupId);

    // countModulesAttemptedByStudent - подсчет количества модулей, в которых студент делал попытки
    // вход: studentId - идентификатор студента
    // выход: количество уникальных модулей, в которых студент выполнял упражнения
    @Query("SELECT COUNT(DISTINCT e.module.id) FROM Attempt a JOIN a.exercise e WHERE a.user.id = :studentId")
    Long countModulesAttemptedByStudent(@Param("studentId") Long studentId);
}