package it.mfx.anbook.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.migration.Migration;
import android.support.annotation.NonNull;

public class SimpleMigration extends Migration {

    private String[] statements;

    public SimpleMigration(int min, int max, @NonNull String single_sql ) {
        super(min, max);
        statements = new String[] { single_sql };
    }

    public SimpleMigration(int min, int max, @NonNull String[] sql_statements ) {
        super(min, max);
        statements = sql_statements;
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {

        try {
            for (String sql : statements) {
                database.execSQL(sql);
            }
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
    }
}