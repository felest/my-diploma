package org.example.diploma.service;

import org.example.diploma.dto.*;
import org.example.diploma.model.*;
import org.example.diploma.model.Module;
import org.example.diploma.repository.*;
import org.example.diploma.repository.AttemptRepository;
import org.example.diploma.repository.ModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class StudentService {
    private final ModuleRepository moduleRepository;
    private final AttemptRepository attemptRepository;
    private final ExerciseService exerciseService;
    private final UserService userService;

    // Конструктор StudentService - внедрение зависимостей
    // вход:
    //   - moduleRepository - репозиторий для работы с модулями
    //   - attemptRepository - репозиторий для работы с попытками
    //   - exerciseService - сервис для работы с упражнениями
    //   - userService - сервис для работы с пользователями
    // выход: созданный экземпляр StudentService
    @Autowired
    public StudentService(ModuleRepository moduleRepository, AttemptRepository attemptRepository, ExerciseService exerciseService, UserService userService) {
        this.moduleRepository = moduleRepository;
        this.attemptRepository = attemptRepository;
        this.exerciseService = exerciseService;
        this.userService = userService;
    }

    // getAvailableModules - получение всех доступных модулей
    // вход: отсутствует
    // выход: список всех модулей в системе
    // примечание: временная реализация, позже будет добавлена логика доступности
    public List<Module> getAvailableModules() {
        // пока возвращаются все модули, позже добавить логику доступности
        return moduleRepository.findAll();
    }

    // getAvailableModulesForStudent - получение модулей, доступных студенту
    // вход: studentId - идентификатор студента
    // выход: список модулей, назначенных группе студента
    // логика:
    //  - если студент состоит в группе, возвращает модули, назначенные этой группе
    //  - если студент не в группе или у группы нет модулей, возвращает пустой список
    public List<Module> getAvailableModulesForStudent(Long studentId) {
        Optional<User> studentOptional = userService.findUserById(studentId);
        if (studentOptional.isPresent()) {
            User student = studentOptional.get();

            // если студент есть в группе, вернуть модули назначенные группе
            if (student.getGroup() != null && student.getGroup().getAssignedModules() != null) {
                return student.getGroup().getAssignedModules();
            }
        }
        // если студента нет в группе или у группы нет модулей, вернуть пустой список
        return new ArrayList<>();
    }

    // getExercisesForModule - получение упражнений по идентификатору модуля
    // вход: moduleId - идентификатор модуля
    // выход: список упражнений, принадлежащих указанному модулю
    public List<Exercise> getExercisesForModule(Long moduleId) {
        return exerciseService.getExercisesByModuleId(moduleId);
    }

    // checkAnswer - проверка правильности ответа на упражнение
    // вход:
    //   - exercise - упражнение для проверки
    //   - selectedAnswer - выбранный студентом ответ
    // выход:
    //   - true - если ответ правильный
    //   - false - если ответ неправильный
    public boolean checkAnswer(Exercise exercise, String selectedAnswer) {
        return exercise.getCorrectAnswer().equals(selectedAnswer);
    }

    // getExerciseByIndex - получение упражнения по индексу в модуле
    // вход:
    //   - moduleId - идентификатор модуля
    //   - exerciseIndex - индекс упражнения в списке
    // выход: упражнение по указанному индексу или null, если индекс невалидный
    public Exercise getExerciseByIndex(Long moduleId, int exerciseIndex) {
        List<Exercise> exercises = getExercisesForModule(moduleId);
        if (exercises.isEmpty() || exerciseIndex < 0 || exerciseIndex >= exercises.size()) {
            return null;
        }
        return exercises.get(exerciseIndex);
    }

    // getTotalExercisesInModule - получение общего количества упражнений в модуле
    // вход: moduleId - идентификатор модуля
    // выход: количество упражнений в модуле
    public int getTotalExercisesInModule(Long moduleId) {
        return getExercisesForModule(moduleId).size();
    }

    // getExerciseById - получение упражнения по идентификатору
    // вход: id - идентификатор упражнения
    // выход: Optional<Exercise> - упражнение, если найдено
    public Optional<Exercise> getExerciseById(Long id) {
        return exerciseService.getExerciseById(id);
    }

    // getRandomExerciseForModule - получение случайного упражнения из модуля
    // вход: moduleId - идентификатор модуля
    // выход: случайное упражнение из модуля
    // исключения:
    //  - RuntimeException - если в модуле нет упражнений
    public Exercise getRandomExerciseForModule(Long moduleId) {
        List<Exercise> exercises = getExercisesForModule(moduleId);
        if (exercises.isEmpty()) {
            throw new RuntimeException("No exercises available for this module");
        }

        Random random = new Random();
        return exercises.get(random.nextInt(exercises.size()));
    }

    // saveAttempt - сохранение попытки ответа студента
    // вход:
    //   - user - пользователь (студент)
    //   - exercise - упражнение
    //   - selectedAnswer - выбранный ответ
    //   - timeSpent - затраченное время в секундах
    // выход: сохраненный объект Attempt
    // логика:
    //  - создает новую попытку с текущим временем
    //  - проверяет правильность ответа
    //  - сохраняет попытку в базу данных
    public Attempt saveAttempt(User user, Exercise exercise, String selectedAnswer, Integer timeSpent) {
        Attempt attempt = new Attempt();
        attempt.setUser(user);
        attempt.setExercise(exercise);
        attempt.setSelectedAnswer(selectedAnswer);
        attempt.setCorrect(checkAnswer(exercise, selectedAnswer));
        attempt.setAttemptTime(LocalDateTime.now());
        attempt.setTimeSpentSeconds(timeSpent);

        return attemptRepository.save(attempt);
    }

    // getUserAttempts - получение всех попыток пользователя
    // вход: userId - идентификатор пользователя
    // выход: список попыток пользователя
    public List<Attempt> getUserAttempts(Long userId) {
        return attemptRepository.findByUserId(userId);
    }

    // getUserSuccessRate - расчет процента успешных попыток пользователя
    // вход: userId - идентификатор пользователя
    // выход: процент правильных ответов (от 0 до 100)
    // логика:
    //  - вычисляет отношение правильных попыток к общему количеству
    public Double getUserSuccessRate(Long userId) {
        Long total = attemptRepository.countTotalAttemptsByUserId(userId);
        Long correct = attemptRepository.countCorrectAttemptsByUserId(userId);

        if (total == 0) return 0.0;
        return (correct.doubleValue() / total.doubleValue()) * 100;
    }

    // getCompletedModulesCount - получение количества завершенных модулей для студента
    // вход: studentId - идентификатор студента
    // выход: количество завершенных модулей
    // логика:
    //  - модуль считается завершенным, если студент правильно ответил
    //    на все упражнения в этом модуле хотя бы один раз
    public int getCompletedModulesCount(Long studentId) {
        List<Module> availableModules = getAvailableModulesForStudent(studentId);
        int completedCount = 0;

        for (Module module : availableModules) {
            if (isModuleCompleted(studentId, module.getId())) {
                completedCount++;
            }
        }
        return completedCount;
    }

    // isModuleCompleted - проверка, завершен ли модуль для студента
    // вход:
    //   - studentId - идентификатор студента
    //   - moduleId - идентификатор модуля
    // выход:
    //   - true - если модуль завершен
    //   - false - если модуль не завершен
    // логика:
    //  - проверяет наличие правильных ответов на все упражнения модуля
    //  - если в модуле нет упражнений, возвращает false
    public boolean isModuleCompleted(Long studentId, Long moduleId) {
        List<Exercise> exercises = getExercisesForModule(moduleId);

        // если в модуле нет упражнений, он не может быть завершен
        if (exercises.isEmpty()) {
            return false;
        }

        // проверить, есть ли правильные ответы на все упражнения модуля
        for (Exercise exercise : exercises) {
            boolean hasCorrectAttempt = attemptRepository.existsByUserIdAndExerciseIdAndIsCorrectTrue(
                    studentId, exercise.getId());

            if (!hasCorrectAttempt) {
                return false;
            }
        }
        return true;
    }

    // getModuleProgress - получение прогресса по модулю в процентах
    // вход:
    //   - studentId - идентификатор студента
    //   - moduleId - идентификатор модуля
    // выход: процент завершения модуля (от 0 до 100)
    // логика:
    //  - вычисляет отношение завершенных упражнений к общему количеству упражнений в модуле
    public double getModuleProgress(Long studentId, Long moduleId) {
        List<Exercise> exercises = getExercisesForModule(moduleId);

        if (exercises.isEmpty()) {
            return 0.0;
        }

        long completedExercises = exercises.stream()
                .filter(exercise -> attemptRepository.existsByUserIdAndExerciseIdAndIsCorrectTrue(
                        studentId, exercise.getId()))
                .count();

        return (double) completedExercises / exercises.size() * 100;
    }
}
