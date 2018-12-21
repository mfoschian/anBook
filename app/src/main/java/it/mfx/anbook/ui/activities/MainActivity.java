package it.mfx.anbook.ui.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import it.mfx.anbook.MyApp;
import it.mfx.anbook.R;
import it.mfx.anbook.models.Book;

public class MainActivity extends AppCompatActivity {

    private MyApp app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        app = (MyApp)getApplication();
        init();
    }


    private void init() {

        app.getActiveBookAsync(new MyApp.Callback<Book>() {
            @Override
            public void onSuccess(Book book) {
                Log.i("MainActivity", "got the book: " + (book == null ? "none" : book.title));
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });



    }
    private void init_debug() {
        app.addFakeData(new MyApp.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {

                Book book = app.getActiveBook();

            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });


    }
}
