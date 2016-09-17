package com.example.michi.amido;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        settings = Settings.getInstance(this);

        CheckBox b1 = (CheckBox)findViewById(R.id.admin_checkbox);
        b1.setChecked(settings.isAdminEnabled());
        CheckBox b2 = (CheckBox)findViewById(R.id.kana_checkbox);
        b2.setChecked(settings.isShowKana());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, settings.getLearnCountList());

        Spinner s1 = (Spinner)findViewById(R.id.learn_draw_count);
        s1.setAdapter(adapter);
        s1.setSelection(settings.getLearnCountListIndex(settings.getLearnDrawCount()));
        s1.setOnItemSelectedListener(this);


        Spinner s2 = (Spinner)findViewById(R.id.learn_show_count);
        s2.setAdapter(adapter);
        s2.setSelection(settings.getLearnCountListIndex(settings.getLearnShowCount()));
        s2.setOnItemSelectedListener(this);

        EditText e = (EditText)findViewById(R.id.edit_user);
        e.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                settings.setUserName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        e.setText(settings.getUserName());

    }

    public void onKanaCheckbox(View v) {
        CheckBox b = (CheckBox)v;
        settings.setShowKana(b.isChecked());
    }

    public void onAdminCheckbox(View v) {
        CheckBox b = (CheckBox)v;
        settings.setAdminEnabled(b.isChecked());
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent == findViewById(R.id.learn_show_count))
            settings.setLearnShowCount(Integer.valueOf(settings.getLearnCountList().get(position)));
        if (parent == findViewById(R.id.learn_draw_count))
            settings.setLearnDrawCount(Integer.valueOf(settings.getLearnCountList().get(position)));

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
