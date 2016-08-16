package com.example.vietnamdicttemp;

import com.example.vietnamdicttemp.R;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class Favorite extends AppCompatActivity implements LoaderCallbacks<Cursor> {

	ListView listFavorite;
	SimpleCursorAdapter simpleCurAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.d(this.getClass().getName(), "onCreate()");
		setContentView(R.layout.favorite);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		listFavorite = (ListView)findViewById(R.id.listFavorite);
		listFavorite.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Intent wordIntent = new Intent(getBaseContext(), WordActivity.class);
				Uri uridata = Uri.parse(EnglishVietContentProvider.URI);
				uridata = Uri.withAppendedPath(uridata, String.valueOf(id));
				wordIntent.setData(uridata);
				startActivity(wordIntent);
			}
		});;
		simpleCurAdapter = new SimpleCursorAdapter(getBaseContext(),
//				android.R.layout.simple_list_item_1,
				R.layout.record,
	            null,
	            new String[] { SearchManager.SUGGEST_COLUMN_TEXT_1},
	            new int[] { R.id.text1}, 0);
		
		listFavorite.setAdapter(simpleCurAdapter);
		getSupportLoaderManager().initLoader(1, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// TODO Auto-generated method stub
		Log.d(this.getClass().getName(), "onCreateLoader()");
		Uri uri = Uri.parse(EnglishVietContentProvider.URI_FA);
		return new CursorLoader(getBaseContext(), uri, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		// TODO Auto-generated method stub
		Log.d(this.getClass().getName(), "onLoadFinished()");
		if(arg1!=null){
			Log.d(this.getClass().getName(), "onLoadFinished : "+ arg1.getCount());
		}
		simpleCurAdapter.swapCursor(arg1);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.d(this.getClass().getName(), "onStop()");
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.d(this.getClass().getName(), "onPause()");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.d(this.getClass().getName(), "onResume()");
		super.onResume();
	}

}
