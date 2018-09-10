package com.devandroid.fbatista.uberclone.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.devandroid.fbatista.uberclone.R;

public class HomeMotoristaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_motorista);
        getSupportActionBar().hide();
    }
}
