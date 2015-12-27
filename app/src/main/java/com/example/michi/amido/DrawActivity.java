package com.example.michi.amido;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

public class DrawActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    /*@Override
    protected void onCreate(Bundle savedInstanceState) {

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }*/

    private CharacterDatabase db;
    private CharacterDatabase.Answer answer_list;
    CharacterView characterView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        ListView lv = (ListView)findViewById(R.id.answer_list);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showDetails(answer_list.get(position).c);
            }
        });

        db = CharacterDatabase.getInstance(this);
        characterView = (CharacterView)findViewById(R.id.view);
    }

    public void showDetails(CharacterDatabase.Character c) {
        Intent myIntent = new Intent(DrawActivity.this, DetailsActivity.class);
        myIntent.putExtra("id", c.id);
        startActivity(myIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_draw, menu);

        SearchView sv = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        sv.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClearButton(View b) {
        characterView.clear();
    }

    public void setAnswers(CharacterDatabase.Answer al) {
        answer_list = al;

        ListView lv = (ListView)findViewById(R.id.answer_list);
        ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        lv.setAdapter(aa);
        for (CharacterDatabase.AnswerItem i : al){
            String s = getResources().getString(R.string.draw_answer_format);
            aa.add(String.format(s, i.c.glyph, i.c.getSimpleEnglish(), i.c.getSimpleGerman(), (int)(i.score * 100)));
        }
    }

    public void onOkButton(View b) {
        setAnswers(db.find(characterView.getDigest()));
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Intent myIntent = new Intent(DrawActivity.this, SearchActivity.class);
        myIntent.putExtra("query", query);
        startActivity(myIntent);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}
