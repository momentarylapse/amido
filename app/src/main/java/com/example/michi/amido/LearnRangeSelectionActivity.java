package com.example.michi.amido;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

public class LearnRangeSelectionActivity extends AppCompatActivity {

    String type;
    String method;

    ListManager listManager;
    ArrayList<ListManager.List> lists;
    ArrayList<String> listNames;
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

    int scoreColor(float score) {
        if (score == 0.0f)
            return Color.argb(0xff, 0xff, 0xff, 0xff);

        if (score > 1) {
            score = Math.min((float)Math.pow(score, 0.3f) + 0.2f, 2.0f);
            return Color.argb(0xff, (int) (0xff / score), (int) (0xff / score), 0xff);
        } else {
            score = Math.max(score - 0.2f, 0.5f);
            return Color.argb(0xff, 0xff, (int) (0xff * score), (int) (0xff * score));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        final ListView lv = (ListView)findViewById(R.id.range_list);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListManager.List list = lists.get(position);

                Intent myIntent;
                if (method.equals("draw")) {
                    myIntent = new Intent(LearnRangeSelectionActivity.this, LearnDrawActivity.class);
                } else if (method.equals("show")) {
                    myIntent = new Intent(LearnRangeSelectionActivity.this, LearnShowActivity.class);
                } else /*if (method.equals("flash"))*/ {
                    myIntent = new Intent(LearnRangeSelectionActivity.this, LearnFlashActivity.class);
                }
                myIntent.putExtra("type", type);
                myIntent.putExtra("method", method);
                myIntent.putExtra("key", list.key);
                startActivity(myIntent);
            }
        });

        final ProgressTracker pt = ProgressTracker.getInstance(this);

        int step = Settings.getInstance(this).getLearnCount(method);

        listManager = ListManager.getInstance(this);
        lists = listManager.getLists(type, step, 0);

        //ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        listNames = new ArrayList<>();
        for (ListManager.List l : lists) {
            Date last = pt.getLast(type, method, l.key);
            if (last == null)
                listNames.add(l.key);
            else
                //listNames.add(String.format("%s       (%s    %.1f)", l.key, pt.niceDate(last), pt.getScore(l, "draw")));
                listNames.add(String.format("%s       (%s)", l.key, pt.niceDate(last)));
        }
        //lv.setAdapter(aa);

        lv.setAdapter(new BaseAdapter()
        {
            public View getView(int position, View convertView, ViewGroup parent)
            {
                if (convertView == null)
                {
                    convertView = new TextView(LearnRangeSelectionActivity.this);
                    convertView.setPadding(10, 25, 10, 25);
                    ((TextView)convertView).setTextSize(20);
                    //((TextView)convertView).setTextColor(Color.WHITE);
                }

                //if (position == lv.getSelectedItemPosition())

                ListManager.List l = lists.get(position);
                float score = pt.getScore(l, method);
                convertView.setBackgroundColor(scoreColor(score));
                ((TextView) convertView).setText(listNames.get(position));

                return convertView;
            }

            public long getItemId(int position)
            {
                return position;
            }

            public Object getItem(int position)
            {
                return listNames.get(position);
            }

            public int getCount()
            {
                return listNames.size();
            }
        });



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
