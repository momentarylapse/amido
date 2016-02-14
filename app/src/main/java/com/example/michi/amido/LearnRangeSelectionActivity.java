package com.example.michi.amido;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

public class LearnRangeSelectionActivity extends AppCompatActivity {

    String type;
    String method;

    ListManager listManager;
    ArrayList<ListManager.List> lists;
    //int scrollPos;
    Parcelable state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_range_selection);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        type = getIntent().getStringExtra("type");
        method = getIntent().getStringExtra("method");

    }

    @Override
    protected void onResume() {
        super.onResume();

        ListView lv = (ListView)findViewById(R.id.range_list);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListManager.List list = lists.get(position);

                Intent myIntent;
                if (method.equals("draw")) {
                    myIntent = new Intent(LearnRangeSelectionActivity.this, LearnDrawActivity.class);
                } else {
                    myIntent = new Intent(LearnRangeSelectionActivity.this, LearnShowActivity.class);
                }
                myIntent.putExtra("type", type);
                myIntent.putExtra("method", method);
                myIntent.putExtra("key", list.key);
                startActivity(myIntent);
            }
        });

        ProgressTracker pt = ProgressTracker.getInstance(this);

        int step = Settings.getInstance(this).getLearnCount(method);

        listManager = ListManager.getInstance(this);
        lists = listManager.getLists(type, step, 0);

        ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        lv.setAdapter(aa);
        for (ListManager.List l : lists) {
            Date last = pt.getLast(type, method, l.key);
            if (last == null)
                aa.add(l.key);
            else
                aa.add(String.format("%s       (%s    %.1f)", l.key,pt.niceDate(last), pt.getScore(l, "draw")));
        }

        /*for (int i=0;i<5; i++) {
            if (lv.getChildAt(i) != null)
                lv.getChildAt(i).setBackgroundColor(Color.BLUE);
        }*/

        if (state != null)
            lv.onRestoreInstanceState(state);
    }

    @Override
    protected void onPause() {
        ListView lv = (ListView)findViewById(R.id.range_list);
        state = lv.onSaveInstanceState();
        super.onPause();
    }

}
