package com.app.mobile.fast.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.app.mobile.fast.R;

public class HomeMotoristaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_motorista);
        getSupportActionBar().hide();
    }
}
