package com.example.youyutest.httpparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class BaiduMp3Downloader {
	private static String searchSongUrl = "http://music.baidu.com/search?key=";
	private static String downloadSongUrlFormat = "http://music.baidu.com/song/{0}/download?__o=%2Fsearch";

	public static void main(String[] args) throws ClientProtocolException,
			IOException, JSONException {
		downloadMap3Baidu("爱");
	}

	public static String downloadMap3Baidu(String songTitle)
			throws ClientProtocolException, IOException, JSONException {
		String baiduCookie = loginBaidu("xiaomomolqj","wabalqj");
		DefaultHttpClient client = new DefaultHttpClient();
		CookieStore cookieStore = new BasicCookieStore();
		String[]cookieArr = baiduCookie.split(";");
		for (int i = 0; i < cookieArr.length; i++) {
			String[] cookieItemArr = cookieArr[i].split("=");
			BasicClientCookie cookie = new BasicClientCookie(cookieItemArr[0], cookieArr[i].substring(cookieItemArr[0].length()));
			cookie.setVersion(0);
			cookie.setDomain(".baidu.com");
			cookie.setPath("/");
			cookieStore.addCookie(cookie);
		}
		client.setCookieStore(cookieStore);

		return getSongUrl(client, songTitle,baiduCookie);
		
	}

	private static String getSongUrl(HttpClient client, String songTitle,String baiduCookie)
			throws ClientProtocolException, IOException, JSONException {
		songTitle = songTitle.replaceAll(" ", "");
		HttpGet get = new HttpGet(searchSongUrl + songTitle);
		HttpResponse response = client.execute(get);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),"utf-8"));
		StringBuilder sbSongHtml = new StringBuilder();
		String loginLine = "";
		while ((loginLine = rd.readLine()) != null) {
			sbSongHtml.append(loginLine);
		}

		Document document = Jsoup.parse(sbSongHtml.toString());
		Element songElement = document.getElementsByClass("song-title").get(0);
		String songHerf = songElement.child(0).attr("href");
		String songInnerTitle = songElement.child(0).text();
		String songId = songHerf.split("/")[2];
		String songDownloadUrl = (new MessageFormat(downloadSongUrlFormat)
				.format(new Object[] { songId })).toString();
		return getSongDetailUrl(client, songDownloadUrl);
	}

	private static String getSongDetailUrl(HttpClient client,
			String songDownloadUrl) throws ClientProtocolException, IOException, JSONException {
		HttpGet get = new HttpGet(songDownloadUrl);
		HttpResponse response = client.execute(get);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),"utf-8"));
		StringBuilder sbSongHtml = new StringBuilder();
		String loginLine = "";
		while ((loginLine = rd.readLine()) != null) {
			sbSongHtml.append(loginLine);
		}

		Document document = Jsoup.parse(sbSongHtml.toString());
		Elements elements = document.getElementsByAttribute("data-data");
		Element songElement = null;
		if(elements.size()==3){
			//超高品质
			songElement = elements.get(2);
		}else if(elements.size()==2){
			//高品质
			songElement = elements.get(1);
		}else{
			//一般
			songElement = elements.get(0);
		}
		String songHerfStr = songElement.attr("data-data");
		JSONObject songJson = new JSONObject(songHerfStr);
		String songHerf = (String)songJson.get("link");
		songHerf = songHerf.substring(songHerf.indexOf("=")+1);
		
		return songHerf;
	}

	public static String loginBaidu(String username,String password) {
		
		  String retCookie = null;
		  URL url = null;
		  HttpURLConnection connection = null;
		  try {
		   url = new URL("http://www.baidu.com");
		   connection = (HttpURLConnection) url.openConnection();
		   connection.setRequestProperty("User-Agent","Internet Explorer");
		   connection.setRequestProperty("Host", "www.baidu.com");
		   connection.connect();
		   String cookieTemp = connection.getHeaderField("Set-Cookie");
		   connection.disconnect();
		   String token=getToken(cookieTemp);
		   url = new URL("https://passport.baidu.com/v2/api/?login");
		   StringBuffer loginPost = new StringBuffer();
		   loginPost.append("username="+username)
		     .append("&password="+password)
		     .append("&charset=utf-8")
		     .append("&callback=parent.bdPass.api.login._postCallback")
		     .append("&mem_pass=on")
		     .append("&tpl=mn")
		     .append("&staticpage=https://passport.baidu.com/v2Jump.html")
		     .append("&isPhone=false")
		     .append("&token="+token)
		     .append("&loginType=1")
		     .append("&index=0")
		     .append("&safeflg=0");
		   connection = (HttpURLConnection) url.openConnection();
		   HttpURLConnection.setFollowRedirects(true);
		   //connection.setInstanceFollowRedirects(true);
		   connection.setDoOutput(true); // 需要向服务器写数据
		   connection.setDoInput(true);
		   connection.setUseCaches(false); // 获得服务器最新的信息
		   connection.setAllowUserInteraction(false);
		   connection.setRequestMethod("POST");
		   setRequestProperty(connection,"https://passport.baidu.com/v2/?login&tpl=mn",cookieTemp,"passport.baidu.com");
		   connection.getOutputStream().write(loginPost.toString().getBytes());
		   connection.getOutputStream().flush();
		   connection.getOutputStream().close();
		   connection.connect();
		   String loginCookie = "";
		   Map<String,List<String>> fields=connection.getHeaderFields();
		   connection.getResponseCode();
		   String bdussCookie = "";  //用于连接首页所用的cookie
		   for(String s:fields.get("Set-Cookie")){
		    if(s.indexOf("BDUSS")!=-1){
		     bdussCookie=s;
		     loginCookie+=s;   //请求登录后获取的所有cookie
		    }
		   }
		   connection.disconnect();
		   retCookie = cookieTemp+";"+bdussCookie;
		   url = new URL("http://www.baidu.com/"); //连接到首页，查看是否登录成功
		   connection = (HttpURLConnection) url.openConnection();
		   connection.setRequestProperty("User-Agent","Internet Explorer");
		   connection.setRequestProperty("Host", "www.baidu.com");
		   connection.setRequestProperty("Cookie",cookieTemp+";"+bdussCookie);
		   connection.connect();
		   InputStream is = connection.getInputStream();
		   BufferedReader br = new BufferedReader(new InputStreamReader(is,"utf-8"));
		   String text;
		   while ((text = br.readLine()) != null) {
		    System.out.println(text);
		   }
		  } catch (Exception e) {
		   e.printStackTrace();
		  } finally {
		   if (connection != null)
		    connection.disconnect();
		  }
		  return retCookie;
		 }
		 
		 public static String getToken(String cookie){
		  URL url;
		  String token = "";
		  HttpURLConnection connection;
		  try {
		   url = new URL("https://passport.baidu.com/v2/api/?getapi&class=login&tpl=mn&tangram=false");
		   connection = (HttpURLConnection) url.openConnection();
		   connection.setRequestProperty("User-Agent","Internet Explorer");
		   connection.setRequestProperty("Host", "passport.baidu.com");
		   connection.setRequestProperty("Cookie", cookie);
		   connection.setRequestProperty("Referer", "https://passport.baidu.com/v2/?login&tpl=mn&u=http://www.baidu.com/");
		   connection.connect();
		   InputStream is = connection.getInputStream();
		   BufferedReader br = new BufferedReader(new InputStreamReader(is));
		   String text;
		   while ((text = br.readLine()) != null) {
		    Pattern p = Pattern.compile("(?<=bdPass.api.params.login_token=').*(?=';)");//正则表达式截取token字符串,这里可以不用正则,主要熟悉下正则
		    Matcher m = p.matcher(text);
		    if (m.find()) {
		     token = m.group(0);
		    }
		   }
		  } catch (MalformedURLException e) {
		   e.printStackTrace();
		  } catch (IOException e) {
		   e.printStackTrace();
		  }
		  return token;
		 }
		 
		 public static void setRequestProperty(HttpURLConnection connection,String connectionUrl,String cookie,String host){
		  connection.setRequestProperty("Connection",connectionUrl);
		  connection.setRequestProperty("Cookie", cookie);
		  connection.setRequestProperty("Host", host);
		 }
		 
}
