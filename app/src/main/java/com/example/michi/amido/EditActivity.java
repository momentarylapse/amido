package com.example.michi.amido;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class EditActivity extends AppCompatActivity {

    private String type;
    private int char_id;
    private CharacterView characterView;

    Character character;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        char_id = getIntent().getIntExtra("id", 0);
        type = getIntent().getStringExtra("type");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_ok);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText et = (EditText) findViewById(R.id.pronunciation);
                character.pronunciation = et.getText().toString();
                et = (EditText) findViewById(R.id.english);
                character.english = et.getText().toString();
                et = (EditText) findViewById(R.id.german);
                character.german = et.getText().toString();
                et = (EditText) findViewById(R.id.pinyin);
                character.pinyin = et.getText().toString();

                Log.i("xxx", character.strokes);
                ArrayList<Stroke> strokes = characterView.getStrokes();
                character.setStrokes(strokes, CharacterDatabase.digestStrokes(strokes));
                Log.i("xxx", character.strokes);

                character.changed = true;
                CharacterDatabase.getInstance(EditActivity.this).save();
                //EditActivity.this.onBackPressed();
            }
        });

        setCharacter();
    }

    public void setCharacter() {

        CharacterDatabase db = CharacterDatabase.getInstance(this);

        character = db.getSafe(type, char_id);

        TextView tv = (TextView) findViewById(R.id.character);
        tv.setText(character.glyph);
        EditText et = (EditText) findViewById(R.id.pronunciation);
        et.setText(character.pronunciation);
        et = (EditText) findViewById(R.id.english);
        et.setText(character.english);
        et = (EditText) findViewById(R.id.german);
        et.setText(character.german);
        et = (EditText) findViewById(R.id.pinyin);
        et.setText(character.pinyin);
        tv = (TextView) findViewById(R.id.id);
        tv.setText("" + character.id);

        characterView = (CharacterView) findViewById(R.id.view);
        characterView.setStrokes(character);
    }

    public void onClearButton(View b) {
        characterView.clear();
    }

    public void onDeleteStrokeButton(View b) {
        characterView.deleteLastStroke();
    }


    public void onNextButton(View b) {
        char_id ++;
        setCharacter();
    }

    public void onPreviousButton(View b) {
        char_id --;
        setCharacter();
    }

}
