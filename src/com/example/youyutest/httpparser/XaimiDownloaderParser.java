package com.example.youyutest.httpparser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

public class XaimiDownloaderParser {
	
	
	private Handler handler;
	Message msg;
	private String xiamiId;

	public XaimiDownloaderParser(Handler handler) {
		this.handler = handler;
	}
	

	public void downloadXiami() throws Exception {

		List<String> xiamiSongList = XiamiDownloader.getSongListById(getXiamiId());
		downloadSongList(xiamiSongList);
	}

	private void downloadSongList(List<String> songList) throws Exception {
		//create file dir
		File SDCardRoot = Environment.getExternalStorageDirectory();
		File file = new File(SDCardRoot, "/music");
		file.mkdirs();
		for(String song:songList){
			try {
				downloadFromInternet(song);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		sendMsg(3,0,0,"end");

	}

	private boolean downloadFromInternet(String songTitle) {
		String internetDownloadUrl = "";
		try {
			internetDownloadUrl = BaiduMp3Downloader.downloadMap3Baidu(songTitle);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(StringUtils.isBlank(internetDownloadUrl)){
			return false;
		}
		try {
			saveSong(songTitle, internetDownloadUrl);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private void saveSong(String songTitle, String songUrl)
			throws IOException, ClientProtocolException, FileNotFoundException {
		HttpGet get = new HttpGet(songUrl);
		HttpClient client = new DefaultHttpClient();
		HttpResponse response = client.execute(get);
		File SDCardRoot = Environment.getExternalStorageDirectory();
		File file = new File(SDCardRoot, "/music/"+songTitle+".mp3");
		if(!file.exists()){
			file.createNewFile();
		}
		FileOutputStream fileOutput = new FileOutputStream(file);
		InputStream inputStream = response.getEntity().getContent();
		
		long totalSize = response.getEntity().getContentLength();
		long downloadedSize = 0;
		//send message
		sendMsg(0,totalSize,downloadedSize,songTitle);
		byte[] buffer = new byte[1024];
		int bufferLength = 0; // used to store a temporary size of the buffer
		while ((bufferLength = inputStream.read(buffer)) > 0) {
			fileOutput.write(buffer, 0, bufferLength);
			downloadedSize += bufferLength;
			sendMsg(1,totalSize,downloadedSize,songTitle);
		}
		fileOutput.close();
		sendMsg(2,totalSize,downloadedSize,songTitle);
	}

	private void sendMsg(int flag, long totalSize,long currentSize,String songTitle) {
		msg = new Message();
		msg.what = flag;
		Bundle bundleData = new Bundle();
		switch (flag) {
		case 0:
			bundleData.putString("songTitle", songTitle);
			bundleData.putLong("totalSize", totalSize);
			break;
		case 1:
			bundleData.putLong("currentSize", currentSize);
			break;
		case 2:
			bundleData.putString("songTitle", songTitle);
			break;
		case 3:
			break;
		default:
			break;
		}
		msg.setData(bundleData);
		msg.setData(bundleData);
		handler.sendMessage(msg);
	}


	public String getXiamiId() {
		return xiamiId;
	}


	public void setXiamiId(String xiamiId) {
		this.xiamiId = xiamiId;
	}
}
