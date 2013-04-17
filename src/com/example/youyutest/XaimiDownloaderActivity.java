package com.example.youyutest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.youyutest.httpparser.XaimiDownloaderParser;

public class XaimiDownloaderActivity extends Activity implements OnClickListener {

	private Handler handler;
	private Button downloadButton;
	private ProgressBar progressBar;
	private TextView downloadLable;
	private XaimiDownloaderParser xiamiParser;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_xaimi_downloader);
		handler = createHandler();
	    xiamiParser = new XaimiDownloaderParser(handler);
	    downloadButton = (Button) findViewById(R.id.xiamiDownloadButton);
	    downloadButton.setOnClickListener(this);
	    progressBar = (ProgressBar)findViewById(R.id.xiamiPbDownload);
	    downloadLable = (TextView) findViewById(R.id.xiamiTextView1);
	}
	
	
	 private Handler createHandler() {
		    
		    return new Handler(){
		      
		      @Override
		      public void handleMessage(Message msg) {
		        Bundle bundle = msg.getData();
		        switch (msg.what) {
		        case 0:
		          downloadLable.setText(bundle.getString("songTitle")+"开始下载");
		          progressBar.setMax(((Long)bundle.getLong("totalSize")).intValue());
		          break;
		        case 1:
		          progressBar.setProgress(((Long)bundle.getLong("currentSize")).intValue());
		          break;
		        case 2:
		          downloadLable.setText(bundle.getString("songTitle")+"结束下载");
		          break;
		        case 3:
		          downloadLable.setText("下载完成");
		          break;
		        default:
		          break;
		        }
		        super.handleMessage(msg);
		      }
		    };
		  }


	@Override
	public void onClick(View v) {
		String xiamiId = ((EditText)findViewById(R.id.xiamiId)).getText().toString();
		xiamiParser.setXiamiId(xiamiId);
		(new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					xiamiParser.downloadXiami();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		})).start();
	}


}
