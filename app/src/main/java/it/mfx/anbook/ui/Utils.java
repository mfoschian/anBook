package it.mfx.anbook.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;


public class Utils {

    public static void runActivity(Activity parent, Class<?> cls ) {
        Intent intent = new Intent(parent, cls);
        parent.startActivity(intent);
    }

    public static boolean isUiThread() {
        boolean isUiThread = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? Looper.getMainLooper().isCurrentThread()
                : Thread.currentThread() == Looper.getMainLooper().getThread();

        return isUiThread;
    }

    public interface UICallback {
        void onUIReady();
    }

    public static void runOnUIthread( @NonNull final UICallback cb ) {
        boolean isUiThread = isUiThread();
        if( isUiThread ) {
            cb.onUIReady();
        }
        else {

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    //this runs on the ui thread
                    cb.onUIReady();
                }
            });
        }

    }

    public static void showMsg(final Context ctx, final String msg) {

        runOnUIthread(new UICallback() {
            @Override
            public void onUIReady() {
                Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    public interface ConfirmListener {
        void onPressed();
    }

    public static void confirmYesNo(Context ctx, String title, String message, Integer icon_id, final ConfirmListener yesCb, final ConfirmListener noCb ) {

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        if( title != null )
            builder.setTitle(title);

        if( message != null )
                builder.setMessage(message);

        if( icon_id != null )
                builder.setIcon(icon_id);

        DialogInterface.OnClickListener onYes = null;
        DialogInterface.OnClickListener onNo = null;

        if( yesCb != null ) {
            onYes = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    yesCb.onPressed();
                }
            };
        }

        if( noCb != null ) {
            onNo = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    noCb.onPressed();
                }
            };
        }

        builder.setPositiveButton(android.R.string.yes, onYes );
        builder.setNegativeButton(android.R.string.no, onNo );

        builder.show();
    }

    public static void confirmBase(Context ctx, String title, String message, Integer icon_id, final ConfirmListener yesCb, final ConfirmListener noCb ) {

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        if( title != null )
            builder.setTitle(title);

        if( message != null )
                builder.setMessage(message);

        if( icon_id != null )
                builder.setIcon(icon_id);

        DialogInterface.OnClickListener onYes = null;
        DialogInterface.OnClickListener onNo = null;

        if( yesCb != null ) {
            onYes = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    yesCb.onPressed();
                }
            };
            builder.setPositiveButton(android.R.string.yes, onYes );
        }

        if( noCb != null ) {
            onNo = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    noCb.onPressed();
                }
            };
            builder.setNegativeButton(android.R.string.no, onNo );
        }


        builder.show();
    }

    public static void confirm(Context ctx, String message, final ConfirmListener yesCb ) {
        confirm(ctx, null, message, null, yesCb );
    }

    public static void confirm(Context ctx, String message, Integer icon_id, final ConfirmListener yesCb ) {
        confirm(ctx, null, message, icon_id, yesCb );
    }

    public static void confirm(Context ctx, String title, String message, Integer icon_id, final ConfirmListener yesCb ) {
        confirmBase(ctx, title, message, icon_id, yesCb, new ConfirmListener() {
            @Override
            public void onPressed() {
                Log.i("showmsg", "Pressed no button");
            }
        });
    }

    public static void showModalMsg(Context ctx, String message, final ConfirmListener cb ) {
        confirmBase(ctx, null, message, null, cb, null );
    }
}
