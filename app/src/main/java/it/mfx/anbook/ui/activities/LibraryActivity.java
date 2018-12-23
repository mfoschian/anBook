package it.mfx.anbook.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.mfx.anbook.MyApp;
import it.mfx.anbook.R;
import it.mfx.anbook.models.Book;
import it.mfx.anbook.ui.HolderFactory;
import it.mfx.anbook.ui.ListRecyclerViewAdapter;
import it.mfx.anbook.ui.Utils;

public class LibraryActivity extends AppCompatActivity {

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

                        if (mBooks.size() == 0) {
                            String msg = getResources().getString(R.string.no_books_loaded);
                            Utils.showModalMsg(LibraryActivity.this, msg, new Utils.ConfirmListener() {
                                @Override
                                public void onPressed() {
                                    //finish();
                                }
                            });
                        }

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
        final int bkgColor = getBkgColor();

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
                        holder.mBookTitleView.setTextColor(getResources().getColor(R.color.colorPrimary));
                    }
                    else {
                        holder.mBookTitleView.setTextColor(bkgColor);
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

    }

}
