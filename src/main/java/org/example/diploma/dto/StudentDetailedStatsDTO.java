package org.example.diploma.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StudentDetailedStatsDTO {
    private Long attemptId;
    private String moduleTitle;
    private String exerciseQuestion;
    private String selectedAnswer;
    private String correctAnswer;
    private Boolean correct;
    private LocalDateTime attemptTime;
    private Integer timeSpentSeconds;

    public StudentDetailedStatsDTO(Long attemptId, String moduleTitle, String exerciseQuestion,
                                   String selectedAnswer, String correctAnswer, Boolean correct,
                                   LocalDateTime attemptTime, Integer timeSpentSeconds) {
        this.attemptId = attemptId;
        this.moduleTitle = moduleTitle;
        this.exerciseQuestion = exerciseQuestion;
        this.selectedAnswer = selectedAnswer;
        this.correctAnswer = correctAnswer;
        this.correct = correct;
        this.attemptTime = attemptTime;
        this.timeSpentSeconds = timeSpentSeconds;
    }
}
