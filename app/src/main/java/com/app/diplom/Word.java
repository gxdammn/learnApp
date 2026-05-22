package com.app.diplom;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "words")
public class Word {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String wordEn
    public String wordRu;
    public String imageUrl;
    public int isLearned;
    public int status;
    public long nextReviewDate;

    public Word(String wordEn, String wordRu, String imageUrl) {
        this.wordEn = wordEn;
        this.wordRu = wordRu;
        this.imageUrl = imageUrl;
        this.isLearned = 0;
        this.status = 0;
        this.nextReviewDate = System.currentTimeMillis();
    }
}
