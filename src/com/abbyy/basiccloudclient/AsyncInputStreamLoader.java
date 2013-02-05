package com.abbyy.basiccloudclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

public class AsyncInputStreamLoader extends AsyncTaskLoader<InputStream> {
	public static final String ARGUMENT_FILE_PATH = "filePath";

	private URL mUrl;
	private Bundle mArgs;
	private String mAppId;
	private String mPassword;
	private Uri mFilePath;

	private static String test = "<response><task id=\"c3187247-7e81-4d12-8767-bc886c1ab878\" ";
	private static String test2 = "registrationTime=\"2012-02-16T06:42:09Z\" statusChangeTime=\"2012-02-16T06:42:09Z\"  status=\"Queued\" filesCount=\"1\"  credits=\"0\" estimatedProcessingTime=\"1\" description=\"Image.JPG to .pdf\" />    </response>";

	public AsyncInputStreamLoader(Context context, Bundle args) {
		super(context);
		if (args != null) {
			mArgs = args;
			try {
				mUrl = new URL("http://ocrsdk.com/processImage?exportFormat=pdf");
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			mFilePath = (Uri) args.getParcelable(ARGUMENT_FILE_PATH);
		}
	}

	@Override
	public InputStream loadInBackground() {
		// return makeConnection();
		String result = test + "filePath=\"" + mFilePath.toString() + "\"" + test2;
		return new ByteArrayInputStream(result.getBytes());
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

	private InputStream makeConnection() {
		mAppId = "";
		mPassword = "";

		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
		HttpConnectionParams.setSoTimeout(httpParams, 15000);

		DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
		CredentialsProvider credentials = httpClient.getCredentialsProvider();
		credentials.setCredentials(AuthScope.ANY,
				new UsernamePasswordCredentials(mAppId, mPassword));
		httpClient.setCredentialsProvider(credentials);

		HttpPost request = new HttpPost(mUrl.toExternalForm());
		// TODO insert entity with file
		HttpResponse response = null;
		try {
			 response = httpClient.execute(request);
			return response.getEntity().getContent();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
