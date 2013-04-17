package com.example.youyutest.httpparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.example.youyutest.domain.UserLoginInfo;

public class DoubanParser {
	
	private UserLoginInfo userLoginInfo;
	private int downloadCount;
	private Handler handler;
	Message msg;

	private static final String doubanLoginUrl = "http://www.douban.com/j/app/login";
	private static final MessageFormat doubanFavoriteSongUrlFormat = new MessageFormat(
			"http://www.douban.com/j/app/radio/liked_songs?"
					+ "exclude=675558|12384|642358|546734|10761|761079|1394944|546727|676245|431315&"
					+ "version=608&client=s:mobile|y:android+4.1.1|f:608|m:Google|d:-1178839463|e:google_galaxy_nexus&"
					+ "app_name=radio_android&from=android_608_Google&formats=aac&"
					+ "count={0}&token={1}&user_id={2}&expire={3}");
	
	public DoubanParser(Handler handler,UserLoginInfo userLoginInfo) {
		this.handler = handler;
		this.userLoginInfo = userLoginInfo;
	}
	

	public void downloadMyFavoriteSong() throws ClientProtocolException,
			IOException, JSONException, URISyntaxException {

		HttpClient client = new DefaultHttpClient();
		// 登录
		Object[] paramsObj = getParamsByLogin();
		String favoriteSongStr = getFavoriteSong(client, paramsObj);
		downloadFavoriteSongFromJsonStr(client, favoriteSongStr);
	}

	private Object[] getParamsByLogin(){
		
		Object[] paramsObj = new Object[] { downloadCount, userLoginInfo.getToken(), userLoginInfo.getUserId(), userLoginInfo.getExpire() };
		return paramsObj;
	}

	private String getFavoriteSong(HttpClient client, Object[] paramsObj)
			throws MalformedURLException, URISyntaxException, IOException,
			ClientProtocolException {
		HttpResponse response;
		BufferedReader rd;
		String favoriteSongUrl = doubanFavoriteSongUrlFormat.format(paramsObj);
		URL url = new URL(favoriteSongUrl);
		URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(),
				url.getQuery(), null);
		// 获取歌曲列表
		HttpGet get = new HttpGet(uri);
		response = client.execute(get);
		rd = new BufferedReader(new InputStreamReader(response.getEntity()
				.getContent()));
		StringBuilder sbFavorite = new StringBuilder();
		String favoriteLine = "";
		while ((favoriteLine = rd.readLine()) != null) {
			sbFavorite.append(favoriteLine);
		}
		return sbFavorite.toString();
	}

	private void downloadFavoriteSongFromJsonStr(HttpClient client,
			String favoriteLine) throws JSONException {
		JSONObject favoriteJson = new JSONObject(favoriteLine);
		JSONArray songArray = favoriteJson.getJSONArray("songs");
		//create file dir
		File SDCardRoot = Environment.getExternalStorageDirectory();
		File file = new File(SDCardRoot, "/music");
		file.mkdirs();
		for (int i = 0; i < songArray.length(); i++) {
			JSONObject songJson = songArray.getJSONObject(i);
			String songTitle = songJson.getString("title");
			String songUrl = songJson.getString("url");
			try {
				downloadSong(client, songTitle, songUrl);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		sendMsg(3,0,0,"end");

	}

	private void downloadSong(HttpClient client, String songTitle,
			String songUrl) throws ClientProtocolException, IOException {
		//从网上下载高码率的版本
		if(downloadFromInternet(client,songTitle)){
			return;
		}
		saveSong(client, songTitle, songUrl);
	}

	private boolean downloadFromInternet(HttpClient client, String songTitle) {
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
			saveSong(client, songTitle, internetDownloadUrl);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private void saveSong(HttpClient client, String songTitle, String songUrl)
			throws IOException, ClientProtocolException, FileNotFoundException {
		HttpGet get = new HttpGet(songUrl);
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


	public void setDownloadCount(int downloadCount) {
		this.downloadCount = downloadCount;
	}
	
}






