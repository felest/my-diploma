package org.example.diploma.repository;

import org.example.diploma.model.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WordRepository extends JpaRepository<Word, Long> {

    // findByModuleId - поиск всех слов по идентификатору модуля
    // вход: moduleId - идентификатор модуля
    // выход: список слов, принадлежащих указанному модулю
    // логика:
    //  - возвращает все слова, связанные с модулем по foreign key
    //  - используется для отображения словаря модуля и генерации упражнений
    List<Word> findByModuleId(Long moduleId);

    // deleteByModuleId - удаление всех слов по идентификатору модуля
    // вход: moduleId - идентификатор модуля
    // выход: void
    // логика:
    //  - удаляет все слова, связанные с указанным модулем
    //  - используется при удалении модуля для каскадного удаления слов
    //  - обеспечивает целостность данных при удалении модулей
    void deleteByModuleId(Long moduleId);
}