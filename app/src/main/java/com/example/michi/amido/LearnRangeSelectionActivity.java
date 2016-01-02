package com.example.michi.amido;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Date;

public class LearnRangeSelectionActivity extends AppCompatActivity {

    final static int STEP = 20;
    final static int MAX = 1000;

    String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_range_selection);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        type = getIntent().getStringExtra("type");

        ListView lv = (ListView)findViewById(R.id.range_list);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent myIntent;
                if (type.equals("draw")) {
                    myIntent = new Intent(LearnRangeSelectionActivity.this, LearnDrawActivity.class);
                } else {
                    myIntent = new Intent(LearnRangeSelectionActivity.this, LearnShowActivity.class);
                }
                myIntent.putExtra("type", type);
                myIntent.putExtra("list", getList(position));
                myIntent.putExtra("key", getKey(position));
                startActivity(myIntent);
            }
        });

        ProgressTracker pt = ProgressTracker.getInstance(this);

        ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        lv.setAdapter(aa);
        for (int i=0; i<MAX/STEP; i++) {
            String key = getKey(i);
            Date last = pt.getLast(type, key);
            if (last == null)
                aa.add(key);
            else
                aa.add(key + "       (" + pt.niceDate(last) + ")");
        }
    }

    public String getKey(int position) {
        return String.format("%d-%d", position * STEP+1, position * STEP + STEP);
    }

    public int[] getList(int position) {
        int first = position * STEP + 1;
        int last = position * STEP + STEP;
        int list[] = new int[last - first + 1];
        for (int i=first; i<=last; i++)
            list[i-first] = i;
        return list;
    }

}
