package com.app.diplom;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import java.util.concurrent.Executors;

@Database(entities = {Word.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract WordDao wordDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "word_database")
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    Executors.newSingleThreadExecutor().execute(() -> {
                                        WordDao dao = getDatabase(context).wordDao();
                                        dao.insertAll(
                                                new Word("Apple", "Яблоко", "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a6/Pink_lady_and_cross_section.jpg/1920px-Pink_lady_and_cross_section.jpg?utm_source=commons.wikimedia.org&utm_campaign=index&utm_content=thumbnail&_=20090122010700"),
                                                new Word("House", "Дом", "https://avatars.mds.yandex.net/get-altay/19580059/2a0000019cd3395de63b47bcf66a5c379a74/L_height"),
                                                new Word("Cat", "Кот", "https://i.pinimg.com/736x/e8/23/e0/e823e08eb26ea8cdb30f4174bebcebc7.jpg"),
                                                new Word("Monkey", "Обезьяна", "https://c.tenor.com/2Q0N3B5zo3MAAAAd/tenor.gif"),
                                                new Word("Disconnect", "Отключить", "https://memepedia.ru/wp-content/uploads/2020/06/sinij-provod-interneta-mem-4.jpg"),
                                                new Word("Anomaly", "Аномалия", "https://memepedia.ru/wp-content/uploads/2026/04/images.jpg")
                                        );
                                    });
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}