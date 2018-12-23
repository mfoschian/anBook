package it.mfx.anbook.database;


import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import it.mfx.anbook.models.Book;


@Dao
public interface BookDao {

    @Query("SELECT * FROM books")
    List<Book> getAllSync();

    @Query("SELECT * FROM books")
    LiveData<List<Book>> getAll();

    @Query("SELECT * FROM books WHERE id = :book_id")
    Book getBookSync(String book_id);

    @Query("SELECT count(*) FROM sentences WHERE book_id = :book_id")
    int getBookSentencesCount(String book_id);

    @Query("SELECT * FROM books WHERE active = 1 LIMIT 1")
    Book getActiveSync();

    @Query("SELECT * FROM books WHERE active = 1 LIMIT 1")
    LiveData<Book> getActive();

    @Query("UPDATE books SET active = 0")
    void clearActive();

    @Query("UPDATE books SET active = 1 WHERE id = :book_id")
    void setActive(String book_id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Book... books);

    @Update
    void updateAll(Book... books);

    @Delete
    void delete(Book book);


/*
    @Query("SELECT t.*, t2.childs FROM tags t "
            + " LEFT OUTER JOIN (SELECT parent_id, count(*) as childs FROM tags GROUP BY parent_id) t2 "
            + " ON t2.parent_id = t.id"
            + " order by priority desc, label")
    LiveData<List<Tag>> getAll();

    @Query("SELECT t.*, t2.childs FROM tags t"
            + " LEFT OUTER JOIN (SELECT parent_id, count(*) as childs FROM tags GROUP BY parent_id) t2 "
            + " ON t2.parent_id = t.id"
            + " where id LIKE :id ")
    Tag findById(String id);

    @Query("SELECT t.*, t2.childs FROM tags t "
            + " LEFT OUTER JOIN (SELECT parent_id, count(*) as childs FROM tags GROUP BY parent_id) t2 "
            + " ON t2.parent_id = t.id"
            + " where t.parent_id LIKE :tag_id")
    List<Tag> findByParent(String tag_id);

    @Query("SELECT COUNT(*) from tags")
    int countItems();

    @Query("SELECT COUNT(*) from moves m left join tags t on (m.tag_id = t.id) where m.tag_id = :tag_id")
    int countMovesOf(String tag_id);

    @Query("SELECT m.* from moves m left join tags t on (m.tag_id = t.id) where m.tag_id = :tag_id")
    List<Move> getMovesOf(String tag_id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Tag... items);
*/
}
