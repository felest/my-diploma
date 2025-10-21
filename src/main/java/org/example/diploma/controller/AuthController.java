package org.example.diploma.controller;

import org.example.diploma.dto.RegistrationRequest;
import org.example.diploma.model.User;
import org.example.diploma.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    private final UserService userService;

    // Конструктор AuthController - внедрение зависимости UserService
    // вход: userService - сервис для работы с пользователями
    // выход: созданный экземпляр AuthController
    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // home - обработка корневого маршрута и перенаправление пользователя
    // вход: authentication - объект аутентификации Spring Security
    // выход: строка перенаправления в зависимости от роли пользователя
    // логика:
    //  - если пользователь аутентифицирован и не анонимный, перенаправляет на соответствующий dashboard
    //  - если пользователь не аутентифицирован, перенаправляет на страницу логина
    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            // Проверить роль пользователя и перенаправить на соответствующий dashboard
            boolean isTeacher = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_TEACHER"));
            boolean isStudent = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_STUDENT"));

            if (isTeacher) {
                return "redirect:/teacher/dashboard";
            } else if (isStudent) {
                return "redirect:/student/dashboard";
            }
        }
        return "redirect:/login";
    }

    // showRegistrationForm - отображение формы регистрации
    // вход: model - модель Spring MVC для передачи данных в представление
    // выход: имя представления для отображения формы регистрации
    // логика:
    //  - добавляет в модель новый объект RegistrationRequest
    //  - возвращает имя шаблона "register"
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("registrationRequest", new RegistrationRequest());
        return "register";
    }

    // registerUser - обработка данных регистрации пользователя
    // вход:
    //   - registrationRequest - DTO с данными регистрации (валидируется)
    //   - bindingResult - объект для хранения результатов валидации
    //   - model - модель Spring MVC для передачи данных в представление
    // выход: строка перенаправления или имя представления
    // логика:
    //  - создает нового пользователя на основе registrationRequest
    //  - проверяет ошибки валидации
    //  - регистрирует пользователя через userService
    // исключения:
    //  - Exception - если регистрация не удалась
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("registrationRequest") RegistrationRequest registrationRequest,
                               BindingResult bindingResult,
                               Model model) {

        User user = new User();
        user.setUsername(registrationRequest.getUsername());
        user.setPassword(registrationRequest.getPassword());
        user.setRole(registrationRequest.getRole());

        if (bindingResult.hasErrors() || !userService.validateRegistration(user, bindingResult)) {
            model.addAttribute("registrationRequest", registrationRequest);
            return "register";
        }

        try {
            userService.registerUser(user);
            return "redirect:/login?registered";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "register";
        }
    }

    // showLoginForm - отображение формы входа
    // вход: отсутствует
    // выход: имя представления для отображения формы логина
    // логика:
    //  - возвращает имя шаблона "login"
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }
}