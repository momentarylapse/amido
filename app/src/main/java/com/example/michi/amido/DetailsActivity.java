package com.example.michi.amido;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        CharacterDatabase db = CharacterDatabase.getInstance(this);

        int id = this.getIntent().getIntExtra("id", 0);
        CharacterDatabase.Character c = db.get(id);

        TextView tv = (TextView)findViewById(R.id.character);
        tv.setText(c.glyph);
        tv = (TextView)findViewById(R.id.pronunciation);
        tv.setText(c.pronunciation);
        tv = (TextView)findViewById(R.id.english);
        tv.setText(c.english);
        tv = (TextView)findViewById(R.id.german);
        tv.setText(c.german);

        CharacterView v = (CharacterView)findViewById(R.id.view2);
        v.setDemo(c);
    }
    @Override
    protected void onDestroy() {
        CharacterView v = (CharacterView)findViewById(R.id.view2);
        v.close();
        super.onDestroy();
    }

}
