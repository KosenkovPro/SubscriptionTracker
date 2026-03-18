package pro.kosenkov.subtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "subs.db";
    private static final int DB_VER = 1;
    private static final String TABLE = "subscriptions";

    private static DbHelper instance;

    public static synchronized DbHelper get(Context ctx) {
        if (instance == null) instance = new DbHelper(ctx.getApplicationContext());
        return instance;
    }

    private DbHelper(Context ctx) { super(ctx, DB_NAME, null, DB_VER); }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "start_date INTEGER," +
                "trial_end INTEGER," +
                "price REAL," +
                "billing_cycle TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int o, int n) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    public long insert(Subscription s) {
        ContentValues cv = toCV(s);
        return getWritableDatabase().insert(TABLE, null, cv);
    }

    public void update(Subscription s) {
        getWritableDatabase().update(TABLE, toCV(s), "_id=?",
                new String[]{String.valueOf(s.getId())});
    }

    public void delete(long id) {
        getWritableDatabase().delete(TABLE, "_id=?", new String[]{String.valueOf(id)});
    }

    public List<Subscription> getAll() {
        List<Subscription> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query(TABLE, null, null, null, null, null, "trial_end ASC");
        while (c.moveToNext()) list.add(fromCursor(c));
        c.close();
        return list;
    }

    public Subscription getById(long id) {
        Cursor c = getReadableDatabase().query(TABLE, null, "_id=?",
                new String[]{String.valueOf(id)}, null, null, null);
        Subscription s = c.moveToFirst() ? fromCursor(c) : null;
        c.close();
        return s;
    }

    private ContentValues toCV(Subscription s) {
        ContentValues cv = new ContentValues();
        cv.put("name", s.getName());
        cv.put("start_date", s.getStartDate());
        cv.put("trial_end", s.getTrialEndDate());
        cv.put("price", s.getPrice());
        cv.put("billing_cycle", s.getBillingCycle());
        return cv;
    }

    private Subscription fromCursor(Cursor c) {
        Subscription s = new Subscription();
        s.setId(c.getLong(c.getColumnIndexOrThrow("_id")));
        s.setName(c.getString(c.getColumnIndexOrThrow("name")));
        s.setStartDate(c.getLong(c.getColumnIndexOrThrow("start_date")));
        s.setTrialEndDate(c.getLong(c.getColumnIndexOrThrow("trial_end")));
        s.setPrice(c.getDouble(c.getColumnIndexOrThrow("price")));
        s.setBillingCycle(c.getString(c.getColumnIndexOrThrow("billing_cycle")));
        return s;
    }
}
