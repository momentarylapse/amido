package com.example.michi.amido;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class LearnShowActivity extends AppCompatActivity {
    CharacterView characterView;

    ArrayList<Character> list;
    Character curCharacter;
    int done;

    String type;
    String key;

    enum Mode {
        DEMO,
        USER
    }
    Mode mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_show);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        characterView = (CharacterView) findViewById(R.id.view);

        CharacterDatabase db = CharacterDatabase.getInstance(this);

        type = getIntent().getStringExtra("type");
        key = getIntent().getStringExtra("key");

        int ids[] = getIntent().getIntArrayExtra("list");
        list = db.get(type, ids);
        done = 0;

        chooseRandom();
    }

    public void setCharacter(Character c) {
        curCharacter = c;
        TextView tv = (TextView)findViewById(R.id.translation);
        tv.setText(c.niceList(c.getTranslation(this)));
        tv = (TextView)findViewById(R.id.pronunciation);
        tv.setText(c.getNicePronunciation(this));
        updateStatus();
        setMode(Mode.DEMO);
    }

    public void setMode(Mode m) {
        mode = m;
        ImageButton b1 = (ImageButton)findViewById(R.id.button_undo);
        b1.setVisibility(m == Mode.USER ? View.VISIBLE : View.INVISIBLE);
        ImageButton b2 = (ImageButton)findViewById(R.id.button_clear);
        b2.setVisibility(m == Mode.USER ? View.VISIBLE : View.INVISIBLE);
        characterView.clear();
        if (m == Mode.DEMO) {
            characterView.setDemo(curCharacter);
        }
    }

    public void updateStatus() {
        TextView tv = (TextView)findViewById(R.id.status);
        tv.setText(String.format(getResources().getString(R.string.learn_draw_status), done, list.size() + done));
    }

    public void chooseRandom() {
        if (list.size() == 0) {
            ProgressTracker p = ProgressTracker.getInstance(this);
            p.add(type, "show", key, 0);

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
        if (mode == Mode.DEMO) {
            setMode(Mode.USER);
            return;
        }
        float score = curCharacter.score(characterView.getDigest());
        if (score > 0) {
            Toast.makeText(this, String.format(getResources().getString(R.string.learn_draw_correct), curCharacter.glyph, CharacterDatabase.scoreToString(score)), Toast.LENGTH_SHORT).show();
            list.remove(curCharacter);
            done ++;
            chooseRandom();

        } else {
            Toast.makeText(this, String.format(getResources().getString(R.string.learn_draw_wrong), curCharacter.glyph), Toast.LENGTH_SHORT).show();
            setMode(Mode.DEMO);
        }
    }

}
