package org.example.diploma.controller;

import org.example.diploma.dto.ModuleProgressDTO;
import org.example.diploma.model.*;
import org.example.diploma.model.Module;
import org.example.diploma.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
public class StudentController {
    private final StudentService studentService;
    private final UserService userService;
    private final ExerciseService exerciseService;
    private final ModuleService moduleService;
    private final WordService wordService;

    // Конструктор StudentController - внедрение зависимостей сервисов
    // вход:
    //   - studentService - сервис для работы с функциональностью студента
    //   - userService - сервис для работы с пользователями
    //   - exerciseService - сервис для работы с упражнениями
    //   - moduleService - сервис для работы с модулями
    //   - wordService - сервис для работы со словами
    // выход: созданный экземпляр StudentController
    @Autowired
    public StudentController(StudentService studentService,
                             UserService userService,
                             ExerciseService exerciseService,
                             ModuleService moduleService,
                             WordService wordService) {
        this.studentService = studentService;
        this.userService = userService;
        this.exerciseService = exerciseService;
        this.moduleService = moduleService;
        this.wordService = wordService;
    }

    // studentDashboard - отображение главной страницы студента
    // вход:
    //   - userDetails - данные аутентифицированного пользователя
    //   - model - модель Spring MVC для передачи данных в представление
    // выход: имя представления dashboard или перенаправление на логин
    // логика:
    //  - получает пользователя по имени из userDetails
    //  - добавляет в модель доступные модули, статистику успеваемости и количество завершенных модулей
    //  - возвращает шаблон student/dashboard
    @GetMapping("/dashboard")
    public String studentDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Optional<User> userOptional = userService.findByUsername(userDetails.getUsername());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            List<Module> modules = studentService.getAvailableModulesForStudent(user.getId());

            model.addAttribute("user", user);
            model.addAttribute("modules", modules);
            model.addAttribute("successRate", studentService.getUserSuccessRate(user.getId()));
            model.addAttribute("completedModules", studentService.getCompletedModulesCount(user.getId()));

            return "student/dashboard";
        }
        return "redirect:/login";
    }

    // listModules - отображение списка доступных модулей для студента
    // вход:
    //   - userDetails - данные аутентифицированного пользователя
    //   - model - модель Spring MVC для передачи данных в представление
    // выход: имя представления со списком модулей или перенаправление на логин
    // логика:
    //  - получает студента по имени из userDetails
    //  - добавляет в модель список доступных модулей
    //  - возвращает шаблон student/modules
    @GetMapping("/modules")
    public String listModules(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Optional<User> userOptional = userService.findByUsername(userDetails.getUsername());
        if (userOptional.isPresent()) {
            User student = userOptional.get();
            List<Module> modules = studentService.getAvailableModulesForStudent(student.getId());
            model.addAttribute("modules", modules);
            return "student/modules";
        }
        return "redirect:/login";
    }

    // submitAnswer - обработка ответа студента на упражнение
    // вход:
    //   - userDetails - данные аутентифицированного пользователя
    //   - exerciseId - идентификатор упражнения
    //   - selectedAnswer - выбранный студентом ответ
    //   - timeSpent - время, затраченное на ответ (в секундах, по умолчанию 0)
    //   - nextExerciseIndex - индекс следующего упражнения (опционально)
    //   - redirectAttributes - атрибуты для перенаправления (результаты, ошибки)
    // выход: строка перенаправления на страницу практики модуля
    // логика:
    //  - сохраняет попытку ответа студента
    //  - проверяет правильность ответа
    //  - добавляет флаги для отображения результата
    // исключения:
    //  - Exception - если произошла ошибка при сохранении попытки или проверке ответа
    @PostMapping("/exercises/{exerciseId}/submit")
    public String submitAnswer(@AuthenticationPrincipal UserDetails userDetails,
                               @PathVariable Long exerciseId,
                               @RequestParam String selectedAnswer,
                               @RequestParam(defaultValue = "0") Integer timeSpent,
                               @RequestParam(required = false) Integer nextExerciseIndex,
                               RedirectAttributes redirectAttributes) {
        try {
            Optional<User> userOptional = userService.findByUsername(userDetails.getUsername());
            Optional<Exercise> exerciseOptional = exerciseService.getExerciseById(exerciseId);

            if (userOptional.isPresent() && exerciseOptional.isPresent()) {
                User user = userOptional.get();
                Exercise exercise = exerciseOptional.get();

                // сохранить попытку
                studentService.saveAttempt(user, exercise, selectedAnswer, timeSpent);

                // проверить правильность ответа
                boolean isCorrect = studentService.checkAnswer(exercise, selectedAnswer);

                // добавить флаги для отображения результата
                redirectAttributes.addFlashAttribute("showResult", true);
                redirectAttributes.addFlashAttribute("lastResult", isCorrect);
                redirectAttributes.addFlashAttribute("correctAnswer", exercise.getCorrectAnswer());
                redirectAttributes.addFlashAttribute("selectedAnswer", selectedAnswer);

                // если передан индекс следующего упражнения, перенаправить на него
                if (nextExerciseIndex != null) {
                    redirectAttributes.addAttribute("exerciseIndex", nextExerciseIndex);
                }

                return "redirect:/student/modules/" + exercise.getModule().getId() + "/practice";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error submitting answer: " + e.getMessage());
        }

        return "redirect:/student/modules";
    }

    // practiceModule - отображение упражнения для практики модуля
    // вход:
    //   - userDetails - данные аутентифицированного пользователя
    //   - id - идентификатор модуля
    //   - exerciseIndex - индекс текущего упражнения (по умолчанию 0)
    //   - model - модель Spring MVC для передачи данных в представление
    // выход: имя представления для практики или перенаправление
    // логика:
    //  - проверяет доступ студента к модулю
    //  - получает список упражнений модуля
    //  - определяет текущее упражнение по индексу
    //  - добавляет в модель данные модуля, упражнений и информацию о прогрессе
    // исключения:
    //  - возможны исключения при работе с базой данных
    @GetMapping("/modules/{id}/practice")
    public String practiceModule(@AuthenticationPrincipal UserDetails userDetails,
                                 @PathVariable Long id,
                                 @RequestParam(defaultValue = "0") Integer exerciseIndex,
                                 Model model) {
        Optional<User> userOptional = userService.findByUsername(userDetails.getUsername());
        if (userOptional.isPresent()) {
            User student = userOptional.get();

            // проверить, доступен ли модуль студенту
            List<Module> availableModules = studentService.getAvailableModulesForStudent(student.getId());
            boolean hasAccess = availableModules.stream().anyMatch(m -> m.getId().equals(id));

            if (!hasAccess) {
                return "redirect:/student/modules?error=access_denied";
            }

            Module module = moduleService.getModuleById(id).orElse(null);
            if (module != null) {
                List<Exercise> exercises = studentService.getExercisesForModule(id);

                // проверить, есть ли упражнения
                if (exercises.isEmpty()) {
                    return "redirect:/student/modules/" + id + "/words?error=no_exercises";
                }

                Exercise currentExercise = studentService.getExerciseByIndex(id, exerciseIndex);

                if (currentExercise == null) {
                    // если упражнения закончились, перенаправить на результаты
                    return "redirect:/student/results";
                }

                model.addAttribute("module", module);
                model.addAttribute("exercises", exercises);
                model.addAttribute("currentExercise", currentExercise);
                model.addAttribute("currentExerciseIndex", exerciseIndex);
                model.addAttribute("totalExercises", exercises.size());
                model.addAttribute("isLastExercise", exerciseIndex == exercises.size() - 1);

                return "student/practice";
            }
        }
        return "redirect:/student/modules";
    }

    // viewResults - отображение результатов и статистики студента
    // вход:
    //   - userDetails - данные аутентифицированного пользователя
    //   - model - модель Spring MVC для передачи данных в представление
    // выход: имя представления с результатами или перенаправление на логин
    // логика:
    //  - получает все попытки студента
    //  - подсчитывает статистику: количество правильных/неправильных ответов, уникальные модули
    //  - добавляет статистику и попытки в модель
    @GetMapping("/results")
    public String viewResults(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Optional<User> userOptional = userService.findByUsername(userDetails.getUsername());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            List<Attempt> attempts = studentService.getUserAttempts(user.getId());

            // подсчет статистики в контроллере
            long correctCount = attempts.stream().filter(Attempt::isCorrect).count();
            long incorrectCount = attempts.size() - correctCount;

            // Уникальные модули
            long uniqueModulesCount = attempts.stream()
                    .map(attempt -> attempt.getExercise().getModule().getTitle())
                    .distinct()
                    .count();

            model.addAttribute("attempts", attempts);
            model.addAttribute("correctCount", correctCount);
            model.addAttribute("incorrectCount", incorrectCount);
            model.addAttribute("uniqueModulesCount", uniqueModulesCount);
            model.addAttribute("successRate", studentService.getUserSuccessRate(user.getId()));

            return "student/results";
        }
        return "redirect:/login";
    }

    // viewModuleWords - отображение списка слов модуля
    // вход:
    //   - userDetails - данные аутентифицированного пользователя
    //   - id - идентификатор модуля
    //   - model - модель Spring MVC для передачи данных в представление
    // выход: имя представления со словами модуля или перенаправление
    // логика:
    //  - проверяет доступ студента к модулю
    //  - получает список слов модуля
    //  - добавляет модуль и слова в модель
    @GetMapping("/modules/{id}/words")
    public String viewModuleWords(@AuthenticationPrincipal UserDetails userDetails,
                                  @PathVariable Long id,
                                  Model model) {
        Optional<User> userOptional = userService.findByUsername(userDetails.getUsername());
        if (userOptional.isPresent()) {
            User student = userOptional.get();

            // проверить, доступен ли модуль студенту
            List<Module> availableModules = studentService.getAvailableModulesForStudent(student.getId());
            boolean hasAccess = availableModules.stream().anyMatch(m -> m.getId().equals(id));

            if (!hasAccess) {
                return "redirect:/student/modules?error=access_denied";
            }

            Module module = moduleService.getModuleById(id).orElse(null);
            if (module != null) {
                List<Word> words = wordService.getWordsByModuleId(id);
                model.addAttribute("module", module);
                model.addAttribute("words", words);
                return "student/words";
            }
        }
        return "redirect:/student/modules";
    }
}