package com.example.vietnamdicttemp;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

public class EnglishVietDB extends SQLiteOpenHelper {
	private static EnglishVietDB sInstance;

	private static String DB_PATH = "";
	private static String DB_NAME = "database.sqlite";
	public static String TABLE_PHRASE = "phrase";

	private Map<String, String> mAliasMap;

	private String tag = "EnglishVietDB";
	private final Context context;
	private SQLiteDatabase db;

	public SQLiteDatabase getDb() {
		return db;
	}

	public void setDb(SQLiteDatabase db) {
		this.db = db;
	}

	public String KEY_ROWID = "_id";
	public String KEY_WORD = "l_from";
	public static String KEY_DATA = "l_to";
	public String KEY_FAVORITE = "favorite";

	public static synchronized EnglishVietDB getInstance(Context context) {

		// Use the application context, which will ensure that you
		// don't accidentally leak an Activity's context.
		// See this article for more information: http://bit.ly/6LRzfx
		if (sInstance == null) {
			sInstance = new EnglishVietDB(context.getApplicationContext());
		}
		return sInstance;
	}

	public EnglishVietDB(Context context) {
		super(context, DB_NAME, null, 1);
		this.context = context;
		if (android.os.Build.VERSION.SDK_INT >= 17) {
			DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
			Log.d(tag, ">=17: " + DB_PATH);
		} else {
			DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";

//			Log.d(tag, "<=17" + DB_PATH);
		}
		String path = context.getFilesDir().getPath();
		Log.d(tag, "path: " + path);
		mAliasMap = new HashMap<String, String>();
		// Unique id for the each Suggestions ( Mandatory )
		mAliasMap.put("_ID", KEY_ROWID + " as " + "_id");
		// Text for Suggestions ( Mandatory )
		mAliasMap.put(SearchManager.SUGGEST_COLUMN_TEXT_1, KEY_WORD + " as "
				+ SearchManager.SUGGEST_COLUMN_TEXT_1);
		// Icon for Suggestions ( Optional )
		// mAliasMap.put(SearchManager.SUGGEST_COLUMN_ICON_1, KEY_ROWID + " as "
		// + SearchManager.SUGGEST_COLUMN_ICON_1);

		// This value will be appended to the Intent data on selecting an item
		// from Search result or Suggestions ( Optional )
		mAliasMap.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, KEY_ROWID
				+ " as " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
		mAliasMap.put(KEY_DATA, KEY_DATA);
		mAliasMap.put(KEY_FAVORITE, KEY_FAVORITE);
	}

	//get list of sugegestion words
	public Cursor getSuggestionWords(String[] selectionArgs, int limit) {
		if (limit < 0) {
			limit = 1;
		}
		String selection = KEY_WORD + " like ? ";
		if (selectionArgs != null) {
			selectionArgs[0] = selectionArgs[0] + "%";
			Log.d(this.getClass().getName(), selectionArgs[0]);
		}
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setProjectionMap(mAliasMap);
		queryBuilder.setTables(TABLE_PHRASE);
		Cursor c = queryBuilder.query(db, new String[] { "_ID",
				SearchManager.SUGGEST_COLUMN_TEXT_1,
				// SearchManager.SUGGEST_COLUMN_ICON_1,
				SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID
				 }, selection, selectionArgs, null, null,
				"_id" + " asc ", limit + "");
		Log.d(this.getClass().getName(), c.getColumnName(0));
		Log.d(this.getClass().getName(), c.getColumnName(1));
		return c;

	}

	public Cursor getFavorites(String[] selectionArgs, int limit) {
		if (limit < 0) {
			limit = 1;
		}
//		String selection = KEY_FAVORITE+" IS NULL";
		String selection = KEY_FAVORITE+"> 0";
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setProjectionMap(mAliasMap);
		queryBuilder.setTables(TABLE_PHRASE);
		Cursor c = queryBuilder.query(db, new String[] { "_ID",
				SearchManager.SUGGEST_COLUMN_TEXT_1,
				// SearchManager.SUGGEST_COLUMN_ICON_1,
				SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID,
				KEY_DATA,KEY_FAVORITE}, selection,
				null, null, null, "_id" + " asc ", limit + "");
		Log.d(this.getClass().getName(), c.getColumnName(0));
		Log.d(this.getClass().getName(), c.getColumnName(1));
		Log.d(this.getClass().getName(), c.getColumnName(2));
		Log.d(this.getClass().getName(), c.getColumnName(3));
		return c;

	}

	// enter when display suggestion
	public Cursor getTopOfListSugWords(String[] selectionArgs) {
		String selection = KEY_WORD + " like ? ";
		if (selectionArgs != null) {
			selectionArgs[0] = selectionArgs[0] + "%";
			Log.d(this.getClass().getName(), selectionArgs[0]);
		}
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setProjectionMap(mAliasMap);
		queryBuilder.setTables(TABLE_PHRASE);
		int limit = 1;
		Cursor c = queryBuilder.query(db, new String[] { "_ID",
		/*
		 * SearchManager.SUGGEST_COLUMN_TEXT_1, //
		 * SearchManager.SUGGEST_COLUMN_ICON_1,
		 * SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID,
		 */
				KEY_DATA,KEY_FAVORITE }, selection, selectionArgs, null, null, "_id" + " asc ",
				limit + "");
		Log.d(this.getClass().getName(), c.getColumnName(0));
		Log.d(this.getClass().getName(), c.getColumnName(1));
		return c;

	}

	// query when click on one suggestion
	public Cursor getWordById(String id) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(TABLE_PHRASE);
		Cursor c = queryBuilder.query(db, new String[] { KEY_ROWID, KEY_DATA,KEY_FAVORITE },
				"_id = ?", new String[] { id }, null, null, null, "1");
		return c;
	}

	// Creates a empty database on the system and rewrites it with your own
	// database.
	public void create() throws IOException {
		boolean dbExist = checkDataBase();

		if (dbExist) {
			// do nothing - database already exist
		} else {
			// By calling this method and empty database will be created into
			// the default system path
			// of your application so we are gonna be able to overwrite that
			// database with our database.
			this.getReadableDatabase();
			try {
				copyDataBase();
			} catch (IOException e) {
				throw new Error("Error copying database");
			}
		}
	}

	// Check if the database exist to avoid re-copy the data
	private boolean checkDataBase() {
		SQLiteDatabase checkDB = null;
		try {
			String path = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(path, null,
					SQLiteDatabase.OPEN_READWRITE);
		} catch (SQLiteException e) {
			// database don't exist yet.
			e.printStackTrace();
		}
		if (checkDB != null) {
			checkDB.close();
		}
		return checkDB != null ? true : false;
	}

	// copy your assets db
	private void copyDataBase() throws IOException {
		// Open your local db as the input stream
		InputStream myInput = context.getAssets().open(DB_NAME);

		String outFileName = DB_PATH + DB_NAME;

		// Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		// transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}

		myOutput.flush();
		myOutput.close();
		myInput.close();
	}

	// Open the database
	public boolean open() {
		try {
			String myPath = DB_PATH + DB_NAME;
			db = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READWRITE);
			return true;
		} catch (SQLException sqle) {
			db = null;
			return false;
		}
	}

	@Override
	public synchronized void close() {
		if (db != null)
			db.close();
		super.close();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}

