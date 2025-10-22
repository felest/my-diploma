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

    // Конструктор ModuleService - внедрение зависимостей
    // вход:
    //   - moduleRepository - репозиторий для работы с модулями
    //   - exerciseService - сервис для работы с упражнениями
    // выход: созданный экземпляр ModuleService
    @Autowired
    public ModuleService(ModuleRepository moduleRepository, ExerciseService exerciseService) {
        this.moduleRepository = moduleRepository;
        this.exerciseService = exerciseService;
    }

    // getUserModules - получение всех модулей пользователя (преподавателя)
    // вход: userId - идентификатор пользователя
    // выход: список модулей, созданных указанным пользователем
    public List<Module> getUserModules(Long userId) {
        return moduleRepository.findByUserId(userId);
    }

    // getModuleById - получение модуля по идентификатору
    // вход: id - идентификатор модуля
    // выход: Optional<Module> - модуль, если найден
    public Optional<Module> getModuleById(Long id) {
        return moduleRepository.findById(id);
    }

    // saveModule - сохранение модуля в базу данных
    // вход: module - объект модуля для сохранения
    // выход: сохраненный объект модуля
    public Module saveModule(Module module) {
        return moduleRepository.save(module);
    }

    // deleteModule - удаление модуля по идентификатору
    // вход: id - идентификатор модуля для удаления
    // выход: void
    // логика:
    //  - удаляет модуль из базы данных
    //  - связанные слова и упражнения удаляются каскадно через соответствующие сервисы
    public void deleteModule(Long id) {
        moduleRepository.deleteById(id);
    }

    // isModuleTitleUniqueForUser - проверка уникальности названия модуля для пользователя
    // вход:
    //   - title - название модуля для проверки
    //   - userId - идентификатор пользователя (преподавателя)
    // выход:
    //   - true - если название уникально для данного пользователя
    //   - false - если модуль с таким названием уже существует у пользователя
    // логика:
    //  - используется для валидации при создании и редактировании модулей
    public boolean isModuleTitleUniqueForUser(String title, Long userId) {
        return !moduleRepository.existsByTitleAndUserId(title, userId);
    }

    // generateExercisesForModule - генерация упражнений для модуля
    // вход: moduleId - идентификатор модуля
    // выход: void
    // логика:
    //  - вызывает сервис упражнений для генерации упражнений на основе слов модуля
    //  - создает упражнения типа MULTIPLE_CHOICE для каждого слова в модуле
    public void generateExercisesForModule(Long moduleId) {
        Optional<Module> moduleOptional = getModuleById(moduleId);
        if (moduleOptional.isPresent()) {
            exerciseService.generateExercisesForModule(moduleOptional.get());
        }
    }
}