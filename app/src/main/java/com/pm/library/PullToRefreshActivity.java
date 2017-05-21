package com.pm.library;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.library.widght.PullToRefreshView;

public class PullToRefreshActivity extends Activity implements PullToRefreshView.OnFooterRefreshListener,
		PullToRefreshView.OnHeaderRefreshListener {
	private PullToRefreshView mPullToRefreshView;
	private ListView lv;
	private ArrayAdapter<String> adapter;
	private String[] items = new String[20];

	private long mExitTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pull_to_refresh);

		mPullToRefreshView = (PullToRefreshView) findViewById(R.id.pull_to_refresh_view);
		lv = (ListView) findViewById(R.id.lv);

		for (int i = 0; i < 20; i++) {
			items[i] = "item" + i;
		}
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, items);
		lv.setAdapter(adapter);

		mPullToRefreshView.setOnHeaderRefreshListener(this);
		mPullToRefreshView.setOnFooterRefreshListener(this);

	}

	@Override
	public void onFooterRefresh(PullToRefreshView view) {
		loadMoreData();
	}

	@Override
	public void onHeaderRefresh(PullToRefreshView view) {
		refreshData();
	}

	public void refreshData() {
		try {
			Thread.sleep(2 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		mPullToRefreshView.onHeaderRefreshComplete();
		Toast.makeText(PullToRefreshActivity.this, "刷新完成", Toast.LENGTH_SHORT).show();
	}

	public void loadMoreData() {
		try {
			Thread.sleep(2 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		mPullToRefreshView.onFooterRefreshComplete();
		Toast.makeText(PullToRefreshActivity.this, "加载完成", Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_UP) {
			if (System.currentTimeMillis() - mExitTime > 2 * 1000) {
				Toast.makeText(this, "加载完成", Toast.LENGTH_SHORT).show();
				mExitTime = System.currentTimeMillis();
			} else {
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		}
		return super.onKeyUp(keyCode, event);
	}

}
