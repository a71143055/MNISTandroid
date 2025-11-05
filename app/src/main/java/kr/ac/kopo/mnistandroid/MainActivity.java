package kr.ac.kopo.mnistandroid;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button drawBtn = findViewById(R.id.button);
        drawBtn.setOnClickListener(view -> {
            Intent i = new Intent(MainActivity.this, DrawActivity.class);
            startActivity(i);
        });
    }
}