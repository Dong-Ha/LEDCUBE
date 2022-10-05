package com.example.ledcube;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.ledcube.ble.BleActivity;
import com.example.ledcube.gmselect.SelectActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button gamestart = findViewById(R.id.btnstart);
        gamestart.setOnClickListener(view -> {
            Intent i = new Intent( MainActivity.this, SelectActivity.class);
            startActivity(i);
        });

        Button blesetting = findViewById(R.id.btnsetting);
        blesetting.setOnClickListener(view -> {
            Intent i = new Intent( MainActivity.this, BleActivity.class);
            startActivity(i);
        });


    }
}