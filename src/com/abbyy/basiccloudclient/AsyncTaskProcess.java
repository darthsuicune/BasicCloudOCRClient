package com.abbyy.basiccloudclient;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

public class AsyncTaskProcess extends AsyncTaskLoader<String> {
	public static final String ARGUMENT_TASK_ID = "taskId";
	public static final String ARGUMENT_PROCESSING_TIME = "processingTime";
	public static final String ARGUMENT_STATUS = "Status";
	
	private String mTaskId;
	private String mStatus;
	private int mProcessingTime;

	public AsyncTaskProcess(Context context, Bundle args) {
		super(context);
		mTaskId = args.getString(ARGUMENT_TASK_ID);
	}
	
	@Override
	protected void onStartLoading() {
		forceLoad();
	}

	@Override
	public String loadInBackground() {
		if(!mStatus.equals("Completed")){
			try {
				Thread.sleep(mProcessingTime*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "";
		} else{
			return "www.google.es";
		}
	}

}
