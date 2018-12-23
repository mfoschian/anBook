package it.mfx.anbook.database;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;


import java.util.List;

import it.mfx.anbook.models.Sentence;

@Dao
public interface SentenceDao {

    @Query("SELECT * from sentences where book_id = :book_id and sentence_num = :sentence_num")
    Sentence getSentence(String book_id, int sentence_num);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Sentence> sentences);

    @Query("DELETE FROM sentences WHERE book_id = :book_id")
    void clearBook(String book_id);

    @Update
    void updateAll(Sentence... sentences);

    @Delete
    void delete(Sentence sentence);
}
