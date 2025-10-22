package org.example.diploma.service;

import org.example.diploma.model.Word;
import org.example.diploma.repository.WordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class WordService {
    private final WordRepository wordRepository;

    // Конструктор WordService - внедрение зависимости
    // вход: wordRepository - репозиторий для работы со словами
    // выход: созданный экземпляр WordService
    @Autowired
    public WordService(WordRepository wordRepository) {
        this.wordRepository = wordRepository;
    }

    // getWordsByModuleId - получение всех слов по идентификатору модуля
    // вход: moduleId - идентификатор модуля
    // выход: список слов, принадлежащих указанному модулю
    // логика:
    //  - используется для отображения словаря модуля и генерации упражнений
    public List<Word> getWordsByModuleId(Long moduleId) {
        return wordRepository.findByModuleId(moduleId);
    }

    // saveWord - сохранение слова в базу данных
    // вход: word - объект слова для сохранения
    // выход: сохраненный объект слова
    public Word saveWord(Word word) {
        return wordRepository.save(word);
    }

    // deleteWord - удаление слова по идентификатору
    // вход: id - идентификатор слова для удаления
    // выход: void
    public void deleteWord(Long id) {
        wordRepository.deleteById(id);
    }

    // deleteWordsByModuleId - удаление всех слов по идентификатору модуля
    // вход: moduleId - идентификатор модуля
    // выход: void
    // логика:
    //  - используется для каскадного удаления при удалении модуля
    //  - удаляет все слова, связанные с указанным модулем
    public void deleteWordsByModuleId(Long moduleId) {
        wordRepository.deleteByModuleId(moduleId);
    }

    // getWordById - получение слова по идентификатору
    // вход: id - идентификатор слова
    // выход: Optional<Word> - слово, если найдено
    public Optional<Word> getWordById(Long id) {
        return wordRepository.findById(id);
    }
}