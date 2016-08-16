
package com.example.vietnamdicttemp;

import java.util.HashMap;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class EnglishVietContentProvider extends ContentProvider{
	public static final String PROVIDER_NAME = "com.example.provider.dict";
	public static final String URI = "content://" + PROVIDER_NAME+"/records";
	public static final String URI_FA = "content://" + PROVIDER_NAME+"/favorites";
	public static final Uri CONTENT_URI = Uri.parse(URI);
	//content://com.example.provider.dict/search_suggest_query/?limit=50
	//content://android.demosearch.citysuggestion/search_suggest_query/a?limit=50
	public static final String ID_COL = "_id";
	public static final String WORD_COL = "l_from";
	public static final String MEAN_COL = "l_to";

	public static final int WORD_SUGG = 1;
	public static final int WORD = 2;
	public static final int WORD_ID = 3;
	public static final int WORD_FA= 4;
	

	UriMatcher uriMatcher = buildUriMatcher();
	private UriMatcher buildUriMatcher(){
		UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_NAME, SearchManager.SUGGEST_URI_PATH_QUERY, WORD_SUGG);
		uriMatcher.addURI(PROVIDER_NAME, "records", WORD);
		uriMatcher.addURI(PROVIDER_NAME, "records/#", WORD_ID);
		uriMatcher.addURI(PROVIDER_NAME, "favorites", WORD_FA);
		return uriMatcher;
	}

	private SQLiteDatabase database;
	EnglishVietDB evDB;

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		// mDBHelper = new DatabaseHelper(getContext());
		// mDb = mDBHelper.getWritableDatabase();
		evDB = EnglishVietDB.getInstance(getContext());
		if(evDB.open()){
			database = evDB.getDb();
			return (database == null) ? false : true;
		}else 
			return false;
		
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(EnglishVietDB.TABLE_PHRASE);
		Log.d(this.getClass().getName(), "uri: "+ uri.toString());
		Cursor c;
		switch (uriMatcher.match(uri)) {
		case WORD:
			//suggestion
			Log.d(this.getClass().getName(), "match word");
			c = evDB.getTopOfListSugWords(selectionArgs);
			c.setNotificationUri(getContext().getContentResolver(), uri);
			return c;
		case WORD_ID:
			//select one in list word suggestion
			Log.d(this.getClass().getName(), "match wordid");
			String id = uri.getLastPathSegment();
			c = evDB.getWordById(id);
			c.setNotificationUri(getContext().getContentResolver(), uri);
			return c;
		case WORD_SUGG:
			Log.d(this.getClass().getName(), "match suggestion");
			c = evDB.getSuggestionWords(selectionArgs,20);
			c.setNotificationUri(getContext().getContentResolver(), uri);
			return c;
		case WORD_FA:
			//favorite list word
			Log.d(this.getClass().getName(), "favorite");
			c = evDB.getFavorites(selectionArgs,1000);
			return c;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
	}

	@Override
	public String getType(Uri uri) {
		Log.d(this.getClass().getName(), "getType(Uri)");
		// TODO Auto-generated method stub
		switch (uriMatcher.match(uri)) {
		case WORD:
			return "vnd.android.cursor.dir/vnd.example.records";
		case WORD_ID:
			return "vnd.android.cursor.item/vnd.example.records";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		long rowID = database.insert(EnglishVietDB.TABLE_PHRASE, "", values);

		/**
		 * If record is added successfully
		 */

		if (rowID > 0) {
			Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
			getContext().getContentResolver().notifyChange(_uri, null);
			return _uri;
		}
		throw new SQLException("Failed to add a record into " + uri);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		int count = 0;

		switch (uriMatcher.match(uri)) {
		case WORD:
			count = database.delete(EnglishVietDB.TABLE_PHRASE, selection, selectionArgs);
			break;

		case WORD_ID:
			String id = uri.getPathSegments().get(1);
			count = database.delete(EnglishVietDB.TABLE_PHRASE,
					ID_COL
							+ " = "
							+ id
							+ (!TextUtils.isEmpty(selection) ? " AND ("
									+ selection + ')' : ""), selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		int count = 0;

		switch (uriMatcher.match(uri)) {
		case WORD:
			count = database.update(EnglishVietDB.TABLE_PHRASE, values, selection, selectionArgs);
			break;

		case WORD_ID:
			count = database.update(
					EnglishVietDB.TABLE_PHRASE,
					values,
					ID_COL
							+ " = "
							+ uri.getPathSegments().get(1)
							+ (!TextUtils.isEmpty(selection) ? " AND ("
									+ selection + ')' : ""), selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}


}
