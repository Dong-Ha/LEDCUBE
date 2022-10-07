package com.example.ledcube.gmselect;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.ledcube.R;
import com.example.ledcube.draw.DrawActivity;
import com.example.ledcube.maze.MazeActivity;

public class SelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.gameselect);

        Button game1 = findViewById(R.id.button1);
        game1.setOnClickListener(view -> {
            Intent i = new Intent(SelectActivity.this, MazeActivity.class);
            startActivity(i);
        });


        Button game2 = findViewById(R.id.button2);
        game2.setOnClickListener(view -> {
            Intent i = new Intent(SelectActivity.this, DrawActivity.class);
            startActivity(i);
        });

    }
}