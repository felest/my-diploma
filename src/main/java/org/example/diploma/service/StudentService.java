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

    @Autowired
    public StudentService(ModuleRepository moduleRepository, AttemptRepository attemptRepository, ExerciseService exerciseService, UserService userService) {
        this.moduleRepository = moduleRepository;
        this.attemptRepository = attemptRepository;
        this.exerciseService = exerciseService;
        this.userService = userService;
    }

    public List<Module> getAvailableModules() {
        // Пока возвращаются все модули, позже добавить логику доступности
        return moduleRepository.findAll();
    }

    public List<Module> getAvailableModulesForStudent(Long studentId) {
        Optional<User> studentOptional = userService.findUserById(studentId);
        if (studentOptional.isPresent()) {
            User student = studentOptional.get();

            // Если студент в группе, возвращаем модули назначенные группе
            if (student.getGroup() != null && student.getGroup().getAssignedModules() != null) {
                return student.getGroup().getAssignedModules();
            }
        }
        // Если студент не в группе или у группы нет модулей, возвращаем пустой список
        return new ArrayList<>();
    }

    public List<Exercise> getExercisesForModule(Long moduleId) {
        return exerciseService.getExercisesByModuleId(moduleId);
    }

    public boolean checkAnswer(Exercise exercise, String selectedAnswer) {
        return exercise.getCorrectAnswer().equals(selectedAnswer);
    }

    public Exercise getExerciseByIndex(Long moduleId, int exerciseIndex) {
        List<Exercise> exercises = getExercisesForModule(moduleId);
        if (exercises.isEmpty() || exerciseIndex < 0 || exerciseIndex >= exercises.size()) {
            return null;
        }
        return exercises.get(exerciseIndex);
    }

    public int getTotalExercisesInModule(Long moduleId) {
        return getExercisesForModule(moduleId).size();
    }

    public Optional<Exercise> getExerciseById(Long id) {
        // Вам нужно добавить этот метод в ExerciseService
        return exerciseService.getExerciseById(id);
    }

    public Exercise getRandomExerciseForModule(Long moduleId) {
        List<Exercise> exercises = getExercisesForModule(moduleId);
        if (exercises.isEmpty()) {
            throw new RuntimeException("No exercises available for this module");
        }

        Random random = new Random();
        return exercises.get(random.nextInt(exercises.size()));
    }

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

    public List<Attempt> getUserAttempts(Long userId) {
        return attemptRepository.findByUserId(userId);
    }

    public Double getUserSuccessRate(Long userId) {
        Long total = attemptRepository.countTotalAttemptsByUserId(userId);
        Long correct = attemptRepository.countCorrectAttemptsByUserId(userId);

        if (total == 0) return 0.0;
        return (correct.doubleValue() / total.doubleValue()) * 100;
    }

    /**
     * Получить количество завершенных модулей для студента
     * Модуль считается завершенным, если студент правильно ответил
     * на все упражнения в этом модуле хотя бы один раз
     */
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

    /**
     * Проверить, завершен ли модуль для студента
     */
    public boolean isModuleCompleted(Long studentId, Long moduleId) {
        List<Exercise> exercises = getExercisesForModule(moduleId);

        // Если в модуле нет упражнений, он не может быть завершен
        if (exercises.isEmpty()) {
            return false;
        }

        // Проверяем, есть ли правильные ответы на все упражнения модуля
        for (Exercise exercise : exercises) {
            boolean hasCorrectAttempt = attemptRepository.existsByUserIdAndExerciseIdAndIsCorrectTrue(
                    studentId, exercise.getId());

            if (!hasCorrectAttempt) {
                return false;
            }
        }
        return true;
    }

    /**
     * Получить прогресс по модулю в процентах
     */
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
