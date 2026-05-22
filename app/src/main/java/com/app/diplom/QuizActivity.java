package com.app.diplom;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

public class QuizActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvCoins, tvQuestionWord;
    private LinearLayout answersContainer;
    private Button[] answerButtons = new Button[4];

    private AppDatabase db;
    private List<Word> quizWords;          // Список слов для текущей сессии теста
    private List<Word> allDatabaseWords;   // Все слова из базы (нужны для генерации ложных ответов)

    private int currentRound = 0;
    private final int totalRounds = 10;    // Количество вопросов в одном тесте
    private int sessionCoins = 0;          // Заработанные монетки за эту сессию
    private Word currentCorrectWord;       // Правильное слово текущего раунда

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_screen);

        // Связываем элементы интерфейса
        progressBar = findViewById(R.id.quiz_progress_bar);
        tvCoins = findViewById(R.id.tv_quiz_coins);
        tvQuestionWord = findViewById(R.id.tv_quiz_word);
        answersContainer = findViewById(R.id.answers_container);

        answerButtons[0] = findViewById(R.id.btn_answer1);
        answerButtons[1] = findViewById(R.id.btn_answer2);
        answerButtons[2] = findViewById(R.id.btn_answer3);
        answerButtons[3] = findViewById(R.id.btn_answer4);

        db = AppDatabase.getDatabase(this);

        // Загружаем данные из БД
        loadQuizData();

        // Вешаем один обработчик кликов на все 4 кнопки
        View.OnClickListener answerClickListener = this::handleAnswerClick;
        for (Button btn : answerButtons) {
            btn.setOnClickListener(answerClickListener);
        }
    }

    private void loadQuizData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            allDatabaseWords = db.wordDao().getAllWords();

            long currentTime = System.currentTimeMillis();
            // Пытаемся получить слова, у которых подошел срок повторения по Лейтнеру
            quizWords = db.wordDao().getWordsForLeitnerTest(currentTime);

            // Если таких слов мало (например, всё уже повторил сегодня),
            // берем просто любые изученные слова для тренировки
            if (quizWords == null || quizWords.size() < 4) {
                quizWords = db.wordDao().getAnyLearnedWords();
            }

            runOnUiThread(() -> {
                if (quizWords != null && quizWords.size() >= 4) {
                    progressBar.setMax(Math.min(quizWords.size(), totalRounds));
                    tvCoins.setText(String.valueOf(sessionCoins));
                    startNewRound();
                } else {
                    Toast.makeText(this, "Сначала изучите хотя бы 4 слова в карточках!", Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        });
    }

    private void startNewRound() {
        // Проверяем, не закончилась ли игровая сессия
        if (currentRound >= quizWords.size() || currentRound >= totalRounds) {
            endQuizSession();
            return;
        }

        // Восстанавливаем дефолтный вид кнопок
        for (Button btn : answerButtons) {
            btn.setBackgroundColor(Color.parseColor("#6200EE")); // Стандартный фиолетовый (или твой цвет)
            btn.setEnabled(true);
        }

        // Обновляем прогресс
        progressBar.setProgress(currentRound + 1);

        // Берем текущее правильное слово
        currentCorrectWord = quizWords.get(currentRound);
        tvQuestionWord.setText(currentCorrectWord.wordEn);

        // Генерируем массив из 4 вариантов ответов
        List<String> options = new ArrayList<>();
        options.add(currentCorrectWord.wordRu); // Добавляем 1 правильный

        // Набираем 3 случайных неправильных ответа из общей базы
        List<Word> pool = new ArrayList<>(allDatabaseWords);
        pool.remove(currentCorrectWord); // Чтобы правильный ответ не продублировался
        Collections.shuffle(pool);       // Перемешиваем базу

        for (int i = 0; i < 3; i++) {
            if (i < pool.size()) {
                options.add(pool.get(i).wordRu);
            } else {
                options.add("---"); // На случай, если в базе критически мало слов
            }
        }

        // Перемешиваем сами варианты ответов, чтобы правильный не всегда был первым
        Collections.shuffle(options);

        // Выводим текст на кнопки
        for (int i = 0; i < 4; i++) {
            answerButtons[i].setText(options.get(i));
        }
    }

    private void handleAnswerClick(View v) {
        Button clickedButton = (Button) v;
        String selectedAnswer = clickedButton.getText().toString();

        // Сразу блокируем все кнопки, чтобы пользователь не кликал повторно
        for (Button btn : answerButtons) {
            btn.setEnabled(false);
            btn.setBackgroundColor(Color.GRAY);
            btn.setTextColor(Color.WHITE);
        }

        if (selectedAnswer.equals(currentCorrectWord.wordRu)) {
            // Ответ ВЕРНЫЙ
            clickedButton.setBackgroundColor(Color.GREEN);
            clickedButton.setTextColor(Color.WHITE);
            clickedButton.setTypeface(null, Typeface.BOLD);
            sessionCoins += 10; // Начисляем 10 монеток
            tvCoins.setText(String.valueOf(sessionCoins));


// Система Лейтнера: повышаем ящик и высчитываем время
            Executors.newSingleThreadExecutor().execute(() -> {
                if (currentCorrectWord.status < 4) {
                    currentCorrectWord.status += 1; // Переводим в следующий ящик
                }

                long daysInMillis = 24 * 60 * 60 * 1000L;
                long delay = 1 * daysInMillis; // По умолчанию 1 день (Ящик 1)

                if (currentCorrectWord.status == 2) delay = 3 * daysInMillis;  // Ящик 2: 3 дня
                else if (currentCorrectWord.status == 3) delay = 7 * daysInMillis;  // Ящик 3: 7 дней
                else if (currentCorrectWord.status == 4) delay = 14 * daysInMillis; // Ящик 4: 14 дней

                currentCorrectWord.nextReviewDate = System.currentTimeMillis() + delay;
                db.wordDao().updateWord(currentCorrectWord);
            });
        } else {
            // Ответ НЕВЕРНЫЙ
            clickedButton.setBackgroundColor(Color.RED);
            clickedButton.setTextColor(Color.WHITE);
            clickedButton.setTypeface(null, Typeface.BOLD);
            // Подсвечиваем пользователю, какая кнопка была правильной
            for (Button btn : answerButtons) {
                if (btn.getText().toString().equals(currentCorrectWord.wordRu)) {
                    btn.setBackgroundColor(Color.GREEN);
                    clickedButton.setTextColor(Color.WHITE);
                    clickedButton.setTypeface(null, Typeface.BOLD);

                }
            }

            // Система Лейтнера: штраф. Сбрасываем слово в ящик 0 и заставляем повторить завтра
            Executors.newSingleThreadExecutor().execute(() -> {
                currentCorrectWord.status = 0;
                // Доступно снова через 1 день (или можно поставить System.currentTimeMillis(), чтоб гонять пока не выучит)
                currentCorrectWord.nextReviewDate = System.currentTimeMillis() + (24 * 60 * 60 * 1000L);
                db.wordDao().updateWord(currentCorrectWord);
            });
        }

        // Делаем паузу в 1.5 секунды, чтобы пользователь увидел результат, и переходим к следующему вопросу
        new Handler().postDelayed(() -> {
            currentRound++;
            startNewRound();
        }, 1500);
    }

    private void endQuizSession() {
        // Сохраняем заработанные монетки в SharedPreferences общего баланса приложения
        SharedPreferences prefs = getSharedPreferences("GameData", MODE_PRIVATE);
        int globalCoins = prefs.getInt("coins", 0);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("coins", globalCoins + sessionCoins);
        editor.apply();

        Toast.makeText(this, "Тест завершен! Заработано монет: " + sessionCoins, Toast.LENGTH_LONG).show();
        finish(); // Закрываем экран и возвращаемся на MainActivity
    }
}