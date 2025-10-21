package org.example.diploma.controller;

import org.example.diploma.model.*;
import org.example.diploma.model.Module;
import org.example.diploma.service.ExerciseService;
import org.example.diploma.service.ModuleService;
import org.example.diploma.service.UserService;
import org.example.diploma.service.WordService;
import org.example.diploma.dto.StudentStatsDTO;
import org.example.diploma.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/teacher")
public class DashboardController {
    private final UserService userService;
    private final ModuleService moduleService;
    private final WordService wordService;
    private final ExerciseService exerciseService;

    // Конструктор DashboardController - внедрение зависимостей сервисов
    // вход:
    //   - userService - сервис для работы с пользователями
    //   - moduleService - сервис для работы с модулями
    //   - wordService - сервис для работы со словами
    //   - exerciseService - сервис для работы с упражнениями
    // выход: созданный экземпляр DashboardController
    @Autowired
    public DashboardController(UserService userService, ModuleService moduleService, WordService wordService, ExerciseService exerciseService) {
        this.userService = userService;
        this.moduleService = moduleService;
        this.wordService = wordService;
        this.exerciseService = exerciseService;
    }

    @Autowired
    private StatsService statsService;

    // teacherDashboard - отображение главной страницы преподавателя
    // вход:
    //   - userDetails - данные аутентифицированного пользователя
    //   - model - модель Spring MVC для передачи данных в представление
    // выход: имя представления dashboard или перенаправление на логин
    // логика:
    //  - получает пользователя по имени из userDetails
    //  - добавляет в модель модули пользователя
    //  - возвращает шаблон teacher/dashboard
    @GetMapping("/dashboard")
    public String teacherDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Optional<User> userOptional = userService.findByUsername(userDetails.getUsername());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            model.addAttribute("modules", moduleService.getUserModules(user.getId()));
            return "teacher/dashboard";
        }
        return "redirect:/login";
    }

    // listModules - отображение списка модулей преподавателя
    // вход:
    //   - userDetails - данные аутентифицированного пользователя
    //   - model - модель Spring MVC для передачи данных в представление
    // выход: имя представления со списком модулей или перенаправление на логин
    // логика:
    //  - получает пользователя по имени из userDetails
    //  - добавляет в модель модули пользователя
    //  - возвращает шаблон teacher/modules
    @GetMapping("/modules")
    public String listModules(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Optional<User> userOptional = userService.findByUsername(userDetails.getUsername());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            model.addAttribute("modules", moduleService.getUserModules(user.getId()));
            return "teacher/modules";
        }
        return "redirect:/login";
    }

    // showCreateModuleForm - отображение формы создания нового модуля
    // вход: model - модель Spring MVC для передачи данных в представление
    // выход: имя представления формы создания модуля
    // логика:
    //  - добавляет в модель новый пустой объект Module
    //  - возвращает шаблон teacher/module-form
    @GetMapping("/modules/new")
    public String showCreateModuleForm(Model model) {
        model.addAttribute("module", new Module());
        return "teacher/module-form";
    }

    // createModule - создание нового модуля
    // вход:
    //   - userDetails - данные аутентифицированного пользователя
    //   - module - объект модуля с данными из формы
    //   - redirectAttributes - атрибуты для перенаправления (сообщения об успехе/ошибке)
    // выход: строка перенаправления на список модулей или форму создания
    // логика:
    //  - проверяет уникальность названия модуля для пользователя
    //  - сохраняет модуль в базу данных
    // исключения:
    //  - возможны исключения при работе с базой данных
    @PostMapping("/modules")
    public String createModule(@AuthenticationPrincipal UserDetails userDetails,
                               @ModelAttribute Module module,
                               RedirectAttributes redirectAttributes) {
        Optional<User> userOptional = userService.findByUsername(userDetails.getUsername());
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Проверяем уникальность названия модуля для данного пользователя
            if (!moduleService.isModuleTitleUniqueForUser(module.getTitle(), user.getId())) {
                redirectAttributes.addFlashAttribute("error", "Module with this title already exists");
                return "redirect:/teacher/modules/new";
            }

            module.setUser(user);
            moduleService.saveModule(module);
            redirectAttributes.addFlashAttribute("success", "Module created successfully");
            return "redirect:/teacher/modules";
        }
        return "redirect:/login";
    }

    // showEditModuleForm - отображение формы редактирования модуля
    // вход:
    //   - id - идентификатор модуля для редактирования
    //   - model - модель Spring MVC для передачи данных в представление
    // выход: имя представления формы редактирования или перенаправление на список модулей
    // логика:
    //  - получает модуль по идентификатору
    //  - добавляет в модель модуль и список слов модуля
    //  - возвращает шаблон teacher/module-edit
    @GetMapping("/modules/{id}/edit")
    public String showEditModuleForm(@PathVariable Long id, Model model) {
        Optional<Module> moduleOptional = moduleService.getModuleById(id);
        if (moduleOptional.isPresent()) {
            Module module = moduleOptional.get();
            model.addAttribute("module", module);
            model.addAttribute("words", wordService.getWordsByModuleId(id));
            return "teacher/module-edit";
        }
        return "redirect:/teacher/modules";
    }

    // updateModule - обновление данных модуля
    // вход:
    //   - id - идентификатор модуля для обновления
    //   - module - объект модуля с обновленными данными из формы
    //   - redirectAttributes - атрибуты для перенаправления (сообщения об успехе)
    // выход: строка перенаправления на список модулей
    // логика:
    //  - находит существующий модуль по идентификатору
    //  - обновляет заголовок и описание модуля
    //  - сохраняет изменения в базу данных
    @PostMapping("/modules/{id}")
    public String updateModule(@PathVariable Long id,
                               @ModelAttribute Module module,
                               RedirectAttributes redirectAttributes) {
        Optional<Module> existingModuleOptional = moduleService.getModuleById(id);
        if (existingModuleOptional.isPresent()) {
            Module existingModule = existingModuleOptional.get();
            existingModule.setTitle(module.getTitle());
            existingModule.setDescription(module.getDescription());
            moduleService.saveModule(existingModule);
            redirectAttributes.addFlashAttribute("success", "Module updated successfully");
        }
        return "redirect:/teacher/modules";
    }

    // deleteModule - удаление модуля
    // вход:
    //   - id - идентификатор модуля для удаления
    //   - redirectAttributes - атрибуты для перенаправления (сообщения об успехе)
    // выход: строка перенаправления на список модулей
    // логика:
    //  - удаляет модуль и все связанные с ним слова
    // исключения:
    //  - возможны исключения при работе с базой данных
    @PostMapping("/modules/{id}/delete")
    public String deleteModule(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        moduleService.deleteModule(id);
        wordService.deleteWordsByModuleId(id);
        redirectAttributes.addFlashAttribute("success", "Module deleted successfully");
        return "redirect:/teacher/modules";
    }

    // addWordToModule - добавление слова в модуль
    // вход:
    //   - moduleId - идентификатор модуля
    //   - english - английское слово
    //   - russian - русский перевод
    //   - redirectAttributes - атрибуты для перенаправления (сообщения об успехе)
    // выход: строка перенаправления на форму редактирования модуля
    // логика:
    //  - создает новое слово и связывает его с модулем
    //  - сохраняет слово в базу данных
    @PostMapping("/modules/{moduleId}/words")
    public String addWordToModule(@PathVariable Long moduleId,
                                  @RequestParam String english,
                                  @RequestParam String russian,
                                  RedirectAttributes redirectAttributes) {
        Optional<Module> moduleOptional = moduleService.getModuleById(moduleId);
        if (moduleOptional.isPresent()) {
            Module module = moduleOptional.get();
            Word word = new Word();
            word.setEnglish(english);
            word.setRussian(russian);
            word.setModule(module);
            wordService.saveWord(word);
            redirectAttributes.addFlashAttribute("success", "Word added successfully");
        }
        return "redirect:/teacher/modules/" + moduleId + "/edit";
    }

    // deleteWord - удаление слова из модуля
    // вход:
    //   - id - идентификатор слова для удаления
    //   - redirectAttributes - атрибуты для перенаправления (сообщения об успехе)
    // выход: строка перенаправления на форму редактирования модуля
    // логика:
    //  - удаляет слово по идентификатору
    //  - перенаправляет на страницу редактирования модуля
    @PostMapping("/words/{id}/delete")
    public String deleteWord(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Word> wordOptional = wordService.getWordById(id);
        if (wordOptional.isPresent()) {
            Word word = wordOptional.get();
            Long moduleId = word.getModule().getId();
            wordService.deleteWord(id);
            redirectAttributes.addFlashAttribute("success", "Word deleted successfully");
            return "redirect:/teacher/modules/" + moduleId + "/edit";
        }
        return "redirect:/teacher/modules";
    }

    //методы для управления упражнениями

    // showModuleExercises - отображение упражнений модуля
    // вход:
    //   - id - идентификатор модуля
    //   - model - модель Spring MVC для передачи данных в представление
    // выход: имя представления со списком упражнений или перенаправление с ошибкой
    // логика:
    //  - получает модуль и связанные с ним упражнения
    //  - добавляет данные в модель
    // исключения:
    //  - Exception - если произошла ошибка при загрузке упражнений
    @GetMapping("/modules/{id}/exercises")
    public String showModuleExercises(@PathVariable Long id, Model model) {
        try {
            Optional<Module> moduleOptional = moduleService.getModuleById(id);
            if (moduleOptional.isPresent()) {
                Module module = moduleOptional.get();
                List<Exercise> exercises = exerciseService.getExercisesByModuleId(id);
                model.addAttribute("module", module);
                model.addAttribute("exercises", exercises);
                return "teacher/module-exercises";
            } else {
                return "redirect:/teacher/modules?error=Module not found";
            }
        } catch (Exception e) {
            return "redirect:/teacher/modules?error=Error loading exercises: " + e.getMessage();
        }
    }

    // generateExercises - генерация упражнений для модуля
    // вход:
    //   - id - идентификатор модуля
    //   - redirectAttributes - атрибуты для перенаправления (сообщения об успехе/ошибке)
    // выход: строка перенаправления на страницу упражнений модуля
    // логика:
    //  - вызывает сервис для генерации упражнений на основе слов модуля
    // исключения:
    //  - Exception - если произошла ошибка при генерации упражнений
    @PostMapping("/modules/{id}/generate-exercises")
    public String generateExercises(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            moduleService.generateExercisesForModule(id);
            redirectAttributes.addFlashAttribute("success", "Exercises generated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error generating exercises: " + e.getMessage());
        }
        return "redirect:/teacher/modules/" + id + "/exercises";
    }

    // deleteExercise - удаление упражнения
    // вход:
    //   - id - идентификатор упражнения для удаления
    //   - redirectAttributes - атрибуты для перенаправления (сообщения об успехе)
    // выход: строка перенаправления на страницу упражнений модуля
    // логика:
    //  - удаляет упражнение по идентификатору
    @PostMapping("/exercises/{id}/delete")
    public String deleteExercise(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Exercise> exerciseOptional = exerciseService.getExerciseById(id);
        if (exerciseOptional.isPresent()) {
            Exercise exercise = exerciseOptional.get();
            Long moduleId = exercise.getModule().getId();
            exerciseService.deleteExercise(id);
            redirectAttributes.addFlashAttribute("success", "Exercise deleted successfully");
            return "redirect:/teacher/modules/" + moduleId + "/exercises";
        }
        return "redirect:/teacher/modules";
    }

    //для отображения ошибок
    // handleException - обработчик исключений для контроллера
    // вход:
    //   - e - исключение, которое произошло
    //   - model - модель Spring MVC для передачи данных в представление
    // выход: имя представления для отображения ошибки
    // логика:
    //  - добавляет сообщение об ошибке в модель
    //  - возвращает шаблон error
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        model.addAttribute("error", "An error occurred: " + e.getMessage());
        return "error";
    }

     //Страница результатов студентов
     // showResults - отображение результатов студентов
     // вход:
     //   - userDetails - данные аутентифицированного пользователя
     //   - groupId - идентификатор группы для фильтрации (опционально)
     //   - model - модель Spring MVC для передачи данных в представление
     // выход: имя представления с результатами или перенаправление на логин
     // логика:
     //  - получает статистику студентов по преподавателю
     //  - получает список групп преподавателя для фильтра
     //  - добавляет данные в модель
    @GetMapping("/results")
    public String showResults(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam(required = false) Long groupId,
                              Model model) {
        Optional<User> userOptional = userService.findByUsername(userDetails.getUsername());
        if (userOptional.isPresent()) {
            User teacher = userOptional.get();

            // Получить статистику студентов
            List<StudentStatsDTO> studentStats = statsService.getStudentStatsByTeacher(teacher.getId(), groupId);

            // Получить группы преподавателя для фильтра
            List<Group> teacherGroups = statsService.getTeacherGroups(teacher.getId());

            model.addAttribute("studentStats", studentStats);
            model.addAttribute("teacherGroups", teacherGroups);
            model.addAttribute("selectedGroupId", groupId);

            return "teacher/results";
        }
        return "redirect:/login";
    }
}

