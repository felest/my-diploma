package org.example.diploma.controller;

import org.example.diploma.dto.StudentDetailedStatsDTO;
import org.example.diploma.dto.StudentStatsDTO;
import org.example.diploma.model.Group;
import org.example.diploma.model.Module;
import org.example.diploma.model.User;
import org.example.diploma.service.GroupService;
import org.example.diploma.service.ModuleService;
import org.example.diploma.service.StatsService;
import org.example.diploma.service.UserService;
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
@RequestMapping("/teacher/groups")
public class GroupController {
    private final GroupService groupService;
    private final UserService userService;
    private final ModuleService moduleService;
    private final StatsService statsService;

    // Конструктор GroupController - внедрение зависимостей сервисов
    // вход:
    //   - groupService - сервис для работы с группами
    //   - userService - сервис для работы с пользователями
    //   - moduleService - сервис для работы с модулями
    //   - statsService - сервис для работы со статистикой
    // выход: созданный экземпляр GroupController
    @Autowired
    public GroupController(GroupService groupService, UserService userService,
                           ModuleService moduleService, StatsService statsService) {
        this.groupService = groupService;
        this.userService = userService;
        this.moduleService = moduleService;
        this.statsService = statsService;
    }

    // listGroups - отображение списка групп преподавателя
    // вход:
    //   - userDetails - данные аутентифицированного пользователя
    //   - model - модель Spring MVC для передачи данных в представление
    // выход: имя представления со списком групп или перенаправление на логин/ошибку
    // логика:
    //  - получает преподавателя по имени из userDetails
    //  - добавляет в модель список групп преподавателя
    // исключения:
    //  - Exception - если произошла ошибка при загрузке групп
    @GetMapping
    public String listGroups(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            Optional<User> teacherOptional = userService.findByUsername(userDetails.getUsername());
            if (teacherOptional.isPresent()) {
                User teacher = teacherOptional.get();
                model.addAttribute("groups", groupService.getTeacherGroups(teacher.getId()));
                return "teacher/groups";
            }
            return "redirect:/login";
        } catch (Exception e) {
            // добавить информацию об ошибке в модель для отладки
            model.addAttribute("error", "Error loading groups: " + e.getMessage());
            return "error";
        }
    }

    // showCreateGroupForm - отображение формы создания новой группы
    // вход: model - модель Spring MVC для передачи данных в представление
    // выход: имя представления формы создания группы
    // логика:
    //  - добавляет в модель новый пустой объект Group
    //  - возвращает шаблон teacher/group-form
    @GetMapping("/new")
    public String showCreateGroupForm(Model model) {
        model.addAttribute("group", new Group());
        return "teacher/group-form";
    }

    // createGroup - создание новой группы
    // вход:
    //   - userDetails - данные аутентифицированного пользователя
    //   - group - объект группы с данными из формы
    //   - redirectAttributes - атрибуты для перенаправления (сообщения об успехе/ошибке)
    // выход: строка перенаправления на список групп
    // логика:
    //  - проверяет уникальность названия группы для преподавателя
    //  - сохраняет группу в базу данных
    @PostMapping
    public String createGroup(@AuthenticationPrincipal UserDetails userDetails,
                              @ModelAttribute Group group,
                              RedirectAttributes redirectAttributes) {
        Optional<User> teacherOptional = userService.findByUsername(userDetails.getUsername());
        if (teacherOptional.isPresent()) {
            User teacher = teacherOptional.get();

            if (!groupService.isGroupNameUniqueForTeacher(group.getName(), teacher.getId())) {
                redirectAttributes.addFlashAttribute("error", "Group with this name already exists");
                return "redirect:/teacher/groups/new";
            }

            group.setTeacher(teacher);
            groupService.saveGroup(group);
            redirectAttributes.addFlashAttribute("success", "Group created successfully");
        }
        return "redirect:/teacher/groups";
    }

    // viewGroup - просмотр детальной информации о группе
    // вход:
    //   - id - идентификатор группы
    //   - model - модель Spring MVC для передачи данных в представление
    // выход: имя представления с деталями группы или перенаправление на список групп
    // логика:
    //  - получает группу по идентификатору
    //  - добавляет в модель группу, список студентов группы, доступных студентов и модули преподавателя
    @GetMapping("/{id}")
    public String viewGroup(@PathVariable Long id, Model model) {
        Optional<Group> groupOptional = groupService.getGroupById(id);
        if (groupOptional.isPresent()) {
            Group group = groupOptional.get();
            model.addAttribute("group", group);
            model.addAttribute("students", userService.findByGroupId(id));
            model.addAttribute("availableStudents", userService.findStudentsWithoutGroup());
            model.addAttribute("teacherModules", moduleService.getUserModules(group.getTeacher().getId()));
            return "teacher/group-details";
        }
        return "redirect:/teacher/groups";
    }

    // addStudentToGroup - добавление студента в группу
    // вход:
    //   - groupId - идентификатор группы
    //   - studentId - идентификатор студента для добавления
    //   - redirectAttributes - атрибуты для перенаправления (сообщения об успехе)
    // выход: строка перенаправления на страницу группы
    // логика:
    //  - добавляет студента в указанную группу через groupService
    @PostMapping("/{groupId}/students")
    public String addStudentToGroup(@PathVariable Long groupId,
                                    @RequestParam Long studentId,
                                    RedirectAttributes redirectAttributes) {
        groupService.addStudentToGroup(groupId, studentId);
        redirectAttributes.addFlashAttribute("success", "Student added to group");
        return "redirect:/teacher/groups/" + groupId;
    }

    // removeStudentFromGroup - удаление студента из группы
    // вход:
    //   - groupId - идентификатор группы
    //   - studentId - идентификатор студента для удаления
    //   - redirectAttributes - атрибуты для перенаправления (сообщения об успехе)
    // выход: строка перенаправления на страницу группы
    // логика:
    //  - удаляет студента из группы через groupService
    @PostMapping("/{groupId}/students/{studentId}/remove")
    public String removeStudentFromGroup(@PathVariable Long groupId,
                                         @PathVariable Long studentId,
                                         RedirectAttributes redirectAttributes) {
        groupService.removeStudentFromGroup(studentId);
        redirectAttributes.addFlashAttribute("success", "Student removed from group");
        return "redirect:/teacher/groups/" + groupId;
    }

    // assignModuleToGroup - назначение модуля группе
    // вход:
    //   - groupId - идентификатор группы
    //   - moduleId - идентификатор модуля для назначения
    //   - redirectAttributes - атрибуты для перенаправления (сообщения об успехе)
    // выход: строка перенаправления на страницу группы
    // логика:
    //  - назначает модуль группе через groupService
    @PostMapping("/{groupId}/modules")
    public String assignModuleToGroup(@PathVariable Long groupId,
                                      @RequestParam Long moduleId,
                                      RedirectAttributes redirectAttributes) {
        Optional<Module> moduleOptional = moduleService.getModuleById(moduleId);
        if (moduleOptional.isPresent()) {
            groupService.assignModuleToGroup(groupId, moduleOptional.get());
            redirectAttributes.addFlashAttribute("success", "Module assigned to group");
        }
        return "redirect:/teacher/groups/" + groupId;
    }

    // removeModuleFromGroup - удаление модуля из группы
    // вход:
    //   - groupId - идентификатор группы
    //   - moduleId - идентификатор модуля для удаления
    //   - redirectAttributes - атрибуты для перенаправления (сообщения об успехе)
    // выход: строка перенаправления на страницу группы
    // логика:
    //  - удаляет модуль из группы через groupService
    @PostMapping("/{groupId}/modules/{moduleId}/remove")
    public String removeModuleFromGroup(@PathVariable Long groupId,
                                        @PathVariable Long moduleId,
                                        RedirectAttributes redirectAttributes) {
        groupService.removeModuleFromGroup(groupId, moduleId);
        redirectAttributes.addFlashAttribute("success", "Module removed from group");
        return "redirect:/teacher/groups/" + groupId;
    }

    // deleteGroup - удаление группы
    // вход:
    //   - id - идентификатор группы для удаления
    //   - redirectAttributes - атрибуты для перенаправления (сообщения об успехе)
    // выход: строка перенаправления на список групп
    // логика:
    //  - удаляет группу через groupService
    @PostMapping("/{id}/delete")
    public String deleteGroup(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        groupService.deleteGroup(id);
        redirectAttributes.addFlashAttribute("success", "Group deleted successfully");
        return "redirect:/teacher/groups";
    }

    // viewGroupStats - просмотр статистики группы
    // вход:
    //   - id - идентификатор группы
    //   - model - модель Spring MVC для передачи данных в представление
    // выход: имя представления со статистикой группы или перенаправление на список групп
    // логика:
    //  - получает статистику студентов группы и общую статистику группы
    //  - добавляет данные в модель
    @GetMapping("/{id}/stats")
    public String viewGroupStats(@PathVariable Long id, Model model) {
        Optional<Group> groupOptional = groupService.getGroupById(id);
        if (groupOptional.isPresent()) {
            Group group = groupOptional.get();

            // получить статистику студентов
            List<StudentStatsDTO> studentStats = statsService.getStudentStatsByGroup(id);
            StatsService.GroupStatsDTO groupStats = statsService.getGroupStats(id);

            model.addAttribute("group", group);
            model.addAttribute("studentStats", studentStats);
            model.addAttribute("groupStats", groupStats);

            return "teacher/group-stats";
        }
        return "redirect:/teacher/groups";
    }

    // viewStudentDetailedStats - просмотр детальной статистики студента
    // вход:
    //   - groupId - идентификатор группы
    //   - studentId - идентификатор студента
    //   - model - модель Spring MVC для передачи данных в представление
    // выход: имя представления с детальной статистикой студента или перенаправление
    // логика:
    //  - проверяет, что студент принадлежит указанной группе
    //  - получает детальную статистику студента по модулям и упражнениям
    //  - получает общую статистику студента
    // исключения:
    //  - возможны исключения при работе с базой данных
    @GetMapping("/{groupId}/students/{studentId}/stats")
    public String viewStudentDetailedStats(@PathVariable Long groupId,
                                           @PathVariable Long studentId,
                                           Model model) {
        System.out.println("=== DEBUG: viewStudentDetailedStats ===");
        System.out.println("Group ID: " + groupId);
        System.out.println("Student ID: " + studentId);

        Optional<Group> groupOptional = groupService.getGroupById(groupId);
        Optional<User> studentOptional = userService.findUserById(studentId);

        System.out.println("Group found: " + groupOptional.isPresent());
        System.out.println("Student found: " + studentOptional.isPresent());

        if (groupOptional.isPresent() && studentOptional.isPresent()) {
            Group group = groupOptional.get();
            User student = studentOptional.get();

            System.out.println("Student group: " + (student.getGroup() != null ? student.getGroup().getId() : "null"));

            // проверка, что студент действительно в этой группе
            if (student.getGroup() == null || !student.getGroup().getId().equals(groupId)) {
                System.out.println("Student not in group - redirecting");
                return "redirect:/teacher/groups/" + groupId + "/stats?error=student_not_in_group";
            }

            List<StudentDetailedStatsDTO> detailedStats = statsService.getDetailedStatsByStudent(studentId);
            List<StudentStatsDTO> studentStats = statsService.getStudentStatsByGroup(groupId);

            System.out.println("Detailed stats count: " + detailedStats.size());
            System.out.println("Student stats count: " + studentStats.size());

            // нужно найти общую статистику этого студента
            StudentStatsDTO studentGeneralStats = studentStats.stream()
                    .filter(stat -> stat.getStudentId().equals(studentId))
                    .findFirst()
                    .orElse(new StudentStatsDTO(studentId, student.getUsername(), 0L, 0L, null));

            System.out.println("Student general stats: " + studentGeneralStats);

            model.addAttribute("group", group);
            model.addAttribute("student", student);
            model.addAttribute("detailedStats", detailedStats);
            model.addAttribute("studentGeneralStats", studentGeneralStats);

            System.out.println("=== DEBUG: Rendering template ===");
            return "teacher/student-detailed-stats";
        }

        System.out.println("=== DEBUG: Redirecting to groups ===");
        return "redirect:/teacher/groups";
    }
}