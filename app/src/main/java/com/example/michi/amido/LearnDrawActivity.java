package com.example.michi.amido;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LearnDrawActivity extends AppCompatActivity {

    CharacterView characterView;

    ArrayList<CharacterDatabase.Character> list;
    CharacterDatabase.Character curCharacter;
    int done;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_draw);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        characterView = (CharacterView) findViewById(R.id.view);

        CharacterDatabase db = CharacterDatabase.getInstance(this);

        int ids[] = getIntent().getIntArrayExtra("list");
        list = db.get(ids);
        done = 0;

        chooseRandom();
    }

    public void setCharacter(CharacterDatabase.Character c) {
        curCharacter = c;
        TextView tv = (TextView)findViewById(R.id.translation);
        tv.setText(c.german);
        tv = (TextView)findViewById(R.id.pronunciation);
        tv.setText(c.pronunciation);
        characterView.setAutoClear();
        tv = (TextView)findViewById(R.id.status);
        tv.setText(String.format(getResources().getString(R.string.learn_draw_status), done, list.size() + done));
    }

    public void chooseRandom() {
        if (list.size() == 0) {
            Intent myIntent = new Intent(this, LearnDoneActivity.class);
            startActivity(myIntent);
            return;
        }
        Random r = new Random();
        setCharacter(list.get(r.nextInt(list.size())));
    }

    public void onClearButton(View b) {
        characterView.clear();
    }

    public void onDeleteStrokeButton(View b) {
        characterView.deleteLastStroke();
    }

    public void onOkButton(View b) {
        float score = curCharacter.score(characterView.getDigest());
        if (score > 0) {
            Toast.makeText(this, String.format(getResources().getString(R.string.learn_draw_correct), curCharacter.glyph, (int)(score * 100.0f)), Toast.LENGTH_SHORT).show();
            list.remove(curCharacter);
            done ++;

        } else {
            Toast.makeText(this, String.format(getResources().getString(R.string.learn_draw_wrong), curCharacter.glyph), Toast.LENGTH_SHORT).show();

        }
        chooseRandom();
    }

}
