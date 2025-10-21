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

    @Autowired
    public WordService(WordRepository wordRepository) {
        this.wordRepository = wordRepository;
    }

    public List<Word> getWordsByModuleId(Long moduleId) {
        return wordRepository.findByModuleId(moduleId);
    }

    public Word saveWord(Word word) {
        return wordRepository.save(word);
    }

    public void deleteWord(Long id) {
        wordRepository.deleteById(id);
    }

    public void deleteWordsByModuleId(Long moduleId) {
        wordRepository.deleteByModuleId(moduleId);
    }

    public Optional<Word> getWordById(Long id) {
        return wordRepository.findById(id);
    }

}
