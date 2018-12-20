package it.mfx.anbook;

import android.app.Application;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.logging.Logger;

import it.mfx.anbook.database.AppDatabase;
import it.mfx.anbook.models.Book;
import it.mfx.anbook.models.Sentence;

public class MyApp extends Application {

    private AppDatabase db = null;

/*
    public final class IntentRequests {
        final public static int CHOOSE_ITEM_REQUEST = 8000;
        final public static int EDIT_ITEM_REQUEST = 8001;
        final public static int SHOP_RUN_REQUEST = 8002;
        final public static int PERMISSIONS_REQUEST = 8003;
        final public static int CHOOSE_IMPORT_FILE_REQUEST = 8004;
        final public static int EDIT_EVENT_REQUEST = 8005;
        final public static int EDIT_TAG_REQUEST = 8006;
    }
*/

    AppDatabase db() {
        if (db == null) {
            db = AppDatabase.newInstance(this.getApplicationContext());
        }
        return db;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public boolean addBook(JSONObject jsonBook) {
        return addBook(jsonBook, false);
    }

    public boolean addBook(JSONObject jsonBook, boolean setActive) {

        if(jsonBook == null)
            return false;

        try {

            Book book = new Book();

            book.id = jsonBook.getString("id");
            if( book.id == null ) {
                book.id = AppDatabase.newId();
            }
            book.title = jsonBook.getString("title");
            book.version = jsonBook.optString("version", "0");
            book.author = jsonBook.optString("author", "");
            book.active = setActive;

            JSONArray jsonSentences = jsonBook.getJSONArray("sentences");
            if( jsonSentences == null || jsonSentences.length() == 0 ) {
                Log.e("addBook", "book <" + book.title + "> without sentences");
                return false;
            }

            ArrayList<Sentence> sentences = new ArrayList<>();
            for( int i=0; i < jsonSentences.length(); i++ ) {
                String text = jsonSentences.optString(i,null);
                if( text == null )
                    continue;

                Sentence sentence = new Sentence();
                sentence.book_id = book.id;
                sentence.sentence_num = i;
                sentence.text = text;

                sentences.add(sentence);
            }

            if( sentences.isEmpty() ) {
                Log.e("addBook", "book <" + book.title + "> without valid sentences");
                return false;
            }

            AppDatabase db = db();

            db.bookDao().insertAll(book);
            db.sentenceDao().insertAll(sentences);

            return true;
        }
        catch( JSONException ex ) {
            ex.printStackTrace();
            return false;
        }

    }

}
