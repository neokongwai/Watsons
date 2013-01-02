package com.game;
 
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.neogame.R;
 
public class SQLite extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "game.db";	//資料庫名稱
	private static final int DATABASE_VERSION = 1;	//資料庫版本
 
	//private SQLiteDatabase db;
 
	public SQLite(Context context) {	//建構子
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		//db = this.getWritableDatabase();
	}
 
	@Override
	public void onCreate(SQLiteDatabase db) {
		String DATABASE_CREATE_TABLE =
		    "create table score ("
		        + "_ID INTEGER PRIMARY KEY,"
		        + "name TEXT,"
		        + "value INTEGER"
		    + ");";
		//建立config資料表，詳情請參考SQL語法
		db.execSQL(DATABASE_CREATE_TABLE);
	}
 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//oldVersion=舊的資料庫版本；newVersion=新的資料庫版本
		db.execSQL("DROP TABLE IF EXISTS score");	//刪除舊有的資料表
		onCreate(db);
	}
}