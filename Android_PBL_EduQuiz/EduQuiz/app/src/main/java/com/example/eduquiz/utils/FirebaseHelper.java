package com.example.eduquiz.utils;

import androidx.annotation.NonNull;

import com.example.eduquiz.models.Question;
import com.example.eduquiz.models.Quiz;
import com.example.eduquiz.models.QuizAttempt;
import com.example.eduquiz.models.Subject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class FirebaseHelper {

    public static final String USERS = "users";
    public static final String ADMINS = "admins";
    public static final String SUBJECTS = "subjects";
    public static final String SECTIONS = "subjects"; // Synced with subjects
    public static final String QUIZZES = "quizzes";
    public static final String QUESTIONS = "questions";
    public static final String ATTEMPTS = "attempts";
    public static final String FEEDBACK = "feedback";

    private static FirebaseDatabase database;
    private static FirebaseAuth auth;

    public static FirebaseDatabase getDatabase() {
        if (database == null) {
            database = FirebaseDatabase.getInstance();
        }
        return database;
    }

    public static FirebaseAuth getAuth() {
        if (auth == null) {
            auth = FirebaseAuth.getInstance();
        }
        return auth;
    }

    public static DatabaseReference usersRef() {
        return getDatabase().getReference(USERS);
    }

    public static DatabaseReference adminsRef() {
        return getDatabase().getReference(ADMINS);
    }

    public static DatabaseReference subjectsRef() {
        return getDatabase().getReference(SUBJECTS);
    }

    public static DatabaseReference sectionsRef() {
        return subjectsRef();
    }

    public static DatabaseReference quizzesRef() {
        return getDatabase().getReference(QUIZZES);
    }

    public static DatabaseReference questionsRef() {
        return getDatabase().getReference(QUESTIONS);
    }

    public static DatabaseReference attemptsRef() {
        return getDatabase().getReference(ATTEMPTS);
    }

    public static DatabaseReference feedbackRef() {
        return getDatabase().getReference(FEEDBACK);
    }

    public static String currentUserId() {
        FirebaseUser user = getAuth().getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public static String getTodayDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    public static String getYesterdayDateString() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(cal.getTime());
    }

    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * Calculates consecutive days streak from a list of attempts.
     * Consecutive calendar days with at least one completed quiz.
     * Handles missed days and duplicate attempts on the same day correctly.
     */
    public static int calculateStreak(List<QuizAttempt> attempts) {
        if (attempts == null || attempts.isEmpty()) {
            return 0;
        }

        // Collect unique attempt dates
        Set<String> uniqueDates = new HashSet<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (QuizAttempt attempt : attempts) {
            if (attempt.getTimestamp() > 0) {
                uniqueDates.add(sdf.format(new Date(attempt.getTimestamp())));
            } else if (attempt.getDateString() != null && !attempt.getDateString().isEmpty()) {
                uniqueDates.add(attempt.getDateString());
            }
        }

        Calendar cal = Calendar.getInstance();
        String today = sdf.format(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, -1);
        String yesterday = sdf.format(cal.getTime());

        // If no attempt today and no attempt yesterday, streak is broken (0)
        if (!uniqueDates.contains(today) && !uniqueDates.contains(yesterday)) {
            return 0;
        }

        // Start checking from the most recent active day
        int streak = 0;
        cal = Calendar.getInstance();
        if (!uniqueDates.contains(today)) {
            // Started streak yesterday
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }

        while (true) {
            String checkDate = sdf.format(cal.getTime());
            if (uniqueDates.contains(checkDate)) {
                streak++;
                cal.add(Calendar.DAY_OF_YEAR, -1);
            } else {
                break;
            }
        }

        return streak;
    }

    /**
     * Seeds initial default subjects and quizzes if the database is empty.
     */
    public static void seedInitialDataIfNeeded() {
        subjectsRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    performSeed();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private static void performSeed() {
        // 1. Mathematics
        Subject math = new Subject("math", "Mathematics", "Algebra, Geometry & Arithmetic", "math");
        math.setQuizCount(1);
        math.setQuestionCount(5);
        subjectsRef().child("math").setValue(math);

        Quiz mathQuiz = new Quiz("quiz_math_01", "math", "Mathematics", "Basic Mathematics", "Test your core arithmetic and algebra skills", 5, 5);
        mathQuiz.setPublished(true);
        quizzesRef().child("quiz_math_01").setValue(mathQuiz);

        addQuestion("quiz_math_01", "q1", "What is 2 + 2 ?", "3", "4", "5", "6", 2);
        addQuestion("quiz_math_01", "q2", "What is the square root of 64?", "6", "7", "8", "9", 3);
        addQuestion("quiz_math_01", "q3", "What is 15 * 6?", "80", "90", "100", "75", 2);
        addQuestion("quiz_math_01", "q4", "Solve for x: 3x - 5 = 10", "3", "4", "5", "6", 3);
        addQuestion("quiz_math_01", "q5", "What is the perimeter of a square with side 7cm?", "21cm", "28cm", "49cm", "14cm", 2);

        // 2. Science
        Subject science = new Subject("science", "Science", "Physics, Chemistry & Biology", "science");
        science.setQuizCount(1);
        science.setQuestionCount(5);
        subjectsRef().child("science").setValue(science);

        Quiz scienceQuiz = new Quiz("quiz_science_01", "science", "Science", "General Science Essentials", "Fundamental concepts of nature and life", 5, 5);
        scienceQuiz.setPublished(true);
        quizzesRef().child("quiz_science_01").setValue(scienceQuiz);

        addQuestion("quiz_science_01", "q1", "What is known as the powerhouse of the cell?", "Nucleus", "Ribosome", "Mitochondria", "Cytoplasm", 3);
        addQuestion("quiz_science_01", "q2", "What is the chemical symbol for Water?", "CO2", "H2O", "O2", "NaCl", 2);
        addQuestion("quiz_science_01", "q3", "What planet is known as the Red Planet?", "Venus", "Mars", "Jupiter", "Saturn", 2);
        addQuestion("quiz_science_01", "q4", "What gas do plants absorb during photosynthesis?", "Oxygen", "Nitrogen", "Carbon Dioxide", "Hydrogen", 3);
        addQuestion("quiz_science_01", "q5", "What is the speed of light approximately?", "300,000 km/s", "150,000 km/s", "500,000 km/s", "1,000,000 km/s", 1);

        // 3. Computer Science
        Subject cs = new Subject("cs", "Computer Science", "Programming, Algorithms & Systems", "cs");
        cs.setQuizCount(1);
        cs.setQuestionCount(5);
        subjectsRef().child("cs").setValue(cs);

        Quiz csQuiz = new Quiz("quiz_cs_01", "cs", "Computer Science", "Java & Programming Basics", "Fundamental principles of programming in Java", 5, 5);
        csQuiz.setPublished(true);
        quizzesRef().child("quiz_cs_01").setValue(csQuiz);

        addQuestion("quiz_cs_01", "q1", "Which keyword is used to inherit a class in Java?", "implements", "extends", "inherits", "super", 2);
        addQuestion("quiz_cs_01", "q2", "What is the size of an int data type in Java?", "2 bytes", "4 bytes", "8 bytes", "Depends on OS", 2);
        addQuestion("quiz_cs_01", "q3", "Which data structure operates on LIFO (Last In First Out)?", "Queue", "Stack", "Array", "Linked List", 2);
        addQuestion("quiz_cs_01", "q4", "Which company originally created the Android operating system?", "Google", "Android Inc.", "Apple", "Microsoft", 2);
        addQuestion("quiz_cs_01", "q5", "What is the time complexity of binary search on a sorted array?", "O(n)", "O(log n)", "O(n^2)", "O(1)", 2);

        // 4. History
        Subject history = new Subject("history", "History", "World History, Civilizations & Events", "history");
        history.setQuizCount(1);
        history.setQuestionCount(4);
        subjectsRef().child("history").setValue(history);

        Quiz historyQuiz = new Quiz("quiz_history_01", "history", "History", "World History Trivia", "Famous historical landmarks and milestones", 4, 5);
        historyQuiz.setPublished(true);
        quizzesRef().child("quiz_history_01").setValue(historyQuiz);

        addQuestion("quiz_history_01", "q1", "In which year did World War II end?", "1942", "1945", "1948", "1950", 2);
        addQuestion("quiz_history_01", "q2", "Who was the first President of the United States?", "Thomas Jefferson", "George Washington", "Abraham Lincoln", "John Adams", 2);
        addQuestion("quiz_history_01", "q3", "Which ancient civilization built the Pyramids of Giza?", "Romans", "Greeks", "Egyptians", "Mayans", 3);
        addQuestion("quiz_history_01", "q4", "What wall fell in 1989 marking the end of Cold War divisions?", "Great Wall", "Berlin Wall", "Hadrian's Wall", "Western Wall", 2);
    }

    private static void addQuestion(String quizId, String qId, String text, String optA, String optB, String optC, String optD, int correct) {
        Question q = new Question(qId, quizId, text, optA, optB, optC, optD, correct);
        questionsRef().child(quizId).child(qId).setValue(q);
    }
}
