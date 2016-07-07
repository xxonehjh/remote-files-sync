package com.hjh.remote_files_sync_app;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;

public class MainActivity extends Activity {

	private boolean stop = false;
	private EditText out;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		out = (EditText) this.findViewById(R.id.text_for_output);
		startMonitorState();
		
		Intent intent = new Intent(SyncService.ACTION);
		intent.putExtra("sync", true);
		startService(intent);
	}

	protected void onDestroy() {
		stop = true;
		super.onDestroy();
	}

	protected void startMonitorState() {
		new Thread() {
			public void run() {
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				while (!stop) {
					try {
						if (!stop) {
							updateState();
						}
						Thread.sleep(1000);
					} catch (Exception e) {
						HLog.instance.error("显示进度异常", e);
					}
				}
				try {
					updateState();
				} catch (IOException e) {
					Log.e("hjh", "获取状态失败", e);
				}
			}
		}.start();
	}

	private String last_detail = "";

	protected void updateState() throws IOException {
		File stdout = new File(SyncConfig.StoreStdoutPath);
		final String cur_detail = stdout.exists() ? FileUtils.readFileToString(
				stdout, "utf-8") : "";
		MainActivity.this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!last_detail.equals(cur_detail)) {
					out.setText(cur_detail);
					out.setSelection(cur_detail.length());
					last_detail = cur_detail;
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
