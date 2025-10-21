package org.example.diploma.service;

import org.example.diploma.model.User;
import org.example.diploma.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean validateRegistration(User user, BindingResult bindingResult) {
        // Проверка на уникальность имени пользователя
        if (userRepository.existsByUsername(user.getUsername())) {
            bindingResult.addError(new FieldError("user", "username", "Username already exists"));
            return false;
        }

        // Дополнительные проверки (уже есть аннотации в модели)
        return !bindingResult.hasErrors();
    }

    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public List<User> findByGroupId(Long groupId) {
        return userRepository.findByGroupId(groupId);
    }

    public List<User> findStudentsWithoutGroup() {
        return userRepository.findByRoleAndGroupIsNull("STUDENT");
    }

}
