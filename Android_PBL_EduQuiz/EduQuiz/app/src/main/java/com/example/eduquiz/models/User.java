package com.example.eduquiz.models;

public class User {
    private String uid;
    private String name;
    private String email;
    private long createdAt;
    private int streak;
    private String lastActiveDate;
    private int totalAttempts;
    private int totalQuestions;
    private int totalCorrect;
    private int totalWrong;

    public User() {
    }

    public User(String uid, String name, String email, long createdAt) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
        this.streak = 0;
        this.lastActiveDate = "";
        this.totalAttempts = 0;
        this.totalQuestions = 0;
        this.totalCorrect = 0;
        this.totalWrong = 0;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name != null ? name : "Student";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public String getLastActiveDate() {
        return lastActiveDate;
    }

    public void setLastActiveDate(String lastActiveDate) {
        this.lastActiveDate = lastActiveDate;
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(int totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getTotalCorrect() {
        return totalCorrect;
    }

    public void setTotalCorrect(int totalCorrect) {
        this.totalCorrect = totalCorrect;
    }

    public int getTotalWrong() {
        return totalWrong;
    }

    public void setTotalWrong(int totalWrong) {
        this.totalWrong = totalWrong;
    }

    public int getAccuracyPercentage() {
        if (totalQuestions == 0) return 0;
        return (int) Math.round(((double) totalCorrect / totalQuestions) * 100);
    }
}
