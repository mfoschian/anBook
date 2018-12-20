package it.mfx.anbook.models;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "sentences",
        indices = {
                @Index(value = "book_id, sentence_num")
        })
public class Sentence {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String book_id;

    @NonNull
    public int sentence_num;

    public String text;

}
