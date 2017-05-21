package com.pm.library;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.library.widght.ptrlistview.PTRListView;


public class PTRListviewActivity extends Activity {

    private PTRListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ptrlist);

        mListView = (PTRListView) findViewById(R.id.lv_ptr);

        String[] number = getResources().getStringArray(R.array.number);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.item_ptrlist, R.id.tv ,number);
        mListView.setAdapter(adapter);

        mListView.setHeaderRefreshListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PTRListviewActivity.this, "PTRListviewActivity 开始刷新", Toast.LENGTH_SHORT).show();

                mListView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mListView.headerRefreshComplete();
                    }
                }, 1000);
            }
        });
    }
}
