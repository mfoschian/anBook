package it.mfx.anbook.viewmodels;


import android.app.Application;
//import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.ViewModel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

//import it.mfx.anbook.MyApp;
import it.mfx.anbook.models.Book;

//public class BookViewModel extends AndroidViewModel {
public class BookViewModel extends ViewModel {

    /*
    public BookViewModel(Application app) {
        super(app);
    }
    */

    private MutableLiveData<Book> book = new MutableLiveData<>();

    public LiveData<Book> getActiveBook() {
        return book;
    }

    public void setActiveBook(Book b) {
        book.setValue(b);
    }

    /*
    public void loadActiveBook() {
        it.mfx.anbook.MyApp app = getApplication();

        app.getActiveBook(new MyApp.Callback<Book>() {
            @Override
            public void onSuccess(Book result) {
                // May be null

                book.setValue(result);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                book.setValue(null);
            }
        });
    }
    */

}
