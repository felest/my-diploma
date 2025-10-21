package org.example.diploma.dto;

import org.example.diploma.model.Module;

public class ModuleProgressDTO {
    private Module module;
    private double progress;
    private boolean completed;

    public ModuleProgressDTO(Module module, double progress) {
        this.module = module;
        this.progress = progress;
        this.completed = progress >= 100.0;
    }

    // Геттеры и сеттеры
    public Module getModule() { return module; }
    public void setModule(Module module) { this.module = module; }

    public double getProgress() { return progress; }
    public void setProgress(double progress) { this.progress = progress; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
