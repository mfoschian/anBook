package it.mfx.anbook.ui.activities;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import it.mfx.anbook.MyApp;
import it.mfx.anbook.R;
import it.mfx.anbook.models.Book;
import it.mfx.anbook.ui.HolderFactory;
import it.mfx.anbook.ui.ListRecyclerViewAdapter;
import it.mfx.anbook.ui.Utils;
import it.mfx.anbook.ui.dialogs.MessageDialog;
import it.mfx.anbook.utils.FileUtils;

public class LibraryActivity extends AppCompatActivity {


    private static final int MY_PERMISSIONS_REQUEST = 999;
    private static final String[] permissions = new String[] {
            //Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
            //Manifest.permission.CAMERA,
            //Manifest.permission.ACCESS_FINE_LOCATION
    };

    private void loadBook() {

        boolean has_permission = requestPermissions( permissions );
        if( ! has_permission )
            return;

        // Read permissions ok. Load book
        MyApp.browseForFile(this);
    }

    private void loadBook(String fileContent) {
        app().addBook(fileContent, true, new MyApp.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean succeeded) {
                if( succeeded ) {
                    final Book book = app().getActiveBook();
                    Utils.runOnUIthread(new Utils.UICallback() {
                        @Override
                        public void onUIReady() {
                            reloadData();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {

            }
        });
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



    ArrayList<Book> mBooks;

    interface Listener {
        void onBookSelected(String book_id);
    }

    private class BookViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mBookTitleView;
        public final Button mOpenButton;
        public Book mItem;

        public BookViewHolder(View view) {
            super(view);
            mView = view;
            mBookTitleView = view.findViewById(R.id.book_title);
            mOpenButton = view.findViewById(R.id.choose_book);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mBookTitleView.getText() + "'";
        }
    }

    private ListRecyclerViewAdapter<Book, BookViewHolder, Listener> adapter;

    public static void openLibrary(Activity parent) {
        Intent intent = new Intent(parent, LibraryActivity.class);

        //TODO: call for result and, on result, refresh data: this may avoid activity unload
        //parent.startActivity(intent);
        parent.startActivityForResult(intent,MyApp.IntentRequests.CHOOSE_BOOK);
    }

    private static String return_parameter = "book_id";
    public static String getExitCode(Intent data) {
        if( data == null )
            return null;

        String r = data.getStringExtra(return_parameter);

        return r;
    }

    private void closeWithCode(String book_id) {
        Intent intent = new Intent();
        intent.putExtra(return_parameter, book_id);
        setResult(RESULT_OK, intent);
        //finishActivity(MyApp.IntentRequests.CHOOSE_BOOK);
        finish();
    }

    MyApp app() {
        MyApp app = (MyApp)getApplication();
        return app;
    }

    private void reloadData() {
        app().getBooks(new MyApp.Callback<List<Book>>() {
            @Override
            public void onSuccess(List<Book> result) {
                final List<Book> books = result;

                Utils.runOnUIthread(new Utils.UICallback() {
                    @Override
                    public void onUIReady() {
                        mBooks.clear();
                        mBooks.addAll(books);
                        adapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    private int getBkgColor() {
        int color = 0xFFFFFF;
        TypedValue a = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.windowBackground, a, true);
        if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            // windowBackground is a color
            color = a.data;
        }

        return color;
    }

    private void onChoosedBook(String book_id) {
        closeWithCode(book_id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        View view = findViewById(R.id.book_list);

        if (!(view instanceof RecyclerView)) {
            throw new Error("Wrong xml layout");
        }

        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        mBooks = new ArrayList<>();
        //final int unselCol = getBkgColor();
        final int unselCol = getResources().getColor(R.color.colorPrimary);
        final int selCol = getResources().getColor(R.color.colorAccent);

        adapter = new ListRecyclerViewAdapter<Book, BookViewHolder, Listener>(mBooks, R.layout.item_book,
            new HolderFactory<BookViewHolder>() {
                @Override
                public BookViewHolder createHolder(View view) {
                    return new BookViewHolder(view);
                }
            },
            new ListRecyclerViewAdapter.Binder<Book, BookViewHolder, Listener>() {
                @Override
                public void bind(Book item, final BookViewHolder holder, final Listener listener) {
                    holder.mItem = item;
                    holder.mBookTitleView.setText(item.title);
                    if( item.active ) {
                        holder.mBookTitleView.setTextColor(selCol);
                    }
                    else {
                        holder.mBookTitleView.setTextColor(unselCol);
                    }

                    if (listener != null) {
                        holder.mOpenButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String ev_id = holder.mItem.id;
                                listener.onBookSelected(ev_id);
                            }
                        });
                    }

                }
            },
            new LibraryActivity.Listener() {
                @Override
                public void onBookSelected(String book_id) {
                    onChoosedBook(book_id);
                }
            }
        );

        recyclerView.setAdapter(adapter);
        reloadData();

        Button b = findViewById(R.id.load_button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadBook();
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
