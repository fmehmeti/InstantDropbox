package com.fitim.instantdropbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.UploadRequest;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxFileSizeException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

import android.content.Context;
import android.os.AsyncTask;

public class UploadPicture extends AsyncTask<Void, Long, Boolean>{
private DropboxAPI<AndroidAuthSession> mApi;
	
	Context mContext;
	
	private String mErrorMsg;
	private UploadRequest mRequest;
	private File mFile;
	
	public UploadPicture(Context context,DropboxAPI<AndroidAuthSession>api,File file){
		mContext=context;
		mApi=api;
		mFile=file;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		String mPath = "/Photos/";
		
		 try {
	            FileInputStream fis = new FileInputStream(mFile);
	            String path = mPath + mFile.getName();
	            DropboxAPI.Entry e = mApi.putFile(path, fis, mFile.length(),null,null);

	            if (mRequest != null) {
	                mRequest.upload();
	                return true;
	            }

	        } catch (DropboxUnlinkedException e) {
	            // This session wasn't authenticated properly or user unlinked
	            mErrorMsg = "This app wasn't authenticated properly.";
	        } catch (DropboxFileSizeException e) {
	            // File size too big to upload via the API
	            mErrorMsg = "This file is too big to upload";
	        } catch (DropboxPartialFileException e) {
	            // We canceled the operation
	            mErrorMsg = "Upload canceled";
	        } catch (DropboxServerException e) {
	            // Server-side exception
	            if (e.error == DropboxServerException._401_UNAUTHORIZED) {
	                // unauthorized, automatically log the user out !!!
	            } else if (e.error == DropboxServerException._403_FORBIDDEN) {
	                // not allowed to access this
	            } else if (e.error == DropboxServerException._404_NOT_FOUND) {
	                // path not found 
	            } else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
	                // user is over quota
	            } else {
	                // Something else
	            }
	            // This gets the Dropbox error, translated into the user's language
	            mErrorMsg = e.body.userError;
	            if (mErrorMsg == null) {
	                mErrorMsg = e.body.error;
	            }
	        } catch (DropboxIOException e) {
	            // Happens all the time, probably want to retry automatically.
	            mErrorMsg = "Network error.  Try again.";
	        } catch (DropboxParseException e) {
	            // Probably due to Dropbox server restarting, should retry
	            mErrorMsg = "Dropbox error.  Try again.";
	        } catch (DropboxException e) {
	            // Unknown error
	            mErrorMsg = "Unknown error.  Try again.";
	        } catch (FileNotFoundException e) {
	        }
		return false;
	}

}


