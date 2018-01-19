package com.example.michi.amido;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class LearnActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        TextView tv = (TextView)findViewById(R.id.learn_count);
        if (!tv.isInEditMode())
            tv.setText(String.format(getString(R.string.learn_count), ProgressTracker.getInstance(this).getLearnedCount("kanji", "draw")));
    }

    public void onShowButton(View view) {
        Intent myIntent = new Intent(LearnActivity.this, LearnRangeSelectionActivity.class);
        myIntent.putExtra("type", "kanji");
        myIntent.putExtra("method", "show");
        startActivity(myIntent);
    }

    public void onDrawButton(View view) {
        Intent myIntent = new Intent(LearnActivity.this, LearnRangeSelectionActivity.class);
        myIntent.putExtra("type", "kanji");
        myIntent.putExtra("method", "draw");
        startActivity(myIntent);
    }

    public void onFlashButton(View view) {
        Intent myIntent = new Intent(LearnActivity.this, LearnRangeSelectionActivity.class);
        myIntent.putExtra("type", "kanji");
        myIntent.putExtra("method", "flash");
        startActivity(myIntent);
    }
}
