package com.example.eduquiz.admin;

import android.widget.EditText;
import android.widget.Spinner;

public class QuestionFields {

    public EditText questionText;
    public EditText optionA;
    public EditText optionB;
    public EditText optionC;
    public EditText optionD;
    public Spinner correctSpinner;

    public QuestionFields(
            EditText questionText,
            EditText optionA,
            EditText optionB,
            EditText optionC,
            EditText optionD,
            Spinner correctSpinner) {

        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctSpinner = correctSpinner;
    }
}