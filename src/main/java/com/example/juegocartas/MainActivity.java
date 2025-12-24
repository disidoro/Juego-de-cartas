package com.example.juegocartas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnContinuar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enlazamos el botón
        btnContinuar = findViewById(R.id.btnContinuar);

        // Al pulsar el botón, vamos a AjustesPartidaActivity
        btnContinuar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AjustesPartidaActivity.class);
            startActivity(intent);
        });
    }
}
