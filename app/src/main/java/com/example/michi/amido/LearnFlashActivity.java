package com.example.michi.amido;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.example.michi.amido.data.Character;

import java.util.ArrayList;
import java.util.Random;

public class LearnFlashActivity extends AppCompatActivity {
    CharacterView characterView;

    ArrayList<Character> list = null;
    Character curCharacter = null;
    int done;

    String type;
    String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_flash);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        characterView = (CharacterView) findViewById(R.id.view);

        CharacterDatabase db = CharacterDatabase.getInstance(this);

        type = getIntent().getStringExtra("type");
        key = getIntent().getStringExtra("key");

        if (list == null) {
            ListManager.List l = ListManager.getInstance(this).getList(type, key);
            //int ids[] = getIntent().getIntArrayExtra("list");
            list = db.get(type, l.getIds());
            done = 0;

            chooseRandom();
        }
    }

    public void setCharacter(Character c) {
        curCharacter = c;
        TextView tv = (TextView)findViewById(R.id.translation);
        tv.setText(c.niceList(c.getTranslation(this)));
        tv = (TextView)findViewById(R.id.pronunciation);
        tv.setText(c.getNicePronunciation(this));
        updateStatus();
        characterView.setDemo(curCharacter);
        //setMode(Mode.DEMO);
    }

    /*public void setMode(Mode m) {
        mode = m;
        ImageButton b1 = (ImageButton)findViewById(R.id.button_undo);
        b1.setVisibility(m == Mode.USER ? View.VISIBLE : View.INVISIBLE);
        ImageButton b2 = (ImageButton)findViewById(R.id.button_clear);
        b2.setVisibility(m == Mode.USER ? View.VISIBLE : View.INVISIBLE);
        characterView.clear();
        if (m == Mode.DEMO) {
            characterView.setDemo(curCharacter);
        }
    }*/

    public void updateStatus() {
        TextView tv = (TextView)findViewById(R.id.status);
        tv.setText(String.format(getResources().getString(R.string.learn_draw_status), done, list.size() + done));
    }

    public void chooseRandom() {
        if (list.size() == 0) {
            ProgressTracker p = ProgressTracker.getInstance(this);
            p.add(type, "show", key, 0);

            setContentView(R.layout.activity_learn_done);
            return;
        }
        Random r = new Random();
        setCharacter(list.get(r.nextInt(list.size())));
    }

    public void onClearButton(View b) {
        chooseRandom();
    }

    /*public void onDeleteStrokeButton(View b) {
        characterView.deleteLastStroke();
    }*/

    public void onOkButton(View b) {
        list.remove(curCharacter);
        done ++;
        chooseRandom();
    }

    public void onNoButton(View b) {
        chooseRandom();
    }

}
