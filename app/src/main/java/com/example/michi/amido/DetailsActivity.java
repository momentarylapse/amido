package com.example.michi.amido;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class DetailsActivity extends AppCompatActivity {


    private String type;
    private int char_id;
    private CharacterView characterView;


    public static void start(Context context, Character c) {
        Intent myIntent = new Intent(context, DetailsActivity.class);
        myIntent.putExtra("id", c.id);
        myIntent.putExtra("type", c.type);
        context.startActivity(myIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        char_id = getIntent().getIntExtra("id", 0);
        type = getIntent().getStringExtra("type");


        CharacterDatabase db = CharacterDatabase.getInstance(this);

        Character c = db.get(type, char_id);

        TextView tv = (TextView)findViewById(R.id.character);
        tv.setText(c.glyph);
        tv = (TextView)findViewById(R.id.pronunciation);
        tv.setText(c.getNicePronunciation(this));
        tv = (TextView)findViewById(R.id.english);
        tv.setText(c.niceList(c.english));
        tv = (TextView)findViewById(R.id.german);
        tv.setText(c.niceList(c.german));
        tv = (TextView)findViewById(R.id.id);
        tv.setText("" + c.id);

        characterView = (CharacterView)findViewById(R.id.view2);
        characterView.setDemo(c);
    }

}
