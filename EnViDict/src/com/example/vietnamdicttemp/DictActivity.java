package com.example.vietnamdicttemp;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

//import android.support.design.widget.FloatingActionButton;

public class DictActivity extends AppCompatActivity implements
		SearchView.OnQueryTextListener {
	EditText text;
	Button add;
	String tag = "Event";
	SearchView searchView;
	private ShareActionProvider shareActionProvider;
	EnglishVietDB engvietDB;
	Cursor cursor;
	ImageView favoriteImg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setIcon(R.drawable.ic_launcher);
		
		engvietDB = EnglishVietDB.getInstance(this);
		try {
			engvietDB.create();
			engvietDB.open();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		favoriteImg = (ImageView) findViewById(R.id.favor);
		favoriteImg.setOnClickListener(new View.OnClickListener() {
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				TextView wordId = (TextView)findViewById(R.id.idWord);
				new UpdateFavoriteWord().execute(wordId.getText().toString());
			}
		});
		Log.d(tag, "onCreate()");
		showResults("hello");
	}

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			showResults(query);
			Log.d(tag, "in handle Intent");

			// managedQuery(enterSearch, null, null, null, null);
			// onSearchRequested();
		} else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			Log.d(tag, "in action View");
			Uri data = intent.getData();
			Log.d("tag", data.toString());
			showResultData(data);
		}
	}

	private void showResultData(Uri data) {
		cursor = getContentResolver().query(data, null, null, null, null);
		// Cursor cursor = mDbHelper.fetchRecordByQuery(query);
		if (cursor != null && cursor.moveToFirst()) {
			String id = String.valueOf(cursor.getInt(0));
			int favorite = cursor.getInt(2);
			Log.d(tag, "id_" + String.valueOf(cursor.getInt(0)));
			Log.d(tag, "favorite" + String.valueOf(cursor.getInt(2)));
			makeLayoutWord(cursor.getString(1), id, favorite);
			Log.d(tag, cursor.getColumnName(1));
			Log.d(tag, cursor.getString(1));

		} else {
			// delete content in listView
			// listResult.setAdapter(null);
			// no_result.setVisibility(TextView.VISIBLE);
			String noresult = "@Không tìm thấy dữ liệu.";
			makeLayoutWord(noresult, "-1", -1);
			TextView word = (TextView) findViewById(R.id.Word);
			word.setText("");
			word.setVisibility(TextView.INVISIBLE);
		}
	}

	private void makeLayoutWord(String meanWord, String id, int favor) {
		TextView word = (TextView) findViewById(R.id.Word);
		word.setText("");
		word.setVisibility(TextView.VISIBLE);
		ScrollView scr = (ScrollView) findViewById(R.id.scrollView);
		scr.setLayoutParams(new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));
		scr.setPadding(16, 5, 16, 5);
		if (scr.getChildCount() > 0) {
			scr.removeAllViews();
		}

		TextView idText = (TextView) findViewById(R.id.idWord);
		idText.setText(id);
		if (favoriteImg != null) {
			favoriteImg.setVisibility(ImageView.VISIBLE);
			if (favor > 0) {
				favoriteImg.setImageResource(R.drawable.hearted_);
				favoriteImg.setTag(R.drawable.hearted_);
			} else if (favor == 0) {
				favoriteImg.setImageResource(R.drawable.heart_);
				favoriteImg.setTag(R.drawable.heart_);
			} else {
				// favoriteImg.setImageResource();
				favoriteImg.setVisibility(ImageView.INVISIBLE);
			}
		}
		LinearLayout rootOfWord = null;
		rootOfWord = new LinearLayout(this);
		rootOfWord.setOrientation(LinearLayout.VERTICAL);
		rootOfWord.setPadding(5, 1, 6, 6);
		rootOfWord.setVerticalScrollBarEnabled(true);
		String[] line = meanWord.split("\n");
		Log.d("DictActiviti", "linesize : " + line.length);
		for (int i = 0; i < line.length; i++) {
			TextView tv = new TextView(this);
			if (line[i].startsWith("@")) {
				line[i] = line[i].replace("@", " ");
				if (line[i].contains(" /")) {
					word.setText(line[i].split(" /")[0]);
				} else if (line[i].contains(" \\[")) {
					word.setText(line[i].split(" \\[")[0]);
				} else {
					word.setText(line[i]);
				}
				tv.setTextColor(Color.BLACK);
			} else if (line[i].startsWith("*")) {
				tv.setTextColor(Color.RED);
			} else if (line[i].startsWith("=")) {
				tv.setTextColor(Color.DKGRAY);
				line[i] = line[i].replace("=", " ");
				line[i] = line[i].replace("+", " : ");
			} else if (line[i].startsWith("-")) {
				tv.setTextColor(Color.BLUE);
			}
			tv.setText(line[i]);
			tv.setTextSize(20);
			rootOfWord.addView(tv);
		}
		scr.addView(rootOfWord);

	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		setIntent(intent);
		Log.d(tag, "onNewIntent()");
		handleIntent(intent);
//		searchView.clearFocus();
	}

	public void searchDisplay(View v) {
		onSearchRequested();
	}

	public void showResults(String query) {
		Uri enterSearch = EnglishVietContentProvider.CONTENT_URI;
		cursor = getContentResolver().query(enterSearch, null, null,
				new String[] { query }, null);
		if (cursor != null && cursor.moveToFirst()) {
			int favor = cursor.getInt(2);
			makeLayoutWord(cursor.getString(1),
					String.valueOf(cursor.getInt(0)), favor);
		} else {
			// // delete content in listView
			String noresult = "@Không tìm thấy từ : " + query;
			makeLayoutWord(noresult, "-1", -1);
			TextView word = (TextView) findViewById(R.id.Word);
			word.setText("");
			word.setVisibility(TextView.INVISIBLE);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(tag, "onResume()");
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub

		super.onPause();
		if(cursor!=null){
			cursor.close();
		}
		Log.d(tag, "onPause()");
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (engvietDB != null) {
			engvietDB.close();
		}

		Log.d(tag, "onDestroy()");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_search, menu);

		MenuItem SearchItem = (MenuItem) menu.findItem(R.id.menu_search_item);
		searchView = (SearchView) MenuItemCompat.getActionView(SearchItem);
		searchView.setOnQueryTextListener(this);

		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		searchView
				.setSearchableInfo(searchManager
						.getSearchableInfo(new ComponentName(this,
								DictActivity.class)));
		searchView.setIconifiedByDefault(false);

		MenuItem share_item = (MenuItem) menu.findItem(R.id.menu_share_item);
		shareActionProvider = (ShareActionProvider) MenuItemCompat
				.getActionProvider(share_item);
		String playStoreLink = "https://play.google.com/store/apps/details?id="
				+ getPackageName();
		String yourShareText = "Install this app " + playStoreLink;
		setIntentShare(yourShareText);

		return true;
	}

	private void setIntentShare(String text) {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_TEXT, text);
		shareActionProvider.setShareIntent(i);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		/*
		 * if (id == R.id.action_settings) { return true; }else if (id ==
		 * R.id.search_record) { // onSearchRequested(); return true; }
		 */
		switch (id) {
		case R.id.menu_share_item:

			return true;
		case R.id.menu_rate:
			Uri uri = Uri.parse("market://details?id="
					+ getBaseContext().getPackageName());
			Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
			// To count with Play market backstack, After pressing back button,
			// to taken back to our application, we need to add following flags
			// to intent.
			goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
			// Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
			// Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
					Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
			try {
				startActivity(goToMarket);
			} catch (ActivityNotFoundException e) {
				startActivity(new Intent(
						Intent.ACTION_VIEW,
						Uri.parse("http://play.google.com/store/apps/details?id="
								+ getBaseContext().getPackageName())));
			}
			return true;
		case R.id.menu_favorite:
			Log.d(this.getClass().getName(), "menu_favorite");
			Intent intentFav = new Intent(getBaseContext(), Favorite.class);
			startActivity(intentFav);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		// return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onQueryTextChange(String arg0) {
		// showResults(arg0);
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String arg0) {
		// TODO Auto-generated method stub
		return false;

	}

	private class UpdateFavoriteWord extends AsyncTask<String, Void, Integer> {
		ContentValues wordContent;
		int favorite = 0;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			wordContent = new ContentValues();
			String curImageView = String.valueOf(favoriteImg.getTag());
			Log.d(tag, "favoriteed img was click :" + curImageView + ":"
					+ String.valueOf(R.drawable.heart_));
			if (curImageView
					.equalsIgnoreCase(String.valueOf(R.drawable.heart_))) {
				// not favorite -> need change to favorite
				favorite = 1;
			} else {
				favorite = 0;
			}
			wordContent.put("favorite", favorite);

		}

		@Override
		protected Integer doInBackground(String... params) {
			// TODO Auto-generated method stub
			EnglishVietDB evvdb = EnglishVietDB.getInstance(getBaseContext());
			try {
				SQLiteDatabase db = evvdb.getReadableDatabase();
				db.update(EnglishVietDB.TABLE_PHRASE, wordContent, "_id=?",
						new String[] { params[0] });
				db.close();
				return favorite;
			} catch (Exception e) {
				// TODO: handle exception
				return -1;
			}
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			switch (result) {
			case 1://update succuess and now word was favorited, set image favorite to favorited
				favoriteImg.setImageResource(R.drawable.hearted_);
				favoriteImg.setTag(R.drawable.hearted_);
				Toast.makeText(getBaseContext(), "is favorited",Toast.LENGTH_SHORT).show();
				Log.d(tag, "update ok, is favorited");
				break;
			case 0://update succuess and now word was not favorited, set imagefavorite to not favorite
				favoriteImg.setImageResource(R.drawable.heart_);
				favoriteImg.setTag(R.drawable.heart_);
				Log.d(tag, "update ok, is not favorited");
				Toast.makeText(getBaseContext(), "is not favorited",Toast.LENGTH_SHORT).show();
				break;
			case -1://not update success, so alert
				Toast.makeText(getBaseContext(), "Database error! Changes favorited word fail",Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}

	}

}
