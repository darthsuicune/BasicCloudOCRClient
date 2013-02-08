package com.abbyy.basiccloudocrclient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

public class AsyncInputStreamLoader extends AsyncTaskLoader<String> {
	public static final String ARGUMENT_FILE_PATH = "filePath";
	public static final String ARGUMENT_TASK_ID = "taskId";

	private static final String BASE_URL = "http://cloud.ocrsdk.com/";

	private URL mUrl;
	private String mFilePath;
	private String mTaskId;

	public AsyncInputStreamLoader(Context context, Bundle args) {
		super(context);
		if (args != null) {
			mFilePath = args.getString(ARGUMENT_FILE_PATH);
			mTaskId = args.getString(ARGUMENT_TASK_ID);
			if (mTaskId == null) {
				try {
					mUrl = new URL(BASE_URL + "processImage?exportFormat=rtf");
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			} else {
				try {
					mUrl = new URL(BASE_URL + "getTaskStatus?taskId=" + mTaskId);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}

			mFilePath = args.getString(ARGUMENT_FILE_PATH);
		}
	}

	@Override
	public String loadInBackground() {
		return makeConnection();
		// String result;
		// if(mTaskId == null) {
		// result = test + "filePath=\"" + mFilePath.toString() + "\""
		// + test2;
		// } else {
		// result = mTaskId;
		// }
		// return new ByteArrayInputStream(result.getBytes());
	}

	/**
	 * The implementation of the AsyncTaskLoader for the support package has a
	 * bug which requires the forceLoad() on the StartLoading, as it will not
	 * start on its own. This allows for further control of the loadings.
	 */
	@Override
	protected void onStartLoading() {
		forceLoad();
	}

	private String makeConnection() {
		String mAppId = "BasicAndroidCloudOCRClient";
		String mPassword = "5QQQ0U/Wx+mnkIT51ZLiHREF";

		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
		HttpConnectionParams.setSoTimeout(httpParams, 15000);

		DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
		CredentialsProvider credentials = httpClient.getCredentialsProvider();
		credentials.setCredentials(AuthScope.ANY,
				new UsernamePasswordCredentials(mAppId, mPassword));
		httpClient.setCredentialsProvider(credentials);

		HttpPost request = new HttpPost(mUrl.toExternalForm());

		InputStreamEntity entity = null;
		HttpResponse response = null;
		try {
			entity = new InputStreamEntity(new FileInputStream(new File(mFilePath)), -1);
			entity.setContentType("application/octet-stream");
			entity.setChunked(true);
			BufferedHttpEntity bufferedEntity = new BufferedHttpEntity(entity);
			request.setEntity(bufferedEntity);

			response = httpClient.execute(request);
			int code = response.getStatusLine().getStatusCode();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			String result = reader.readLine();
			int i = 0;
			return result;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
