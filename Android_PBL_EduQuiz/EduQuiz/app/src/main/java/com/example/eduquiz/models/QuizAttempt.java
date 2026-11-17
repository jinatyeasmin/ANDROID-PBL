package com.example.eduquiz.models;

import java.io.Serializable;

public class QuizAttempt implements Serializable {
    private String id;
    private String userId;
    private String userName;
    private String quizId;
    private String quizTitle;
    private String subjectId;
    private String subjectName;
    private int totalQuestions;
    private int correctAnswers;
    private int wrongAnswers;
    private int scorePercentage;
    private long timestamp;
    private String dateString;
    private boolean random;

    public QuizAttempt() {
    }

    public QuizAttempt(String id, String userId, String userName, String quizId, String quizTitle, String subjectId, String subjectName, int totalQuestions, int correctAnswers, int wrongAnswers, int scorePercentage, long timestamp, String dateString, boolean random) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.quizId = quizId;
        this.quizTitle = quizTitle;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.wrongAnswers = wrongAnswers;
        this.scorePercentage = scorePercentage;
        this.timestamp = timestamp;
        this.dateString = dateString;
        this.random = random;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getQuizId() {
        return quizId;
    }

    public void setQuizId(String quizId) {
        this.quizId = quizId;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    public void setQuizTitle(String quizTitle) {
        this.quizTitle = quizTitle;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getSectionId() {
        return subjectId;
    }

    public void setSectionId(String sectionId) {
        this.subjectId = sectionId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSectionName() {
        return subjectName;
    }

    public void setSectionName(String sectionName) {
        this.subjectName = sectionName;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public int getScore() {
        return correctAnswers;
    }

    public void setScore(int score) {
        this.correctAnswers = score;
    }

    public int getWrongAnswers() {
        return wrongAnswers;
    }

    public void setWrongAnswers(int wrongAnswers) {
        this.wrongAnswers = wrongAnswers;
    }

    public int getScorePercentage() {
        return scorePercentage;
    }

    public void setScorePercentage(int scorePercentage) {
        this.scorePercentage = scorePercentage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public boolean isRandom() {
        return random;
    }

    public void setRandom(boolean random) {
        this.random = random;
    }
}
