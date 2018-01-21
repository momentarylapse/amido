package com.example.michi.amido;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.michi.amido.data.Character;

import java.util.ArrayList;
import java.util.Random;

public class LearnDrawActivity extends AppCompatActivity {

    CharacterView characterView;

    ArrayList<Character> list;
    Character curCharacter = null;
    Character lastCharacter = null;
    int done;
    int all;
    float score_sum;
    String type;
    String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_draw);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        characterView = (CharacterView) findViewById(R.id.view);

        CharacterDatabase db = CharacterDatabase.getInstance(this);

        type = getIntent().getStringExtra("type");
        key = getIntent().getStringExtra("key");

        ListManager.List l = ListManager.getInstance(this).getList(type, key);
        //int ids[] = getIntent().getIntArrayExtra("list");
        list = db.get(type, l.getIds());
        done = 0;
        all = list.size();
        score_sum = 0;

        Button b = (Button)findViewById(R.id.button_show_last);
        b.setVisibility(View.INVISIBLE);

        chooseRandom();
    }

    @Override
    public void onBackPressed() {
        if (list.size() == 0) {
            finish();
            return;
        }

        // not finished? -> ask
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        finish();
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.learn_question_quit)).setPositiveButton(getResources().getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getResources().getString(R.string.no), dialogClickListener).show();
    }

    public void setCharacter(Character c) {
        curCharacter = c;
        TextView tv = (TextView)findViewById(R.id.translation);
        tv.setText(c.niceList(c.getTranslation(this)));
        tv = (TextView)findViewById(R.id.pronunciation);
        tv.setText(c.getNicePronunciation(this));
        characterView.setAutoClear();
        updateStatus();
    }

    public void updateStatus() {
        TextView tv = (TextView)findViewById(R.id.status);
        tv.setText(String.format(getResources().getString(R.string.learn_draw_status), done, all));
    }

    public void chooseRandom() {
        if (list.size() == 0) {
            ProgressTracker p = ProgressTracker.getInstance(this);
            p.add(type, "draw", key, score_sum / all);


            setContentView(R.layout.activity_learn_done);
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
            Toast.makeText(this, String.format(getResources().getString(R.string.learn_draw_correct), curCharacter.glyph, CharacterDatabase.scoreToString(score)), Toast.LENGTH_SHORT).show();
            list.remove(curCharacter);
            done ++;
            score_sum += score;

        } else {
            Toast.makeText(this, String.format(getResources().getString(R.string.learn_draw_wrong), curCharacter.glyph), Toast.LENGTH_SHORT).show();
        }
        setLastCharacter(curCharacter);
        chooseRandom();
    }

    public void setLastCharacter(Character c) {
        lastCharacter = c;

        Button b = (Button)findViewById(R.id.button_show_last);
        b.setVisibility(View.VISIBLE);
    }

    public void onShowLastCharacterButton(View v) {
        DetailsActivity.start(this, lastCharacter);
    }

}
