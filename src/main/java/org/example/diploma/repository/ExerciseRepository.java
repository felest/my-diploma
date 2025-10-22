package org.example.diploma.repository;

import org.example.diploma.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    // findByModuleId - поиск всех упражнений по идентификатору модуля
    // вход: moduleId - идентификатор модуля
    // выход: список упражнений, принадлежащих указанному модулю
    // логика:
    //  - возвращает все упражнения, связанные с модулем по foreign key
    List<Exercise> findByModuleId(Long moduleId);

    // deleteByModuleId - удаление всех упражнений по идентификатору модуля
    // вход: moduleId - идентификатор модуля
    // выход: void
    // логика:
    //  - удаляет все упражнения, связанные с указанным модулем
    //  - используется при удалении модуля для каскадного удаления упражнений
    void deleteByModuleId(Long moduleId);
}
