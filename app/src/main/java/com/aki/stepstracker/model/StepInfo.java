package com.aki.stepstracker.model;

public class StepInfo {
    private String date;
    private int steps;

    public StepInfo(String date, int steps) {
        this.date = date;
        this.steps = steps;
    }

    public int getSteps() {
        return steps;
    }

    public String getDate() {
        return date;
    }
}
