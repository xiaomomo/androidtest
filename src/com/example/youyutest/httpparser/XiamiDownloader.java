package com.example.youyutest.httpparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class XiamiDownloader {

	private static final String albumUrlBase = "http://www.xiami.com/album/";
	private static final String collectUrlBase = "http://www.xiami.com/song/showcollect/id/";

	public static void main(String[] args) throws Exception {
		getSongListById("763880845");
	}

	public static List<String> getSongListById(String id) throws Exception {
		List<String> songList = null;
		try {
			 songList = getSongListByUrl(albumUrlBase+id);
		} catch (Exception e) {
			songList = getSongListByUrl(collectUrlBase+id); 
		}return songList;
		
	}


	private static List<String> getSongListByUrl(String songurl) throws Exception {
		
		StringBuilder sbAlbumHtml = getHtm(songurl);
		Document document = Jsoup.parse(sbAlbumHtml.toString());
		if(document.getElementsByClass("song_name").isEmpty()){
			throw new Exception("does't have a song");
		}
		Elements songTable = document.getElementsByClass("song_name");
		Iterator<Element> songIterator = songTable.iterator();
		List<String> songList = new ArrayList<String>();
		while(songIterator.hasNext()){
			Element songElement = songIterator.next();
			String songStr = songElement.getAllElements().get(1).text();
			if(songStr.contains("(")){
				songStr = songStr.substring(0,songStr.indexOf("("));
			}
			songList.add(songStr);
		}
		return songList;
	}

	private static StringBuilder getHtm(String songurl) throws IOException,
			ClientProtocolException, UnsupportedEncodingException {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(songurl);
		HttpResponse response = client.execute(get);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),"utf-8"));
		StringBuilder sbAlbumHtml = new StringBuilder();
		String line = "";
		while ((line = rd.readLine()) != null) {
			sbAlbumHtml.append(line);
		}
		return sbAlbumHtml;
	}
}
