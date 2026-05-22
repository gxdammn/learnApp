package com.app.diplom;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import java.util.List;
import java.util.concurrent.Executors;

public class LearnActivity extends AppCompatActivity {

    private TextView tvForeign, tvTranslation, tvCounter;
    private ImageView ivPicture;
    private Button btnNext;

    private AppDatabase db;
    private List<Word> wordsToLearn;
    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.learning_screen);

        tvForeign = findViewById(R.id.tv_learn_foreign);
        tvTranslation = findViewById(R.id.tv_learn_translation);
        tvCounter = findViewById(R.id.tv_word_counter);
        ivPicture = findViewById(R.id.image_word);
        btnNext = findViewById(R.id.btn_next_word);

        db = AppDatabase.getDatabase(this);

        loadSmartWords();

        btnNext.setOnClickListener(v -> {
            currentIndex++;
            displayWord();
        });
    }

    private void loadSmartWords() {
        Executors.newSingleThreadExecutor().execute(() -> {

            wordsToLearn = db.wordDao().getSmartLearnSequence();

            runOnUiThread(() -> {
                if (wordsToLearn != null && !wordsToLearn.isEmpty()) {
                    displayWord();
                } else {
                    Toast.makeText(this, "База данных абсолютно пуста. Добавьте слова!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });
    }
    private void displayWord() {
        if (currentIndex < wordsToLearn.size()) {
            Word currentWord = wordsToLearn.get(currentIndex);

            tvForeign.setText(currentWord.wordEn);
            tvTranslation.setText(currentWord.wordRu);
            tvCounter.setText("Слово " + (currentIndex + 1) + " из " + wordsToLearn.size());

            Glide.with(this)
                    .load(currentWord.imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.stat_notify_error)
                    .into(ivPicture);

            if (currentWord.isLearned == 0) {
                currentWord.isLearned = 1;
                currentWord.status = 0;
                currentWord.nextReviewDate = System.currentTimeMillis();

                Executors.newSingleThreadExecutor().execute(() -> {
                    db.wordDao().updateWord(currentWord);
                });
            }

        } else {
            Toast.makeText(this, "Вы просмотрели абсолютно все слова", Toast.LENGTH_LONG).show();
            currentIndex = 0;
            loadSmartWords();
        }
    }
    public void clickClose(View view) {
        finish();
    }
}