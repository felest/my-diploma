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

    @Autowired
    public GroupController(GroupService groupService, UserService userService,
                           ModuleService moduleService, StatsService statsService) {
        this.groupService = groupService;
        this.userService = userService;
        this.moduleService = moduleService;
        this.statsService = statsService;
    }

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
            // Добавим информацию об ошибке в модель для отладки
            model.addAttribute("error", "Error loading groups: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/new")
    public String showCreateGroupForm(Model model) {
        model.addAttribute("group", new Group());
        return "teacher/group-form";
    }

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

    @PostMapping("/{groupId}/students")
    public String addStudentToGroup(@PathVariable Long groupId,
                                    @RequestParam Long studentId,
                                    RedirectAttributes redirectAttributes) {
        groupService.addStudentToGroup(groupId, studentId);
        redirectAttributes.addFlashAttribute("success", "Student added to group");
        return "redirect:/teacher/groups/" + groupId;
    }

    @PostMapping("/{groupId}/students/{studentId}/remove")
    public String removeStudentFromGroup(@PathVariable Long groupId,
                                         @PathVariable Long studentId,
                                         RedirectAttributes redirectAttributes) {
        groupService.removeStudentFromGroup(studentId);
        redirectAttributes.addFlashAttribute("success", "Student removed from group");
        return "redirect:/teacher/groups/" + groupId;
    }

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

    @PostMapping("/{groupId}/modules/{moduleId}/remove")
    public String removeModuleFromGroup(@PathVariable Long groupId,
                                        @PathVariable Long moduleId,
                                        RedirectAttributes redirectAttributes) {
        groupService.removeModuleFromGroup(groupId, moduleId);
        redirectAttributes.addFlashAttribute("success", "Module removed from group");
        return "redirect:/teacher/groups/" + groupId;
    }

    @PostMapping("/{id}/delete")
    public String deleteGroup(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        groupService.deleteGroup(id);
        redirectAttributes.addFlashAttribute("success", "Group deleted successfully");
        return "redirect:/teacher/groups";
    }

    @GetMapping("/{id}/stats")
    public String viewGroupStats(@PathVariable Long id, Model model) {
        Optional<Group> groupOptional = groupService.getGroupById(id);
        if (groupOptional.isPresent()) {
            Group group = groupOptional.get();

            // Получаем статистику студентов
            List<StudentStatsDTO> studentStats = statsService.getStudentStatsByGroup(id);
            StatsService.GroupStatsDTO groupStats = statsService.getGroupStats(id);

            model.addAttribute("group", group);
            model.addAttribute("studentStats", studentStats);
            model.addAttribute("groupStats", groupStats);

            return "teacher/group-stats";
        }
        return "redirect:/teacher/groups";
    }

//    @GetMapping("/{groupId}/students/{studentId}/stats")
//    public String viewStudentDetailedStats(@PathVariable Long groupId,
//                                           @PathVariable Long studentId,
//                                           Model model) {
//        Optional<Group> groupOptional = groupService.getGroupById(groupId);
//        Optional<User> studentOptional = userService.findUserById(studentId);
//
//        if (groupOptional.isPresent() && studentOptional.isPresent()) {
//            Group group = groupOptional.get();
//            User student = studentOptional.get();
//
//            // Проверяем, что студент действительно в этой группе
//            if (student.getGroup() == null || !student.getGroup().getId().equals(groupId)) {
//                return "redirect:/teacher/groups/" + groupId + "/stats?error=student_not_in_group";
//            }
//
//            List<StudentDetailedStatsDTO> detailedStats = statsService.getDetailedStatsByStudent(studentId);
//            List<StudentStatsDTO> studentStats = statsService.getStudentStatsByGroup(groupId);
//
//            // Находим общую статистику этого студента
//            StudentStatsDTO studentGeneralStats = studentStats.stream()
//                    .filter(stat -> stat.getStudentId().equals(studentId))
//                    .findFirst()
//                    .orElse(new StudentStatsDTO(studentId, student.getUsername(), 0L, 0L, null));
//
//            model.addAttribute("group", group);
//            model.addAttribute("student", student);
//            model.addAttribute("detailedStats", detailedStats);
//            model.addAttribute("studentGeneralStats", studentGeneralStats);
//
//            return "teacher/student-detailed-stats";
//        }
//        return "redirect:/teacher/groups";
//    }

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

            // Проверяем, что студент действительно в этой группе
            if (student.getGroup() == null || !student.getGroup().getId().equals(groupId)) {
                System.out.println("Student not in group - redirecting");
                return "redirect:/teacher/groups/" + groupId + "/stats?error=student_not_in_group";
            }

            List<StudentDetailedStatsDTO> detailedStats = statsService.getDetailedStatsByStudent(studentId);
            List<StudentStatsDTO> studentStats = statsService.getStudentStatsByGroup(groupId);

            System.out.println("Detailed stats count: " + detailedStats.size());
            System.out.println("Student stats count: " + studentStats.size());

            // Находим общую статистику этого студента
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
