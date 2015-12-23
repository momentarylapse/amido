package com.example.michi.amido;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;


public class SearchActivity extends AppCompatActivity {

    private CharacterDatabase db;
    private CharacterDatabase.Answer answer_list;
    CharacterView characterView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SearchView sv = (SearchView)findViewById(R.id.searchView);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                setAnswers(db.find(query));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


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
        Intent myIntent = new Intent(SearchActivity.this, DetailsActivity.class);
        myIntent.putExtra("id", c.id);
        startActivity(myIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_draw, menu);
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
        for (CharacterDatabase.AnswerItem i : al)
            aa.add(i.c.glyph + " - " + i.c.english + " - " + (int)(i.score * 100) + "%");
    }

    public void onOkButton(View b) {
        setAnswers(db.find(characterView.getDigest()));
    }
}
