package com.example.vietnamdicttemp;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class WordActivity extends AppCompatActivity implements LoaderCallbacks<Cursor>{
	Uri wUri;
	private ImageView favoriteImg;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.d(this.getClass().getName(), "onCreate()");
		setContentView(R.layout.main);
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
		
		wUri = getIntent().getData();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		//get data intent. display in view
		// Invokes the method onCreateloader() in non-ui thread
		getSupportLoaderManager().initLoader(0, null, this);
//		onSearchRequested();
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// TODO Auto-generated method stub
		Log.d(this.getClass().getName(), "onCreateLoader");
		return new CursorLoader(getBaseContext(), wUri, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		// TODO Auto-generated method stub
		Log.d(this.getClass().getName(), "onLoadFinished");
		if (cursor != null && cursor.moveToFirst()) {
			String id = String.valueOf(cursor.getInt(0));
			int favorite = cursor.getInt(2);
			String tag = this.getClass().getName();
			Log.d(tag , "id_" + String.valueOf(cursor.getInt(0)));
			Log.d(tag, "favorite" + String.valueOf(cursor.getInt(2)));
			makeLayoutWord(cursor.getString(1), id, favorite);
			Log.d(tag, cursor.getColumnName(1));
			Log.d(tag, cursor.getString(1));

		} else {
			String noresult = "@Không tìm thấy dữ liệu.";
			makeLayoutWord(noresult, "-1", -1);
			TextView word = (TextView) findViewById(R.id.Word);
			word.setText("");
			word.setVisibility(TextView.INVISIBLE);
		}
	}
	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.d(this.getClass().getName(), "onStop");
	}


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.d(this.getClass().getName(), "onPause");
	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.d(this.getClass().getName(), "onResume");
		super.onResume();
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
			String tag = this.getClass().getName();
			Log.d(tag , "favoriteed img was click :" + curImageView + ":"
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
			String tag = this.getClass().getName();
			switch (result) {
			case 1://update succuess and now word was favorited, set image favorite to favorited
				favoriteImg.setImageResource(R.drawable.hearted_);
				favoriteImg.setTag(R.drawable.hearted_);
				Toast.makeText(getBaseContext(), "is favorited",Toast.LENGTH_SHORT).show();
				Log.d(tag , "update ok, is favorited");
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
