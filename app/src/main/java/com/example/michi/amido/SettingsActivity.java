package com.example.michi.amido;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

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
    }

    public void onKanaCheckbox(View v) {
        CheckBox b = (CheckBox)v;
        settings.setShowKana(b.isChecked());
    }

    public void onAdminCheckbox(View v) {
        CheckBox b = (CheckBox)v;
        settings.setAdminEnabled(b.isChecked());
    }

}
