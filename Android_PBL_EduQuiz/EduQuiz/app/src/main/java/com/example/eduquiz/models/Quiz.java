
package com.example.eduquiz.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Quiz implements Serializable {

    private String id;
    private String subjectId;
    private String subjectName;
    private String title;
    private String description;
    private int totalQuestions;
    private int timeLimitMinutes;
    private boolean published;
    private long createdAt;

    // Changed List<Question> to Map<String, Question>
    private Map<String, Question> questions;

    // Default constructor (required by Firebase)
    public Quiz() {
        questions = new HashMap<>();
        published = true;
        timeLimitMinutes = 0;
    }

    // Parameterized constructor
    public Quiz(String id, String subjectId, String subjectName,
                String title, String description,
                int totalQuestions, int timeLimitMinutes) {

        this.id = id;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.title = title;
        this.description = description;
        this.totalQuestions = totalQuestions;
        this.timeLimitMinutes = timeLimitMinutes;
        this.published = false;
        this.createdAt = System.currentTimeMillis();
        this.questions = new HashMap<>();
    }

    // ID
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Subject ID
    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    // Section ID compatibility
    public String getSectionId() {
        return subjectId;
    }

    public void setSectionId(String sectionId) {
        this.subjectId = sectionId;
    }

    // Subject Name
    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    // Title
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // Description
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Total Questions
    public int getTotalQuestions() {
        return totalQuestions > 0
                ? totalQuestions
                : (questions != null ? questions.size() : 0);
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    // Time Limit
    public int getTimeLimitMinutes() {
        return timeLimitMinutes;
    }

    public void setTimeLimitMinutes(int timeLimitMinutes) {
        this.timeLimitMinutes = timeLimitMinutes;
    }

    // Published Status
    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    // Created At
    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    // Questions Map
    public Map<String, Question> getQuestions() {
        return questions;
    }

    public void setQuestions(Map<String, Question> questions) {
        this.questions = questions;
    }

    // Question Count
    public int getQuestionCount() {
        return questions != null
                ? questions.size()
                : totalQuestions;
    }
}
