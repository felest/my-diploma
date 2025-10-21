package org.example.diploma.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "study_groups")
@Data
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private User teacher;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<User> students = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "group_modules",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "module_id")
    )
    private List<Module> assignedModules = new ArrayList<>();

    // Конструкторы, геттеры и сеттеры

    public Group(Long id, String description, String name, User teacher, List<User> students, List<Module> assignedModules) {
        this.id = id;
        this.description = description;
        this.name = name;
        this.teacher = teacher;
        this.students = students;
        this.assignedModules = assignedModules;
    }

    public Group() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getTeacher() {
        return teacher;
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }

    public List<User> getStudents() {
        return students;
    }

    public void setStudents(List<User> students) {
        this.students = students;
    }

    public List<Module> getAssignedModules() {
        return assignedModules;
    }

    public void setAssignedModules(List<Module> assignedModules) {
        this.assignedModules = assignedModules;
    }
}
