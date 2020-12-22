package io.github.dinh.pokemongame;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class PlayerDB {
    // database constants
    public static final String DB_NAME = "player.sqlite";
    public static final int    DB_VERSION = 1;
    private static final String TABLE_PLAYERS = "players";
    private static final String ID = "id";

    private static class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name,
                        SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // create table
            db.execSQL("CREATE TABLE players (id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , " +
                    "name VARCHAR NOT NULL , " +
                    "points INTEGER NOT NULL  DEFAULT 0)");

            db.execSQL("INSERT INTO players (name, points) VALUES ('Me', 0)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db,
                              int oldVersion, int newVersion) {

            db.execSQL("DROP TABLE \"players\"");
            Log.d("Task list", "Upgrading db from version "
                    + oldVersion + " to " + newVersion);

            onCreate(db);
        }
    }

    // database and database helper objects
    private SQLiteDatabase db;
    public DBHelper dbHelper;

    // constructor
    public PlayerDB(Context context) {
        dbHelper = new DBHelper(context, DB_NAME, null, DB_VERSION);
        openWriteableDB();
        closeDB();
    }
    // private methods
    private void openReadableDB() {
        db = dbHelper.getReadableDatabase();
    }

    private void openWriteableDB() {
        db = dbHelper.getWritableDatabase();
    }

    private void closeDB() {
        if (db != null)
            db.close();
    }

    public ArrayList<HashMapToString> getPlayers(){
        ArrayList<HashMapToString> data =
                new ArrayList<HashMapToString>();
        openReadableDB();
        Cursor cursor = db.rawQuery("SELECT * FROM players ORDER BY id",null );
        while (cursor.moveToNext()) {
            HashMapToString map = new HashMapToString("name");
            map.put("id", cursor.getString(0));
            map.put("name", cursor.getString(1));
            map.put("points", cursor.getString(2));
            data.add(map);
        }
        cursor.close();
        closeDB();

        return data;
    }

    public ArrayList<HashMapToString> getPlayersOrderByPoints(){
        ArrayList<HashMapToString> data =
                new ArrayList<HashMapToString>();
        openReadableDB();
        Cursor cursor = db.rawQuery("SELECT * FROM players ORDER BY points DESC",null );
        while (cursor.moveToNext()) {
            HashMapToString map = new HashMapToString("name");
            map.put("id", cursor.getString(0));
            map.put("name", cursor.getString(1));
            map.put("points", cursor.getString(2));
            data.add(map);
        }
        cursor.close();
        closeDB();

        return data;
    }

    public void insertPlayer(String sName)throws Exception{

        openWriteableDB();
        ContentValues content = new ContentValues();
        content.put("name", sName);
        long nResult = db.insert("players",null, content);
        if(nResult == -1) throw new Exception("no data");
    }

    public void updatePoints(int id, int points){
        openWriteableDB();
        ContentValues content = new ContentValues();
        content.put("points",points);

        db.update(TABLE_PLAYERS, content, ID + " = ? " ,
                new String[]{String.valueOf(id)});
    }

}