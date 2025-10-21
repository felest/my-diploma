package org.example.diploma.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StudentStatsDTO {
    private Long studentId;
    private String studentName;
    private Long groupId;
    private String groupName;
    private Long totalAttempts;
    private Long correctAttempts;
    private Double successRate;
    private LocalDateTime lastActivity;
    private Long totalModules;
    private Long completedModules;

    // конструктор
    public StudentStatsDTO(Long studentId, String studentName, Long totalAttempts,
                           Long correctAttempts, LocalDateTime lastActivity) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.totalAttempts = totalAttempts;
        this.correctAttempts = correctAttempts;
        this.lastActivity = lastActivity;
        this.successRate = totalAttempts > 0 ?
                Math.round((correctAttempts.doubleValue() / totalAttempts.doubleValue()) * 100.0) : 0.0;
    }

    // новый конструктор с groupId
    public StudentStatsDTO(Long studentId, String studentName, Long groupId, String groupName,
                           Long totalAttempts, Long correctAttempts, LocalDateTime lastActivity) {
        this(studentId, studentName, totalAttempts, correctAttempts, lastActivity);
        this.groupId = groupId;
        this.groupName = groupName;
    }
}