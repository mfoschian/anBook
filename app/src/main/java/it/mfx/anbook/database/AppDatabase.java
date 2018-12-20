package it.mfx.anbook.database;


import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;

import java.util.UUID;

import it.mfx.anbook.models.Book;
import it.mfx.anbook.models.Sentence;

@Database(entities = {Book.class, Sentence.class }, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static String dbName = "anbookDB";

    public abstract BookDao bookDao();
    public abstract SentenceDao sentenceDao();

    public static AppDatabase newInstance(Context context) {

        //Migration M_01_02 = new SimpleMigration(1,2, "CREATE INDEX index_tags_label ON tags(label)");
        //Migration M_02_03 = new SimpleMigration(2,3, "ALTER TABLE tags ADD childs INTEGER NOT NULL DEFAULT 0;");

        RoomDatabase.Builder<AppDatabase> b = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, dbName);
        AppDatabase db = b
                //.addMigrations( M_01_02, M_02_03 )
                .fallbackToDestructiveMigration()
                .build();

        return db;
    }


    public static String newId() {
        return UUID.randomUUID().toString();
    }
}
