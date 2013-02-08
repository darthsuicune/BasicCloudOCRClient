package com.abbyy.basiccloudocrclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Xml;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.abbyy.basiccloudocrclient.compat.ActionBarActivity;

public class MainActivity extends ActionBarActivity {
	private static final int LOADER_CONNECTION = 0;
	private static final int LOADER_PROCESS = 1;
	private Uri mUri;
	private String mTaskId;
	TextView mProgressTextView;
	Button uploadView;
	DateFormat mDateFormat = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ss'Z'",
			Locale.GERMANY);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		mProgressTextView = (TextView) findViewById(R.id.progress_text);
		uploadView = (Button) findViewById(R.id.button_upload);
		uploadView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pickImage();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private void pickImage() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent resultIntent) {
		switch (requestCode) {
		case 0:
			if (resultCode == RESULT_OK) {
				mUri = resultIntent.getData();
				launchTask();
			}
			break;
		default:
			break;
		}
	}

	private void parseResponse(String response) {
		if(response == null || response.equals("")){
			return;
		}
		
		XmlPullParser parser = Xml.newPullParser();
		try {
			parser.setInput(new ByteArrayInputStream(response.getBytes()), null);
			readData(parser);
		} catch (XmlPullParserException e) {
			launchTask();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readData(XmlPullParser parser) throws XmlPullParserException,
			IOException {
		parser.nextTag();
		parser.require(XmlPullParser.START_TAG, null, "response");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if (name.equals(getString(R.string.tag_task))) {
				mTaskId = parser.getAttributeValue(null,
						getString(R.string.field_id));
				String status = parser.getAttributeValue(null,
						getString(R.string.field_status));
				
				Bundle args = new Bundle();
				args.putString(AsyncTaskProcess.ARGUMENT_STATUS, status);

				if (!status.equals(getString(R.string.status_completed))) {
					int estimatedProcessingTime = Integer
							.parseInt(parser
									.getAttributeValue(
											null,
											getString(R.string.field_estimated_processing_time)));
					mProgressTextView
							.append("Task " + mTaskId + " started." + "\n");
					Date registrationTime = getDate(parser.getAttributeValue(null,
							getString(R.string.field_registration_time)));
					mProgressTextView.append("Registered: " + registrationTime + "\n");
					mProgressTextView.append("Actual status: " + status + "\n");
					mProgressTextView.append("Estimated time: "
							+ estimatedProcessingTime + "\n");
					
					args.putString(AsyncTaskProcess.ARGUMENT_TASK_ID, mTaskId);
					args.putInt(AsyncTaskProcess.ARGUMENT_PROCESSING_TIME,
							estimatedProcessingTime);
					getSupportLoaderManager().restartLoader(LOADER_PROCESS, args,
							new AsyncTaskHelper());
				} else {
					final String resultURL = parser.getAttributeValue(null, getString(R.string.field_result_url));
					mProgressTextView.append("FINISHED!\n");
					Button download = (Button) findViewById(R.id.button_download);
					download.setVisibility(View.VISIBLE);
					uploadView.setVisibility(View.GONE);
					download.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							launchDownload(resultURL);
						}
					});
				}
				// Date statusChangeTime =
				// getDate(parser.getAttributeValue(null,
				// getString(R.string.field_status_change_time)));
				 int fileCount =
				 Integer.parseInt(parser.getAttributeValue(null,
				 getString(R.string.field_files_count)));
				// int credits = Integer.parseInt(parser.getAttributeValue(null,
				// getString(R.string.field_credits)));
				//
				// String description = parser.getAttributeValue(null,
				// getString(R.string.field_description));
				// String error = parser.getAttributeValue(null,
				// getString(R.string.field_error));

			}
		}
	}

	private Date getDate(String dateAsString) {
		Date date = null;
		try {
			date = mDateFormat.parse(dateAsString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	private String getFilePath() {
		String[] projection = { MediaStore.Images.Media.DATA };
		CursorLoader loader = new CursorLoader(this, mUri, projection, null,
				null, null);
		Cursor cursor = loader.loadInBackground();
		cursor.moveToFirst();
		String result = cursor.getString(cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
		return result;
	}

	private void launchTask() {
		String filePath = getFilePath();

		Bundle args = new Bundle();
		args.putString(AsyncInputStreamLoader.ARGUMENT_FILE_PATH, filePath);
		if (mTaskId != null) {
			args.putString(AsyncInputStreamLoader.ARGUMENT_TASK_ID, mTaskId);
		}
		getSupportLoaderManager()
				.restartLoader(LOADER_CONNECTION, args, new AsyncTaskHelper());
	}

	private void launchDownload(String url) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(intent);
	}

	private class AsyncTaskHelper implements LoaderCallbacks<String> {

		@Override
		public Loader<String> onCreateLoader(int id, Bundle args) {
			Loader<String> loader = null;
			switch(id){
			case LOADER_CONNECTION:
				loader = new AsyncInputStreamLoader(MainActivity.this, args);
				break;
			case LOADER_PROCESS:
				loader = new AsyncTaskProcess(MainActivity.this, args);
				break;
			}
			return loader;
		}

		@Override
		public void onLoadFinished(Loader<String> loader,
				String response) {
			switch(loader.getId()){
			case LOADER_CONNECTION:
				parseResponse(response);
				break;
			case LOADER_PROCESS:
				if (response.equals("")) {
					launchTask();
				}
				break;
			}
		}

		@Override
		public void onLoaderReset(Loader<String> loader) {
		}
	}
}
