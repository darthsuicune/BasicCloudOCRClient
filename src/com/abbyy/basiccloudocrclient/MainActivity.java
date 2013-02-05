package com.abbyy.basiccloudocrclient;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Xml;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.abbyy.basiccloudclient.R;
import com.abbyy.basiccloudocrclient.compat.ActionBarActivity;

public class MainActivity extends ActionBarActivity {
	private Uri mUri;
	TextView mProgressTextView;
	DateFormat mDateFormat = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ss'Z'",
			Locale.GERMANY);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mProgressTextView = (TextView) findViewById(R.id.progress_text);
		Button upload = (Button) findViewById(R.id.button_upload);
		upload.setOnClickListener(new OnClickListener() {
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
			mProgressTextView.setText("");
			mUri = resultIntent.getData();
			Bundle args = new Bundle();
			args.putParcelable(AsyncInputStreamLoader.ARGUMENT_FILE_PATH, mUri);
			getSupportLoaderManager().initLoader(0, args,
					new ConnectionHelper());
			break;
		default:
			break;
		}
	}

	private void parseResponse(InputStream stream) {
		XmlPullParser parser = Xml.newPullParser();
		try {
			parser.setInput(stream, null);
			readData(parser);
		} catch (XmlPullParserException e) {
			e.printStackTrace();
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
			if (name.equals("task")) {
				String taskId = parser.getAttributeValue(null, "id");
				String status = parser.getAttributeValue(null, "status");
				Date registrationTime = getDate(parser.getAttributeValue(null,
						"registrationTime"));
				Date statusChangeTime = getDate(parser.getAttributeValue(null,
						"statusChangeTime"));
				int estimatedProcessingTime = Integer.parseInt(parser
						.getAttributeValue(null, "estimatedProcessingTime"));
				// parser.getAttributeValue(null,
				// getActivity().getString(R.string.field_status_change_time)),
				// Integer.parseInt(parser.getAttributeValue(null,
				// getActivity().getString(R.string.field_files_count))),
				// Integer.parseInt(parser.getAttributeValue(null,
				// getActivity().getString(R.string.field_credits))),
				// Integer.parseInt(parser.getAttributeValue(null,
				// getActivity().getString(R.string.field_estimated_processing_time))),
				// parser.getAttributeValue(null,
				// getActivity().getString(R.string.field_description)),
				// parser.getAttributeValue(null,
				// getActivity().getString(R.string.field_result_url)),
				// parser.getAttributeValue(null,
				// getActivity().getString(R.string.field_error)),

				mProgressTextView.append("Task " + taskId + " started." + "\n");
				mProgressTextView.append("Actual status: " + status + "\n");
				mProgressTextView.append("Estimated time: "
						+ estimatedProcessingTime + "\n");
				Bundle args = new Bundle();
				args.putString(AsyncTaskProcess.ARGUMENT_TASK_ID, taskId);
				args.putString(AsyncTaskProcess.ARGUMENT_STATUS, status);
				args.putInt(AsyncTaskProcess.ARGUMENT_PROCESSING_TIME, estimatedProcessingTime);
				getSupportLoaderManager().initLoader(1, args, new TaskProcessHelper());
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

	private class ConnectionHelper implements LoaderCallbacks<InputStream> {

		@Override
		public Loader<InputStream> onCreateLoader(int id, Bundle args) {
			return new AsyncInputStreamLoader(MainActivity.this, args);
		}

		@Override
		public void onLoadFinished(Loader<InputStream> loader,
				InputStream inputStream) {
			parseResponse(inputStream);
		}

		@Override
		public void onLoaderReset(Loader<InputStream> loader) {
		}
	}
	
	private class TaskProcessHelper implements LoaderCallbacks<String>{

		@Override
		public Loader<String> onCreateLoader(int id, Bundle args) {
			return new AsyncTaskProcess(MainActivity.this, args);
		}

		@Override
		public void onLoadFinished(Loader<String> loader, final String response) {
			if(response.equals("")){
				
			}else{
				mProgressTextView.append("FINISHED!\n");
				Button download = (Button) findViewById(R.id.button_download);
				download.setVisibility(View.VISIBLE);
				download.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(response));
						startActivity(intent);
					}
				});
			}
		}

		@Override
		public void onLoaderReset(Loader<String> loader) {
		}
	}
}
