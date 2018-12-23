package it.mfx.anbook.ui.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import it.mfx.anbook.R;


public class MessageDialog {

    public interface Listener {
        void onOk();
    }

    public void show( final Context context, String message, String title, final Listener listener ) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if( title != null )
                builder.setTitle(title);

        builder.setMessage(message)
                //.setNegativeButton(R.string.dialog_no, null)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (listener != null)
                            listener.onOk();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void showMessage(@NonNull final Context context, @NonNull String message) {
        showMessage(context, message, (Listener)null);
    }

    public static void showMessage(@NonNull final Context context, @NonNull String message, @NonNull String title ) {
        showMessage(context, message, title, null);
    }
    public static void showMessage(@NonNull final Context context, @NonNull String message, Listener listener) {
        String title = context.getString(R.string.error);
        showMessage(context, message, title, listener);
    }

    public static void showMessage(@NonNull final Context context, @NonNull String message, @NonNull String title, Listener listener) {
        MessageDialog d = new MessageDialog();
        d.show(context,message, title, listener);
    }
}
