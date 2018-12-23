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
import android.view.View;
import android.widget.TextView;

import java.io.FileReader;
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

    private static final int MY_PERMISSIONS_REQUEST = 999;
    private static final String[] permissions = new String[] {
            //Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
            //Manifest.permission.CAMERA,
            //Manifest.permission.ACCESS_FINE_LOCATION
    };



    private MyApp app;
    private BookViewModel viewModel = null;
    private Book activeBook = null;

    private TextView titleBox;
    private TextView answerBox;

    private boolean bookLoaded() {
        return activeBook != null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        app = (MyApp)getApplication();

        titleBox = findViewById(R.id.title_box);
        answerBox = findViewById(R.id.answer_box);

        init();
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
        if( titleBox == null || answerBox == null )
            return;

        if( book == null ) {
            titleBox.setText(R.string.no_books_loaded);
            answerBox.setText(R.string.tap_for_load_a_book);
        }
        else {
            titleBox.setText(book.title);
            answerBox.setText(R.string.tap_for_an_answer);
        }
    }

    private void init() {

        viewModel = ViewModelProviders.of(this).get(BookViewModel.class);

        subscribeUI(viewModel);

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
                            answerBox.setText( answer );
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

    private void loadBook() {
        // TODO

        boolean has_permission = requestPermissions( permissions );
        if( ! has_permission )
            return;

        // Read permissions ok. Load book
        MyApp.browseForFile(this);
    }

    private void loadBook(String fileContent) {
        app.addBook(fileContent, true, new MyApp.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean succeeded) {
                if( succeeded ) {
                    final Book book = app.getActiveBook();
                    Utils.runOnUIthread(new Utils.UICallback() {
                        @Override
                        public void onUIReady() {
                            viewModel.setActiveBook(book);
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    private void showMsg( String msg, String title, MessageDialog.Listener listener ) {
        //Toast.makeText(this, msg,Toast.LENGTH_LONG).show();
        MessageDialog.showMessage(this,msg,title, listener );
    }
    private void showMsg( String msg, MessageDialog.Listener listener ) {
        //Toast.makeText(this, msg,Toast.LENGTH_LONG).show();
        showMsg(msg,"",listener);
    }
    private void showMsg( String msg ) {
        showMsg(msg,null,null);
    }
    private void showMsg( String msg, String title ) {
        showMsg(msg, title, null);
    }


    private boolean requestPermissions( String[] permissions ) {

        Activity thisActivity = this;

        boolean hasAllPermissions = true;
        for( int i=0; i<permissions.length; i++ ) {
            String permission = permissions[i];

            if (ContextCompat.checkSelfPermission( thisActivity, permission )
                    != PackageManager.PERMISSION_GRANTED) {

                hasAllPermissions = false;
                break;
            }
        }

        if(!hasAllPermissions) {

            ActivityCompat.requestPermissions(thisActivity,
                    permissions,
                    MY_PERMISSIONS_REQUEST);

            return false;
        } else {
            // Permission has already been granted
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] allowedPermissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, allowedPermissions, grantResults);

        if( requestCode == MY_PERMISSIONS_REQUEST ) {
            int allowedCount = 0;
            for( int i = 0; i < permissions.length; i++) {
                String neededPermission = permissions[i];
                for( int j = 0; j < allowedPermissions.length; j++ ) {
                    String allowedPermission = allowedPermissions[j];
                    if (neededPermission.equals(allowedPermission) && grantResults[i] == 0) {
                        allowedCount++;
                        break;
                    }
                }
            }

            if( allowedCount == permissions.length )
                // All permissions granted
                loadBook();
            else
                showMsg(getString(R.string.permissions_required));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        if( requestCode == MyApp.IntentRequests.BROWSE_FILE_REQUEST  && resultCode == Activity.RESULT_OK ) {

            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i("MAPSEARCH", "Uri: " + uri.toString());

                try {

                    ContentResolver r = getContentResolver();
                    InputStream is = r.openInputStream(uri);

                    String fileContent = FileUtils.getStringFromFile(is);
                    loadBook(fileContent);

                }
                catch( Exception e ) {
                    e.printStackTrace();
                }
            }

        }
    }
}
