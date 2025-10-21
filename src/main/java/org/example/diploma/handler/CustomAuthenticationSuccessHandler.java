package org.example.diploma.handler;

import org.example.diploma.model.User;
import org.example.diploma.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String username = authentication.getName();
        Optional<User> userOptional = userService.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if ("TEACHER".equals(user.getRole())) {
                response.sendRedirect("/teacher/dashboard");
            } else if ("STUDENT".equals(user.getRole())) {
                response.sendRedirect("/student/dashboard");
            } else {
                response.sendRedirect("/dashboard");
            }
        } else {
            response.sendRedirect("/login?error");
        }
    }
}
