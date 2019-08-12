package com.pm.library;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.library.widght.ReFlashListView;
import com.pm.hybridsdk.core.HybridConstant;
import com.pm.hybridsdk.ui.HybridWebViewActivity;
import com.qrcode.SampleActivity;

import java.util.ArrayList;

public class TestActivity extends AppCompatActivity {

    private static final String TAG = "TestActivity";
    private ListView listview;
    private RelativeLayout activitymain;
    
    private ArrayList<Class> mActivitys;
    private ArrayAdapter<Class> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        initialize();
        mActivitys=new ArrayList();
        initDta();
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1,mActivitys);
        listview.setAdapter(mAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Class aClass = mAdapter.getItem(position);
                Intent intent = new Intent(TestActivity.this, aClass);
                if(HybridWebViewActivity.class.isAssignableFrom(aClass)){
                    intent.putExtra(HybridConstant.INTENT_EXTRA_KEY_TOPAGE,"http://www.pmbloger.com/");
                }
                TestActivity.this.startActivity(intent);
            }
        });
    }

    private void initDta() {
        mActivitys.add(PTRListviewActivity.class);
        mActivitys.add(PullToRefreshActivity.class);
        mActivitys.add(XListViewActivity.class);
        mActivitys.add(IndexableListViewActivity.class);
        mActivitys.add(PermissionActivity.class);
//        mActivitys.add(ReFlashListViewActivity.class);
        mActivitys.add(SampleActivity.class);
        mActivitys.add(HybridWebViewActivity.class);
    }

    private void initialize() {

        listview = (ListView) findViewById(R.id.listview);
        activitymain = (RelativeLayout) findViewById(R.id.activity_main);
    }
}
