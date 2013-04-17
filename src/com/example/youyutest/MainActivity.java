package com.example.youyutest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.youyutest.domain.UserLoginInfo;
import com.example.youyutest.httpparser.DoubanParser;

public class MainActivity extends Activity implements OnClickListener {

	private Button downloadButton;
	private DoubanParser doubanParser;
	private ProgressBar progressBar;
	private Handler handler;
	private TextView downloadLable;
	private boolean logined;
	private Button downloadFromXiamiButton;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//�ж��Ƿ��¼�����û�е�¼��ת
		UserLoginInfo userLoginInfo = getUserLoginInfo();
		logined = !StringUtils.isBlank(userLoginInfo.getToken());
		if(!logined){
			startActivity(new Intent(MainActivity.this,LoginActivity.class));
		}
		handler = createHandler();
		doubanParser = new DoubanParser(handler,userLoginInfo);
		downloadButton = (Button) findViewById(R.id.downloadButton);
		downloadButton.setOnClickListener(this);
		
		downloadFromXiamiButton = (Button) findViewById(R.id.downloadFromXiamiButton);
		downloadFromXiamiButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this,XaimiDownloaderActivity.class));
			}
		});
		progressBar = (ProgressBar)findViewById(R.id.down_pb);
		downloadLable = (TextView) findViewById(R.id.textView1);


		
	}

	

	private Handler createHandler() {
		
		return new Handler(){
			
			@Override
			public void handleMessage(Message msg) {
				Bundle bundle = msg.getData();
				switch (msg.what) {
				case 0:
					downloadLable.setText(bundle.getString("songTitle")+"��ʼ����");
					progressBar.setMax(((Long)bundle.getLong("totalSize")).intValue());
					break;
				case 1:
					progressBar.setProgress(((Long)bundle.getLong("currentSize")).intValue());
					break;
				case 2:
					downloadLable.setText(bundle.getString("songTitle")+"��������");
					break;
				case 3:
					downloadLable.setText("�������");
					break;
				default:
					break;
				}
				super.handleMessage(msg);
			}
		};
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		int downloadCount = NumberUtils.toInt(((EditText)findViewById(R.id.downloadCount)).getText().toString());
		doubanParser.setDownloadCount(downloadCount);
		(new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					doubanParser.downloadMyFavoriteSong();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		})).start();

	}
	
	private UserLoginInfo getUserLoginInfo() {

		SharedPreferences userLoginInfoPre = getSharedPreferences("userLoginInfo", 0);
		UserLoginInfo userLoginInfo = new UserLoginInfo();
		userLoginInfo.setEmail(userLoginInfoPre.getString("email", ""));
		userLoginInfo.setPassword(userLoginInfoPre.getString("password", ""));
		userLoginInfo.setUserId(userLoginInfoPre.getString("userId", ""));
		userLoginInfo.setToken(userLoginInfoPre.getString("token", ""));
		userLoginInfo.setExpire(userLoginInfoPre.getString("expire", ""));
		return userLoginInfo;
	}

}
