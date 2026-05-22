package com.app.diplom;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface WordDao {

    @Insert
    void insert(Word word);
    @Insert
    void insertAll(Word... words);

    @Update
    void updateWord(Word word);

    @Query("SELECT * FROM words")
    List<Word> getAllWords();

    // СИСТЕМЕ ЛЕЙТНЕРА
    // берем только те слова что уже открывали (isLearned = 1) и у которых наступило повторения (nextReviewDate <= currentTime)
    @Query("SELECT * FROM words WHERE isLearned = 1 AND nextReviewDate <= :currentTime ORDER BY RANDOM() LIMIT 10")
    List<Word> getWordsForLeitnerTest(long currentTime);

    // если по лейтнеру повторять пока нечего берем любые изученные слова
    @Query("SELECT * FROM words WHERE isLearned = 1 ORDER BY RANDOM() LIMIT 10")
    List<Word> getAnyLearnedWords();

    @Query("SELECT * FROM words ORDER BY " +
            "CASE " +
            "  WHEN isLearned = 1 AND status = 0 THEN 1 " + // ошибки
            "  WHEN isLearned = 0 THEN 2 " +                // после новые слова
            "  ELSE 3 " +                                   // весь остальной шлак
            "END, id ASC")
    List<Word> getSmartLearnSequence();
}