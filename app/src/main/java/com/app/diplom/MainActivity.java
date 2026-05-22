//package com.app.diplom;
//
//import android.os.Bundle;
//import android.view.View;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//
//public class MainActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_main);
////        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
////            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
////            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
////            return insets;
////        });
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            // Если нужен ТОЛЬКО верхний отступ (от статус-бара):
//            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
//            return insets;
//        });
//    }
//
//    public void click(View view){
//        int pressedId = view.getId();
//
//        if (pressedId == R.id.btn_learn){
//            setContentView(R.layout.learning_screen);
//
//            View mainView = findViewById(R.id.main1);
//            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main1), (v, insets) -> {
//                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//                v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
//                return insets;
//            });
//            ViewCompat.requestApplyInsets(mainView);
//        }
//        if (pressedId == R.id.btn_test){
//            setContentView(R.layout.test_screen);
//
//            View mainView = findViewById(R.id.main2);
//            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main2), (v, insets) -> {
//                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//                v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
//                return insets;
//            });
//            ViewCompat.requestApplyInsets(mainView);
//        }
//        if (pressedId == R.id.closeBtn){
//            setContentView(R.layout.activity_main);
//
//            View mainView = findViewById(R.id.main);
//            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//                v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
//                return insets;
//            });
//            ViewCompat.requestApplyInsets(mainView);
//        }
//
//    }
//}

package com.app.diplom;

        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.os.Bundle;
        import android.os.Handler;
        import android.view.View;
        import android.widget.Button;
        import android.widget.ProgressBar;
        import android.widget.TextView;
        import androidx.appcompat.app.AppCompatActivity;
        import java.util.List;
        import java.util.concurrent.Executors;

        import android.view.animation.AlphaAnimation;
        import android.view.animation.Animation;

public class MainActivity extends AppCompatActivity {

    private TextView tvCoins;
    private TextView tvHello;
    private TextView tvProgressText;
    private ProgressBar progressBar;
    private AppDatabase db;

    private final Handler greetingHandler = new Handler();
    private boolean isEnglish = false;
    private Runnable greetingRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализируем элементы интерфейса
        tvCoins = findViewById(R.id.tv_main_coins);
        tvProgressText = findViewById(R.id.tv_progress_text);
        progressBar = findViewById(R.id.main_pb_progress);
        tvHello = findViewById(R.id.textHello);

        // Инициализируем базу данных Room
        db = AppDatabase.getDatabase(this);

        greetingRunnable = new Runnable() {
            @Override
            public void run() {

                // 1. Создаем анимацию исчезновения (от 1.0 - видимый, до 0.0 - невидимый)
                AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
                fadeOut.setDuration(400); // Длительность анимации в миллисекундах (0.4 секунды)

                // Слушатель для анимации: когда текст исчезнет, мы его поменяем и проявим обратно
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        // Этот код сработает СТРОГО когда текст полностью исчезнет
                        if (isEnglish) {
                            tvHello.setText("Hello!");
                        } else {
                            tvHello.setText("Привет!");
                        }
                        isEnglish = !isEnglish;

                        // 2. Создаем анимацию появления (от 0.0 до 1.0)
                        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                        fadeIn.setDuration(400);
                        tvHello.startAnimation(fadeIn);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                // Запускаем первую часть (исчезновение)
                tvHello.startAnimation(fadeOut);

                // Повторяем весь цикл через 5 секунд
                greetingHandler.postDelayed(this, 1500);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCoinsDisplay();
        updateProgressDisplay();
        greetingHandler.postDelayed(greetingRunnable, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // ОСТАНАВЛИВАЕМ таймер, когда пользователь ушел на другой экран,
        // чтобы приложение не тратило батарею впустую в фоне
        greetingHandler.removeCallbacks(greetingRunnable);
    }

    // Метод для загрузки и отображения монеток
    private void updateCoinsDisplay() {
        SharedPreferences prefs = getSharedPreferences("GameData", MODE_PRIVATE);
        int coins = prefs.getInt("coins", 0);
        tvCoins.setText(String.valueOf(coins));
    }

    // Метод для расчета и отображения прогресса слов из Room
    private void updateProgressDisplay() {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Получаем все слова из базы данных
            List<Word> allWords = db.wordDao().getAllWords();

            int totalWords = allWords.size();
            int learnedWords = 0;

            // Считаем, сколько слов пользователь уже начал учить (status > 0)
            for (Word word : allWords) {
                if (word.status > 0) {
                    learnedWords++;
                }
            }

            // Переменные для передачи в главный поток (UI)
            final int finalTotal = totalWords;
            final int finalLearned = learnedWords;

            // Возвращаемся в главный поток, чтобы обновить элементы UI
            runOnUiThread(() -> {
                if (finalTotal > 0) {
                    progressBar.setMax(finalTotal);
                    progressBar.setProgress(finalLearned);
                    tvProgressText.setText("Изучено " + finalLearned + " из " + finalTotal + " слов");
                } else {
                    // Если база еще пустая
                    progressBar.setProgress(0);
                    tvProgressText.setText("В базе пока нет слов");
                }
            });
        });
    }
    public void click(View view) {
        int pressedId = view.getId();

        if (pressedId == R.id.btn_learn) {
            Intent intent = new Intent(MainActivity.this, LearnActivity.class);
            startActivity(intent);
        }
        if (pressedId == R.id.btn_test) {
            Intent intent = new Intent(MainActivity.this, QuizActivity.class);
            startActivity(intent);
        }
    }
}