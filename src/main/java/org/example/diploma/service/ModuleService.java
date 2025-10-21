package org.example.diploma.service;

import org.example.diploma.model.Module;
import org.example.diploma.model.User;
import org.example.diploma.repository.ModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ModuleService {
    private final ModuleRepository moduleRepository;
    private final ExerciseService exerciseService;

    @Autowired
    public ModuleService(ModuleRepository moduleRepository, ExerciseService exerciseService) {
        this.moduleRepository = moduleRepository;
        this.exerciseService = exerciseService;
    }

    public List<Module> getUserModules(Long userId) {
        return moduleRepository.findByUserId(userId);
    }

    public Optional<Module> getModuleById(Long id) {
        return moduleRepository.findById(id);
    }

    public Module saveModule(Module module) {
        return moduleRepository.save(module);
    }

    public void deleteModule(Long id) {
        moduleRepository.deleteById(id);
    }

    public boolean isModuleTitleUniqueForUser(String title, Long userId) {
        return !moduleRepository.existsByTitleAndUserId(title, userId);
    }

    public void generateExercisesForModule(Long moduleId) {
        Optional<Module> moduleOptional = getModuleById(moduleId);
        if (moduleOptional.isPresent()) {
            exerciseService.generateExercisesForModule(moduleOptional.get());
        }
    }
}
