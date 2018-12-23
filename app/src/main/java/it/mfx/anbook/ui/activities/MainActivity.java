package it.mfx.anbook.ui.activities;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.arch.lifecycle.ViewModelProviders;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.InputStream;

import it.mfx.anbook.MyApp;
import it.mfx.anbook.R;
import it.mfx.anbook.models.Book;
import it.mfx.anbook.models.Sentence;
import it.mfx.anbook.ui.Utils;
import it.mfx.anbook.utils.FileUtils;
import it.mfx.anbook.viewmodels.BookViewModel;
import it.mfx.anbook.ui.dialogs.MessageDialog;

public class MainActivity extends AppCompatActivity {

    private MyApp app;
    private BookViewModel viewModel = null;
    private Book activeBook = null;

    private TextView answerBox;


    private boolean bookLoaded() {
        return activeBook != null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        app = (MyApp)getApplication();

        answerBox = findViewById(R.id.answer_box);

        init();
    }

    private void setAnswer( String s ) {
        if( answerBox != null )
            answerBox.setText(s);
    }

    private void setAnswer( int id ) {
        if( answerBox != null )
            answerBox.setText(id);
    }

    private void loadBook() {
        openLibraryMenu();
    }

    private void subscribeUI(BookViewModel modelView) {
        modelView.getActiveBook().observe(this, new Observer<Book>() {
            @Override
            public void onChanged(@Nullable Book book) {
                activeBook = book;
                render(book);
            }
        });

        if( answerBox != null ) {
            answerBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if( bookLoaded() )
                        getAnAnswer();
                    else
                        loadBook();
                }
            });
        }
    }


    private void render(Book book) {

        if( book == null ) {
            setTitle(R.string.no_books_loaded);
            setAnswer(R.string.tap_for_load_a_book);
        }
        else {
            setTitle(book.title);
            setAnswer(R.string.tap_for_an_answer);
        }
    }

    private void init() {

        viewModel = ViewModelProviders.of(this).get(BookViewModel.class);

        subscribeUI(viewModel);
        loadData();
    }

    void loadData() {
        app.getActiveBook(new MyApp.Callback<Book>() {
            @Override
            public void onSuccess(Book result) {
                final Book book = result;
                Utils.runOnUIthread(new Utils.UICallback() {
                    @Override
                    public void onUIReady() {
                        viewModel.setActiveBook(book);
                    }
                });
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

    private void openLibraryMenu() {
        LibraryActivity.openLibrary(this);
    }

    private void getAnAnswer() {
        if( !bookLoaded() ) {
            loadBook();
            return;
        }

        app.getAnAnswer(activeBook, new MyApp.Callback<Sentence>() {
            @Override
            public void onSuccess(Sentence result) {
                if( result != null && result.text != null ) {
                    final String answer = result.text;
                    Utils.runOnUIthread(new Utils.UICallback() {
                        @Override
                        public void onUIReady() {
                            setAnswer( answer );
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        //docListMenuItem = menu.findItem(R.id.action_toggle_list);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.library_menu) {
            openLibraryMenu();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);


        if (requestCode == MyApp.IntentRequests.CHOOSE_BOOK && resultCode == Activity.RESULT_OK) {
            String result = LibraryActivity.getExitCode(resultData);

            if( result != null )
                app.setActiveBook(result, new MyApp.CallbackSimple() {
                    @Override
                    public void onSuccess() {
                        loadData();
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
        }
    }
}
