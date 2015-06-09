package com.schmidtdesigns.shiftez.network;

import com.octo.android.robospice.retrofit.RetrofitGsonSpiceService;

/**
 * Created by Braden on 15-06-08
 */
public class RetrofitSpiceService extends RetrofitGsonSpiceService {

	private final static String BASE_URL = "http://shift-ez.appspot.com";

	@Override
	public void onCreate() {
		super.onCreate();
		addRetrofitInterface(Api.class);
	}

	@Override
	protected String getServerUrl() {
		return BASE_URL;
	}
}
