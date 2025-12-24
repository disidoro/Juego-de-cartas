package com.example.juegocartas;

// Importaciones necesarias
import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * AjustesPartidaActivity
 * ---------------------
 * Configura la partida antes de empezar:
 * - Número de jugadores
 * - Nombres de los jugadores
 */
public class AjustesPartidaActivity extends AppCompatActivity {

    // Spinner para seleccionar el número de jugadores (2 a 5)
    private Spinner spinnerJugadores;

    // Layout donde se añadirán dinámicamente los EditText de los nombres
    private LinearLayout layoutNombres;

    // Botón para iniciar la partida
    private Button btnEmpezarPartida;

    // Lista donde se guardan los EditText creados dinámicamente
    private List<EditText> camposNombres = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ajuste_partida);

        // Enlazamos vistas
        spinnerJugadores = findViewById(R.id.spinnerJugadores);
        layoutNombres = findViewById(R.id.layoutNombres);
        btnEmpezarPartida = findViewById(R.id.btnEmpezarPartida);

        configurarSpinner();
        configurarBoton();
    }

    // ---------------- SPINNER ----------------

    private void configurarSpinner() {

        Integer[] opciones = {2, 3, 4, 5};

        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                opciones
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerJugadores.setAdapter(adapter);

        spinnerJugadores.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                int numeroJugadores = (int) parent.getItemAtPosition(position);
                crearCamposNombres(numeroJugadores);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No se usa
            }
        });
    }

    // ---------------- CREAR EditText ----------------

    private void crearCamposNombres(int numeroJugadores) {

        layoutNombres.removeAllViews();
        camposNombres.clear();

        for (int i = 1; i <= numeroJugadores; i++) {
            EditText editText = new EditText(this);
            editText.setHint("Nombre jugador " + i);

            editText.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    )
            );

            layoutNombres.addView(editText);
            camposNombres.add(editText);
        }
    }

    // ---------------- BOTÓN ----------------

    private void configurarBoton() {

        btnEmpezarPartida.setOnClickListener(v -> {

            List<Jugador> jugadores = new ArrayList<>();

            for (EditText editText : camposNombres) {
                String nombre = editText.getText().toString().trim();

                if (nombre.isEmpty()) {
                    Toast.makeText(this,
                            "Todos los jugadores deben tener nombre",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                jugadores.add(new Jugador(nombre, ""));
            }

            // Crear intent y enviar jugadores
            Intent intent = new Intent(this, PartidaActivity.class);
            intent.putExtra("jugadores", (Serializable) jugadores);

            startActivity(intent);
        });
    }
}
