package org.example.diploma.service;

import org.example.diploma.model.Exercise;
import org.example.diploma.model.Module;
import org.example.diploma.model.Word;
import org.example.diploma.repository.ExerciseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExerciseService {
    private final ExerciseRepository exerciseRepository;
    private final WordService wordService;

    @Autowired
    public ExerciseService(ExerciseRepository exerciseRepository, WordService wordService) {
        this.exerciseRepository = exerciseRepository;
        this.wordService = wordService;
    }

    public List<Exercise> getExercisesByModuleId(Long moduleId) {
        return exerciseRepository.findByModuleId(moduleId);
    }

    public Optional<Exercise> getExerciseById(Long id) {
        return exerciseRepository.findById(id);
    }

    public Exercise saveExercise(Exercise exercise) {
        return exerciseRepository.save(exercise);
    }

    public void deleteExercise(Long id) {
        exerciseRepository.deleteById(id);
    }

    public void deleteExercisesByModuleId(Long moduleId) {
        exerciseRepository.deleteByModuleId(moduleId);
    }

    public Exercise generateMultipleChoiceExercise(Module module, Word word) {
        List<Word> allWords = wordService.getWordsByModuleId(module.getId());

        // Убедимся, что в модуле достаточно слов для создания вариантов ответа
        if (allWords.size() < 4) {
            throw new RuntimeException("Module must have at least 4 words to generate exercises");
        }

        // Создаем упражнение
        Exercise exercise = new Exercise();
        exercise.setType("MULTIPLE_CHOICE");
        exercise.setModule(module);

        // Случайным образом выбираем, на каком языке будет вопрос
        Random random = new Random();
        boolean isEnglishQuestion = random.nextBoolean();

        if (isEnglishQuestion) {
            exercise.setQuestion("What is the translation of: " + word.getEnglish() + "?");
            exercise.setCorrectAnswer(word.getRussian());
        } else {
            exercise.setQuestion("Как переводится: " + word.getRussian() + "?");
            exercise.setCorrectAnswer(word.getEnglish());
        }

        // Выбираем 3 случайных слова для неправильных вариантов ответа
        List<Word> otherWords = allWords.stream()
                .filter(w -> !w.getId().equals(word.getId()))
                .collect(Collectors.toList());

        Collections.shuffle(otherWords);
        List<Word> wrongOptions = otherWords.subList(0, 3);

        // Собираем все варианты ответов
        List<String> allOptions = new ArrayList<>();
        allOptions.add(exercise.getCorrectAnswer());

        for (Word wrongWord : wrongOptions) {
            if (isEnglishQuestion) {
                allOptions.add(wrongWord.getRussian());
            } else {
                allOptions.add(wrongWord.getEnglish());
            }
        }

        // Перемешиваем варианты ответов
        Collections.shuffle(allOptions);

        // Устанавливаем варианты ответов
        exercise.setOption1(allOptions.get(0));
        exercise.setOption2(allOptions.get(1));
        exercise.setOption3(allOptions.get(2));
        exercise.setOption4(allOptions.get(3));

        return exercise;
    }

    public void generateExercisesForModule(Module module) {
        List<Word> words = wordService.getWordsByModuleId(module.getId());

        // Создаем по одному упражнению для каждого слова в модуле
        for (Word word : words) {
            Exercise exercise = generateMultipleChoiceExercise(module, word);
            saveExercise(exercise);
        }
    }
}
