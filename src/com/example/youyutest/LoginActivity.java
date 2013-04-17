package com.example.youyutest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.youyutest.domain.UserLoginInfo;

public class LoginActivity extends Activity implements OnClickListener {
	
	private Button loginButton;
	private static final String doubanLoginUrl = "http://www.douban.com/j/app/login";
	private ProgressBar pbLogin;
	private EditText emailText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		loginButton = (Button)findViewById(R.id.loginButton);
		loginButton.setOnClickListener(this);
		pbLogin = (ProgressBar)findViewById(R.id.pbLogin);
		emailText = (EditText)findViewById(R.id.email);
		
	}

	@Override
	public void onClick(View v) {
		//登录后存储token等信息
		String email = ((EditText)findViewById(R.id.email)).getText().toString();
		String password = ((EditText)findViewById(R.id.password)).getText().toString();
		new LoginTask(email,password).execute();
	}
	
	private class LoginTask extends AsyncTask<URL, Integer, Integer>{

		private String email;
		private String password;
		
		public LoginTask(String email, String password) {
			this.email = email;
			this.password = password;
		}
		
		@Override
		protected Integer doInBackground(URL... params) {
			HttpClient client = new DefaultHttpClient();
			UserLoginInfo userLoginInfo = null;
			try {
				publishProgress(0);
				userLoginInfo = getParamsByLogin(client,email,password);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(null==userLoginInfo){
				publishProgress(1,-1);
			}else{
				saveUserLoginInfo(userLoginInfo);
				publishProgress(1,1);
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if(values[0]==0){
				pbLogin.setVisibility(View.VISIBLE);
			}else if(values[0]==1){
				pbLogin.setVisibility(View.INVISIBLE);
				if(values[1]==-1){
					emailText.setText("登录错误，请重新登录");
				}else{
					//FIXME 要再登录一次百度
					startActivity(new Intent(LoginActivity.this,MainActivity.class));
				}
			}
		}
		
		

		
		
	}
	
	private UserLoginInfo getParamsByLogin(HttpClient client, String email, String password)
			throws UnsupportedEncodingException, IOException,
			ClientProtocolException, JSONException {
		HttpPost post = new HttpPost(doubanLoginUrl);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("email", email));
		params.add(new BasicNameValuePair("version", "608"));
		params.add(new BasicNameValuePair("client",
				"s:mobile|y:android 4.1.1|f:608|m:Google|d:-1178839463|e:google_galaxy_nexus"));
		params.add(new BasicNameValuePair("app_name", "radio_android"));
		params.add(new BasicNameValuePair("password", password));
		params.add(new BasicNameValuePair("from", "android_608_Google"));
		post.setEntity(new UrlEncodedFormEntity(params));
		HttpResponse response = client.execute(post);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response
				.getEntity().getContent()));
		StringBuilder sbLogin = new StringBuilder();
		String loginLine = "";
		while ((loginLine = rd.readLine()) != null) {
			sbLogin.append(loginLine);
		}
		if(!sbLogin.toString().contains("token")){
			return null;
		}
		Document document = Jsoup.parse(sbLogin.toString());
		String body = document.body().html();
		String bodyJson = body.replace("&quot;", " ");
		JSONObject jsonObject = new JSONObject(bodyJson);
		UserLoginInfo userLoginInfo = new UserLoginInfo();
		userLoginInfo.setToken(jsonObject.getString("token"));
		userLoginInfo.setUserId(jsonObject.getString("user_id"));
		userLoginInfo.setExpire(jsonObject.getString("expire"));
		userLoginInfo.setEmail(email);
		userLoginInfo.setPassword(password);
		return userLoginInfo;
	}
	
	private void saveUserLoginInfo(UserLoginInfo userLoginInfo) {
		SharedPreferences userLoginInfoPre = getSharedPreferences("userLoginInfo", 0);
		SharedPreferences.Editor editor = userLoginInfoPre.edit();
		editor.putString("email", userLoginInfo.getEmail());
		editor.putString("password", userLoginInfo.getPassword());
		editor.putString("userId", userLoginInfo.getUserId());
		editor.putString("token", userLoginInfo.getToken());
		editor.putString("expire", userLoginInfo.getExpire());
		editor.commit();
	}
	
	
}









