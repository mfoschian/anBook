package it.mfx.anbook.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;



@Entity(tableName = "books",
        indices = {
                @Index(value = "active")
        })
public class Book {

    @PrimaryKey //(autoGenerate = true)
    @NonNull
    public String id;

    public String title;
    public String version;
    public String author;
    //public String url;
    public boolean active = true;

}

