package it.mfx.anbook;

import android.app.Activity;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


import it.mfx.anbook.database.AppDatabase;
import it.mfx.anbook.database.BookDao;
import it.mfx.anbook.models.Book;
import it.mfx.anbook.models.Sentence;


public class MyApp extends Application {

    private AppDatabase db = null;

    public final class IntentRequests {
        final public static int BROWSE_FILE_REQUEST = 8000;
        final public static int CHOOSE_BOOK = 8001;
        //final public static int SHOP_RUN_REQUEST = 8002;
        //final public static int PERMISSIONS_REQUEST = 8003;
        //final public static int CHOOSE_IMPORT_FILE_REQUEST = 8004;
        //final public static int EDIT_EVENT_REQUEST = 8005;
        //final public static int EDIT_TAG_REQUEST = 8006;
    }


    AppDatabase db() {
        if (db == null) {
            db = AppDatabase.newInstance(this.getApplicationContext());
        }
        return db;
    }


    public interface Callback<T> {
        void onSuccess(T result);

        void onError(Exception e);
    }

    public interface CallbackSimple {
        void onSuccess();

        void onError(Exception e);
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    public boolean addBook(JSONObject jsonBook, boolean setActive) {

        if(jsonBook == null)
            return false;

        try {

            Book book = new Book();

            book.id = jsonBook.optString("id", null);
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
            int sentences_count = 0;
            for( int i=0; i < jsonSentences.length(); i++ ) {
                String text = jsonSentences.optString(i,null);
                if( text == null )
                    continue;

                Sentence sentence = new Sentence();
                sentence.book_id = book.id;
                sentence.sentence_num = sentences_count++;
                sentence.text = text;

                sentences.add(sentence);
            }

            if( sentences.isEmpty() ) {
                Log.e("addBook", "book <" + book.title + "> without valid sentences");
                return false;
            }

            return addBook(book, sentences);
        }
        catch( JSONException ex ) {
            ex.printStackTrace();
            return false;
        }

    }

    public boolean addBook(String book_content, boolean setActive) {

        if(book_content == null)
            return false;

        try {
            JSONObject jsonBook = new JSONObject(book_content);
            return addBook(jsonBook, setActive);
        }
        catch( JSONException ex ) {
            // No JSON boon format: try with plain text
        }


        try {

            String[] lines = book_content.split("\n");
            if( lines.length == 0 )
                return false;

            ArrayList<String> phrases = new ArrayList<>();

            Book book = new Book();

            for( int i=0; i<lines.length; i++ ) {
                String line = lines[i].trim();
                if( line.isEmpty() )
                    continue;

                try {
                    if ("[title]".equals(line.toLowerCase().substring(0, 7))) {
                        book.title = line.substring(8);
                    }
                    else if ("[id]".equals(line.toLowerCase().substring(0, 4))) {
                        book.id = line.substring(5);
                    }
                    else if ("[author]".equals(line.toLowerCase().substring(0, 8))) {
                        book.author = line.substring(9);
                    }
                    else if ("[version]".equals(line.toLowerCase().substring(0, 10))) {
                        book.version = line.substring(11);
                    }
                    else {
                        // Sentence
                        phrases.add(line);
                    }
                }
                catch( IndexOutOfBoundsException ex ) {
                    continue;
                }
            }

            if( phrases.isEmpty() ) {
                Log.e("addBook", "book <" + book.title + "> without valid sentences");
                return false;
            }

            ArrayList<Sentence> sentences = new ArrayList<>();
            book.active = setActive;
            book.sentences_count = 0;

            for( String phrase : phrases ) {
                Sentence s = new Sentence();
                s.book_id = book.id;
                s.text = phrase;
                s.sentence_num = book.sentences_count;
                book.sentences_count += 1;
                sentences.add(s);
            }

            return addBook(book, sentences);
        }
        catch( Exception ex ) {
            ex.printStackTrace();
            return false;
        }

    }

    public boolean addBook(@NonNull Book book, @NonNull List<Sentence> sentences) {

        if( book.title == null || sentences.isEmpty() )
            return false;

        try {

            book.sentences_count = sentences.size();

            AppDatabase db = db();

            db.bookDao().insertAll(book);
            db.sentenceDao().insertAll(sentences);

            return true;
        }
        catch( Exception ex ) {
            ex.printStackTrace();
            return false;
        }

    }

    public void setActiveBook( String book_id ) {
        if( book_id == null )
            return;

        BookDao dao = db().bookDao();

        dao.clearActive();
        dao.setActive(book_id);
    }

    public void setActiveBook(final String book_id, final MyApp.CallbackSimple cb ) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    setActiveBook(book_id);
                    if (cb != null)
                        cb.onSuccess();
                }
                catch (Exception err) {
                    if (cb != null)
                        cb.onError(err);
                }
            }
        });
    }

    public Book getActiveBook() {
        AppDatabase db = db();
        return db.bookDao().getActiveSync();
    }

    public void getBooks(final Callback<List<Book>> cb) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Book> books = db().bookDao().getAllSync();
                    if (cb != null)
                        cb.onSuccess(books);
                }
                catch (Exception err) {
                    if (cb != null)
                        cb.onError(err);
                }
            }
        });

    }

    public void getActiveBook(final Callback<Book> cb) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Book book = getActiveBook();
                    if (cb != null)
                        cb.onSuccess(book);
                }
                catch (Exception err) {
                    if (cb != null)
                        cb.onError(err);
                }
            }
        });
    }

    public void addFakeData( final Callback<Boolean> cb) {
        String json = "{ \"title\": \"First book\"," +
                "\"version\": \"0.1\"," +
                "\"sentences\" : [ \"Prima frase\",\"Seconda frase\", \"Terza frase\"]" +
        "}";

        addBook(json, true, cb );
    }


    public void addBook(final String content, final boolean setActive, final Callback<Boolean> cb) {

            AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    addBook(content, setActive);
                    if (cb != null)
                        cb.onSuccess(true);
                } catch (Exception err) {
                    if (cb != null)
                        cb.onError(err);
                }
            }
        });
    }


    public void getAnAnswer(@NonNull final Book book, @NonNull final Callback<Sentence> cb) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    int sentence_num = (int)Math.floor(Math.random() * (double) book.sentences_count);
                    Sentence sentence = db().sentenceDao().getSentence(book.id, sentence_num);
                    cb.onSuccess(sentence);
                }
                catch (Exception err) {
                    cb.onError(err);
                }
            }
        });
    }


    static public void browseForFile(Activity parent) {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        //intent.setType("image/*");
        //intent.setType("text/xml");   //XML file only
        intent.setType("*/*");      //all files
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            parent.startActivityForResult(intent, IntentRequests.BROWSE_FILE_REQUEST);
        }
        catch( ActivityNotFoundException e) {
            Log.d("OPENFILE", "No activity to handle intent ACTION_OPEN_DOCUMENT");
        }
    }

}
