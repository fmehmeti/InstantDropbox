package com.fitim.instantdropbox;

import java.io.File;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;

import android.app.Activity;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;

public class InstantActivity extends Activity {
	
	private static final String APP_KEY="";
	private static final String APP_SECRET="";
	
	private DropboxAPI<AndroidAuthSession> mApi;	
	private String mErrorMsg;	
	
	
	private PhotoObserver photoObserver = new PhotoObserver();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_instant);
		
		AndroidAuthSession session = buildSession();
		mApi = new DropboxAPI<AndroidAuthSession>(session);
		
		//start authentication
		mApi.getSession().startOAuth2Authentication(InstantActivity.this);
		
		//register content resolver
		this.getApplicationContext().getContentResolver()
									  .registerContentObserver(
											  MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
											  false, 
											  photoObserver);
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		AndroidAuthSession session = mApi.getSession();
		if(session.authenticationSuccessful()){
			try {
				mApi.getSession().finishAuthentication();
				Log.i("AuthLog","successss");
			} catch (IllegalStateException e) {
				Log.i("AuthLog","error auth",e);
			}
		}
		
	}
	
	private AndroidAuthSession buildSession() {
		AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
		AndroidAuthSession session = new AndroidAuthSession(appKeys);
		return session;
	}
	
	private class PhotoObserver extends ContentObserver{
		
		public PhotoObserver() {
			super(null);
			
		}
		
		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			File pic = readPicture(getApplicationContext(),
							  MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		
			UploadPicture upload = new UploadPicture(getApplicationContext(),mApi,pic);
			upload.execute();
		}

		private File readPicture(Context context,Uri uri) {
			Cursor cursor = context.getContentResolver()
							.query(uri, null, null, null, "date_added DESC");
			File file = null;
			if(cursor.moveToNext()){
				int dataColumn = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
				String filePath = cursor.getString(dataColumn);
				
				file = new File(filePath);
			}
			
			return file;			
			
		}	
		
		
	}	

}
