package com.example.michi.amido;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.DialogFragment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.example.michi.amido.data.Character;

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

        Settings.getInstance(this).setAdminAllowed(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_edit);
        if (Settings.getInstance(this).isAdmin()) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent myIntent = new Intent(DetailsActivity.this, EditActivity.class);
                    myIntent.putExtra("type", type);
                    myIntent.putExtra("id", char_id);
                    startActivity(myIntent);
                }
            });
        }else{
           fab.hide();
        }

        CharacterDatabase db = CharacterDatabase.getInstance(this);

        character = db.getSafe(type, char_id);

        TextView tv = (TextView) findViewById(R.id.character);
        tv.setText(character.glyph);
        tv = (TextView) findViewById(R.id.pronunciation);
        tv.setText(character.getNicePronunciation(this));
        tv = (TextView) findViewById(R.id.english);
        tv.setText(character.niceList(character.english));
        tv = (TextView) findViewById(R.id.german);
        tv.setText(character.niceList(character.german));
        tv = (TextView) findViewById(R.id.pinyin);
        tv.setText(character.niceList(character.pinyin));
        tv = (TextView) findViewById(R.id.id);
        tv.setText("" + character.id);

        characterView = (CharacterView) findViewById(R.id.view2);
        characterView.setDemo(character);
        //characterView.startAnimation();
    }

    public void onAddToListButton(View view) {
        //this.onBackPressed();
        DialogFragment f = AddToListFragment.newInstance(character);
        f.show(getFragmentManager(), "");
    }

    public void onEditButton(View view) {
        Intent myIntent = new Intent(DetailsActivity.this, EditActivity.class);
        myIntent.putExtra("type", type);
        myIntent.putExtra("id", char_id);
        startActivity(myIntent);
    }
}
