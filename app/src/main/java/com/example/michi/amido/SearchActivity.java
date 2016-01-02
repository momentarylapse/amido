package com.example.michi.amido;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;


public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private CharacterDatabase db;
    private CharacterDatabase.Answer answer_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ListView lv = (ListView)findViewById(R.id.answer_list);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                      @Override
                                      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                          showDetails(answer_list.get(position).c);
                                      }
                                  });

        db = CharacterDatabase.getInstance(this);


        String query = getIntent().getStringExtra("query");
        setAnswers(db.find(query), query);
    }

    public void showDetails(CharacterDatabase.Character c) {
        DialogFragment f = DetailsFragment.newInstance(c);
        f.show(getFragmentManager(), "");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);

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

    public void setAnswers(CharacterDatabase.Answer al, String query) {
        answer_list = al;

        ListView lv = (ListView)findViewById(R.id.answer_list);
        ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        lv.setAdapter(aa);
        for (CharacterDatabase.AnswerItem i : al){
            String s = getResources().getString(R.string.search_answer_format);
            aa.add(String.format(s, i.c.glyph, i.c.getSimpleTranslation()));
        }

        if (al.size() == 0) {
            String s = getResources().getString(R.string.search_nothing);
            Toast.makeText(this, String.format(s, query), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        setAnswers(db.find(query), query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}
