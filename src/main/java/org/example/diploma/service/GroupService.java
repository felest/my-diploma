package org.example.diploma.service;

import org.example.diploma.model.Group;
import org.example.diploma.model.Module;
import org.example.diploma.model.User;
import org.example.diploma.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final UserService userService;

    @Autowired
    public GroupService(GroupRepository groupRepository, UserService userService) {
        this.groupRepository = groupRepository;
        this.userService = userService;
    }

    public List<Group> getTeacherGroups(Long teacherId) {
        return groupRepository.findByTeacherId(teacherId);
    }

    public Optional<Group> getGroupById(Long id) {
        return groupRepository.findById(id);
    }

    public Group saveGroup(Group group) {
        return groupRepository.save(group);
    }

    public void deleteGroup(Long id) {
        groupRepository.deleteById(id);
    }

    public boolean isGroupNameUniqueForTeacher(String name, Long teacherId) {
        return !groupRepository.existsByNameAndTeacherId(name, teacherId);
    }

    public void assignModuleToGroup(Long groupId, Module module) {
        Optional<Group> groupOptional = getGroupById(groupId);
        if (groupOptional.isPresent()) {
            Group group = groupOptional.get();
            if (!group.getAssignedModules().contains(module)) {
                group.getAssignedModules().add(module);
                groupRepository.save(group);
            }
        }
    }

    public void removeModuleFromGroup(Long groupId, Long moduleId) {
        Optional<Group> groupOptional = getGroupById(groupId);
        if (groupOptional.isPresent()) {
            Group group = groupOptional.get();
            group.getAssignedModules().removeIf(module -> module.getId().equals(moduleId));
            groupRepository.save(group);
        }
    }

    public void addStudentToGroup(Long groupId, Long studentId) {
        Optional<Group> groupOptional = getGroupById(groupId);
        Optional<User> studentOptional = userService.findUserById(studentId);

        if (groupOptional.isPresent() && studentOptional.isPresent()) {
            Group group = groupOptional.get();
            User student = studentOptional.get();

            // Проверяем, что пользователь действительно студент
            if ("STUDENT".equals(student.getRole())) {
                student.setGroup(group);
                userService.saveUser(student);
            }
        }
    }

    public void removeStudentFromGroup(Long studentId) {
        Optional<User> studentOptional = userService.findUserById(studentId);
        if (studentOptional.isPresent()) {
            User student = studentOptional.get();
            student.setGroup(null);
            userService.saveUser(student);
        }
    }
}
