package com.hjh.remote_files_sync_app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class MainActivity extends Activity {

	private EditText serverInfoOut;
	private EditText out;
	private MsgReceiver msgReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		serverInfoOut = (EditText) this.findViewById(R.id.text_for_server_info);
		out = (EditText) this.findViewById(R.id.text_for_output);

		msgReceiver = new MsgReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(HLog.RECEIVER_ID);
		registerReceiver(msgReceiver, intentFilter);
		
		
		Intent intent = new Intent(SyncService.ACTION);
		intent.putExtra("sync_action", "get_server_info");
		startService(intent);
	}

	protected void onDestroy() {
		unregisterReceiver(msgReceiver);
		super.onDestroy();
	}

	protected void updateState(final String msg) {
		MainActivity.this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				out.append(msg);
			}
		});
	}

	protected void updateServerInfo(final String msg) {
		MainActivity.this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				serverInfoOut.setText(msg);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public class MsgReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.hasExtra("server_info")) {
				updateServerInfo(intent.getStringExtra("server_info"));
			}
			if (intent.hasExtra("log")) {
				updateState(intent.getStringExtra("log"));
			}
		}

	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_sync_stop: {
			Intent intent = new Intent(SyncService.ACTION);
			intent.putExtra("sync_action", "stop");
			startService(intent);
		}
			break;
		case R.id.action_sync: {
			Intent intent = new Intent(SyncService.ACTION);
			intent.putExtra("sync_action", "start");
			startService(intent);
		}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
