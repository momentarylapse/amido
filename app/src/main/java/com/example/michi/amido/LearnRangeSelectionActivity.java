package com.example.michi.amido;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class LearnRangeSelectionActivity extends AppCompatActivity {

    final static int STEP = 20;
    final static int MAX = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_range_selection);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        ListView lv = (ListView)findViewById(R.id.range_list);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String type = getIntent().getStringExtra("type");

                if (type.equals("draw")) {
                    Intent myIntent = new Intent(LearnRangeSelectionActivity.this, LearnDrawActivity.class);
                    myIntent.putExtra("first", position * STEP + 1);
                    myIntent.putExtra("last", position * STEP + STEP);
                    startActivity(myIntent);
                } else {
                    /*Intent myIntent = new Intent(LearnRangeSelectionActivity.this, LearnShowActivity.class);
                    myIntent.putExtra("first", position * STEP + 1);
                    myIntent.putExtra("last", position * STEP + STEP);
                    startActivity(myIntent);*/
                    Toast.makeText(LearnRangeSelectionActivity.this, "not implemented yet: " + type, Toast.LENGTH_SHORT).show();
                }
            }
        });

        ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        lv.setAdapter(aa);
        for (int i=0; i<MAX; i+=STEP) {
            aa.add(String.format("%d-%d", i+1, i+STEP));
        }
    }

}
