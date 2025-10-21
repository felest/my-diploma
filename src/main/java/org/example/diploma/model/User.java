package org.example.diploma.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @Size(min = 5, message = "Username must be at least 5 characters")
    private String username;

    @Column(nullable = false)
    @Size(min = 5, message = "Password must be at least 5 characters")
    @Pattern(regexp = "^[^<>*?!:]+$", message = "Password cannot contain <, *, ?, :, ! characters")
    private String password;

    @Column(nullable = false)
    private String role; // "STUDENT" или "TEACHER"

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Module> modules = new ArrayList<>();

    //  связь с группой (для студентов)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;
}
