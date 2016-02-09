package com.example.michi.amido;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class DetailsActivity extends AppCompatActivity {


    private String type;
    private int char_id;
    private CharacterView characterView;

    Character character;


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

        character = db.get(type, char_id);

        TextView tv = (TextView) findViewById(R.id.character);
        tv.setText(character.glyph);
        tv = (TextView) findViewById(R.id.pronunciation);
        tv.setText(character.getNicePronunciation(this));
        tv = (TextView) findViewById(R.id.english);
        tv.setText(character.niceList(character.english));
        tv = (TextView) findViewById(R.id.german);
        tv.setText(character.niceList(character.german));
        tv = (TextView) findViewById(R.id.id);
        tv.setText("" + character.id);

        characterView = (CharacterView) findViewById(R.id.view2);
        characterView.setDemo(character);
    }

    public void onAddToListButton(View view) {
        //this.onBackPressed();
        DialogFragment f = AddToListFragment.newInstance(character);
        f.show(getFragmentManager(), "");
    }
}