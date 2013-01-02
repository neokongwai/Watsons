package com.game;
 
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.neogame.R;
 
public class SQLite extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "game.db";	//��Ʈw�W��
	private static final int DATABASE_VERSION = 1;	//��Ʈw����
 
	//private SQLiteDatabase db;
 
	public SQLite(Context context) {	//�غc�l
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
		//�إ�config��ƪ�A�Ա��аѦ�SQL�y�k
		db.execSQL(DATABASE_CREATE_TABLE);
	}
 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//oldVersion=�ª���Ʈw�����FnewVersion=�s����Ʈw����
		db.execSQL("DROP TABLE IF EXISTS score");	//�R���¦�����ƪ�
		onCreate(db);
	}
}