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

    // Конструктор ExerciseService - внедрение зависимостей
    // вход:
    //   - exerciseRepository - репозиторий для работы с упражнениями
    //   - wordService - сервис для работы со словами
    // выход: созданный экземпляр ExerciseService
    @Autowired
    public ExerciseService(ExerciseRepository exerciseRepository, WordService wordService) {
        this.exerciseRepository = exerciseRepository;
        this.wordService = wordService;
    }

    // getExercisesByModuleId - получение всех упражнений по идентификатору модуля
    // вход: moduleId - идентификатор модуля
    // выход: список упражнений, принадлежащих указанному модулю
    public List<Exercise> getExercisesByModuleId(Long moduleId) {
        return exerciseRepository.findByModuleId(moduleId);
    }

    // getExerciseById - получение упражнения по идентификатору
    // вход: id - идентификатор упражнения
    // выход: Optional<Exercise> - упражнение, если найдено
    public Optional<Exercise> getExerciseById(Long id) {
        return exerciseRepository.findById(id);
    }

    // saveExercise - сохранение упражнения в базу данных
    // вход: exercise - объект упражнения для сохранения
    // выход: сохраненный объект упражнения
    public Exercise saveExercise(Exercise exercise) {
        return exerciseRepository.save(exercise);
    }

    // deleteExercise - удаление упражнения по идентификатору
    // вход: id - идентификатор упражнения для удаления
    // выход: void
    public void deleteExercise(Long id) {
        exerciseRepository.deleteById(id);
    }

    // deleteExercisesByModuleId - удаление всех упражнений по идентификатору модуля
    // вход: moduleId - идентификатор модуля
    // выход: void
    // логика:
    //  - используется для каскадного удаления при удалении модуля
    public void deleteExercisesByModuleId(Long moduleId) {
        exerciseRepository.deleteByModuleId(moduleId);
    }

    // generateMultipleChoiceExercise - генерация упражнения с множественным выбором для слова
    // вход:
    //   - module - модуль, для которого генерируется упражнение
    //   - word - слово, для которого создается упражнение
    // выход: сгенерированный объект Exercise
    // логика:
    //  - создает упражнение типа MULTIPLE_CHOICE
    //  - случайным образом определяет язык вопроса (английский или русский)
    //  - выбирает 3 случайных слова для неправильных вариантов ответа
    //  - перемешивает варианты ответов
    // исключения:
    //  - RuntimeException - если в модуле меньше 4 слов
    public Exercise generateMultipleChoiceExercise(Module module, Word word) {
        List<Word> allWords = wordService.getWordsByModuleId(module.getId());

        // надо убедиться, что в модуле достаточно слов для создания вариантов ответа
        if (allWords.size() < 4) {
            throw new RuntimeException("Module must have at least 4 words to generate exercises");
        }

        // создать упражнение
        Exercise exercise = new Exercise();
        exercise.setType("MULTIPLE_CHOICE");
        exercise.setModule(module);

        // случайным образом выбрать, на каком языке будет вопрос
        Random random = new Random();
        boolean isEnglishQuestion = random.nextBoolean();

        if (isEnglishQuestion) {
            exercise.setQuestion("What is the translation of: " + word.getEnglish() + "?");
            exercise.setCorrectAnswer(word.getRussian());
        } else {
            exercise.setQuestion("Как переводится: " + word.getRussian() + "?");
            exercise.setCorrectAnswer(word.getEnglish());
        }

        // выбирать 3 случайных слова для неправильных вариантов ответа
        List<Word> otherWords = allWords.stream()
                .filter(w -> !w.getId().equals(word.getId()))
                .collect(Collectors.toList());

        Collections.shuffle(otherWords);
        List<Word> wrongOptions = otherWords.subList(0, 3);

        // собрать все варианты ответов
        List<String> allOptions = new ArrayList<>();
        allOptions.add(exercise.getCorrectAnswer());

        for (Word wrongWord : wrongOptions) {
            if (isEnglishQuestion) {
                allOptions.add(wrongWord.getRussian());
            } else {
                allOptions.add(wrongWord.getEnglish());
            }
        }

        // перемешать варианты ответов
        Collections.shuffle(allOptions);

        // установить варианты ответов
        exercise.setOption1(allOptions.get(0));
        exercise.setOption2(allOptions.get(1));
        exercise.setOption3(allOptions.get(2));
        exercise.setOption4(allOptions.get(3));

        return exercise;
    }

    // generateExercisesForModule - генерация упражнений для всех слов модуля
    // вход: module - модуль, для которого генерируются упражнения
    // выход: void
    // логика:
    //  - для каждого слова в модуле создает одно упражнение
    //  - сохраняет все сгенерированные упражнения в базу данных
    public void generateExercisesForModule(Module module) {
        List<Word> words = wordService.getWordsByModuleId(module.getId());

        // создать по одному упражнению для каждого слова в модуле
        for (Word word : words) {
            Exercise exercise = generateMultipleChoiceExercise(module, word);
            saveExercise(exercise);
        }
    }
}