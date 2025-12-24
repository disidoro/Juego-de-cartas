package com.example.juegocartas;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PartidaActivity extends AppCompatActivity {

    // ----------- VISTAS -----------
    private TextView tvTurno, tvObjetivo, tvGanadas, tvRestantes;
    private LinearLayout layoutMano, layoutMesa;
    private Button btnConfirmar;
    private EditText edtPrediccion;

    // ----------- DATOS -----------
    private List<Jugador> jugadores;
    private List<Carta> baraja;

    private int indiceJugadorActual = 0;
    private int bazasJugadas = 0;

    // ----------- FASES -----------
    private enum Fase { PREDICCION, JUGAR_BAZA, FIN_RONDA }
    private Fase faseActual = Fase.PREDICCION;

    // ----------- CONFIG -----------
    private static final int TOTAL_BAZAS = 5;
    private int sumaPredicciones = 0;

    private final List<Carta> cartasEnMesa = new ArrayList<>();
    private final List<Jugador> jugadoresEnMesa = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partida);

        tvTurno = findViewById(R.id.tvTurno);
        tvObjetivo = findViewById(R.id.tvObjetivo);
        tvGanadas = findViewById(R.id.tvGanadas);
        tvRestantes = findViewById(R.id.tvRestantes);
        layoutMano = findViewById(R.id.layoutMano);
        layoutMesa = findViewById(R.id.layoutMesa);
        btnConfirmar = findViewById(R.id.btnConfirmar);
        edtPrediccion = findViewById(R.id.edtPrediccion);

        jugadores = (List<Jugador>) getIntent().getSerializableExtra("jugadores");
        if (jugadores == null || jugadores.size() < 2) {
            Toast.makeText(this, "Se necesitan al menos 2 jugadores", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        baraja = crearBaraja();
        repartirCartas();
        mostrarJugadorActual();

        btnConfirmar.setOnClickListener(v -> {
            if (faseActual == Fase.PREDICCION) confirmarPrediccion();
        });
    }

    // ----------- BARAJA -----------
    private List<Carta> crearBaraja() {
        List<Carta> baraja = new ArrayList<>();
        String[] palos = {"Oros", "Copas", "Espadas", "Bastos"};

        for (String palo : palos) {
            for (int v = 1; v <= 12; v++) {
                baraja.add(new Carta(v, v + " de " + palo));
            }
        }
        Collections.shuffle(baraja);
        return baraja;
    }

    private void repartirCartas() {
        for (Jugador j : jugadores) {
            j.getCartas().clear();
            j.setBazasGanadas(0);
            for (int i = 0; i < TOTAL_BAZAS; i++) {
                j.getCartas().add(baraja.remove(0));
            }
        }
    }

    // ----------- MOSTRAR JUGADOR -----------
    private void mostrarJugadorActual() {
        if (faseActual == Fase.FIN_RONDA) return;

        Jugador j = jugadores.get(indiceJugadorActual);

        tvTurno.setText("Turno de: " + j.getNombreUsuario());
        actualizarInfoJugador(j);

        layoutMano.removeAllViews();

        for (Carta c : j.getCartas()) {
            TextView tv = new TextView(this);
            tv.setText(c.getNombre());
            tv.setPadding(20, 20, 20, 20);

            if (faseActual == Fase.JUGAR_BAZA) {
                tv.setOnClickListener(v -> jugarCarta(c));
            }

            layoutMano.addView(tv);
        }
    }

    private void actualizarInfoJugador(Jugador j) {
        int restantes = Math.max(0, j.getPrediccion() - j.getBazasGanadas());
        tvObjetivo.setText("Objetivo: " + j.getPrediccion());
        tvGanadas.setText("Ganadas: " + j.getBazasGanadas());
        tvRestantes.setText("Restantes: " + restantes);
    }

    // ----------- PREDICCIÓN -----------
    private void confirmarPrediccion() {
        String txt = edtPrediccion.getText().toString().trim();
        if (txt.isEmpty()) {
            edtPrediccion.setError("Introduce un número");
            return;
        }

        int pred = Integer.parseInt(txt);
        if (pred < 0 || pred > TOTAL_BAZAS) {
            edtPrediccion.setError("Número no válido");
            return;
        }

        boolean ultimo = indiceJugadorActual == jugadores.size() - 1;
        if (ultimo && sumaPredicciones + pred == TOTAL_BAZAS) {
            edtPrediccion.setError("La suma no puede ser " + TOTAL_BAZAS);
            return;
        }

        Jugador jugador = jugadores.get(indiceJugadorActual);
        jugador.setPrediccion(pred);
        sumaPredicciones += pred;

        // 👉 MOSTRAR PREDICCIÓN EN LA MESA
        TextView tvPred = new TextView(this);
        tvPred.setText(jugador.getNombreUsuario() + " predice " + pred + " baza(s)");
        tvPred.setPadding(16, 16, 16, 16);
        layoutMesa.addView(tvPred);

        indiceJugadorActual++;

        if (indiceJugadorActual >= jugadores.size()) {
            faseActual = Fase.JUGAR_BAZA;
            indiceJugadorActual = 0;
            btnConfirmar.setEnabled(false);
            edtPrediccion.setEnabled(false);
            layoutMesa.removeAllViews(); // limpiar predicciones
            Toast.makeText(this, "Empieza la ronda", Toast.LENGTH_SHORT).show();
        }

        mostrarJugadorActual();
    }

    // ----------- JUGAR CARTA -----------
    private void jugarCarta(Carta carta) {
        if (faseActual != Fase.JUGAR_BAZA) return;

        Jugador jugador = jugadores.get(indiceJugadorActual);
        jugador.jugarCarta(carta);

        cartasEnMesa.add(carta);
        jugadoresEnMesa.add(jugador);

        TextView tvMesa = new TextView(this);
        tvMesa.setText(jugador.getNombreUsuario() + ": " + carta.getNombre());
        tvMesa.setPadding(16, 16, 16, 16);
        layoutMesa.addView(tvMesa);

        indiceJugadorActual = (indiceJugadorActual + 1) % jugadores.size();

        if (cartasEnMesa.size() == jugadores.size()) {
            resolverBaza();
        } else {
            mostrarJugadorActual();
        }
    }

    // ----------- RESOLVER BAZA -----------
    private void resolverBaza() {

        Carta mejor = cartasEnMesa.get(0);
        Jugador ganador = jugadoresEnMesa.get(0);

        for (int i = 1; i < cartasEnMesa.size(); i++) {
            if (fuerzaCarta(cartasEnMesa.get(i)) > fuerzaCarta(mejor)) {
                mejor = cartasEnMesa.get(i);
                ganador = jugadoresEnMesa.get(i);
            }
        }

        ganador.setBazasGanadas(ganador.getBazasGanadas() + 1);
        bazasJugadas++;

        mostrarDialogoBaza(ganador, mejor);
    }

    private int fuerzaCarta(Carta c) {
        return c.getValor() == 1 ? 100 : c.getValor();
    }

    // ----------- DIÁLOGOS -----------
    private void mostrarDialogoBaza(Jugador ganador, Carta cartaGanadora) {

        StringBuilder msg = new StringBuilder("Cartas jugadas:\n\n");

        for (int i = 0; i < cartasEnMesa.size(); i++) {
            msg.append(jugadoresEnMesa.get(i).getNombreUsuario())
                    .append(": ")
                    .append(cartasEnMesa.get(i).getNombre())
                    .append("\n");
        }

        msg.append("\nGana la baza:\n")
                .append(ganador.getNombreUsuario())
                .append(" con ")
                .append(cartaGanadora.getNombre());

        new AlertDialog.Builder(this)
                .setTitle("Resultado de la baza")
                .setMessage(msg.toString())
                .setCancelable(false)
                .setPositiveButton("Siguiente", (d, w) -> {

                    cartasEnMesa.clear();
                    jugadoresEnMesa.clear();
                    layoutMesa.removeAllViews();

                    if (bazasJugadas >= TOTAL_BAZAS) {
                        faseActual = Fase.FIN_RONDA;
                        mostrarDialogoFinRonda();
                        return;
                    }

                    indiceJugadorActual = jugadores.indexOf(ganador);
                    mostrarJugadorActual();
                })
                .show();
    }

    private void mostrarDialogoFinRonda() {

        StringBuilder resumen = new StringBuilder("FIN DE LA RONDA\n\n");

        for (Jugador j : jugadores) {
            int diferencia = Math.abs(j.getPrediccion() - j.getBazasGanadas());
            j.setVidas(j.getVidas() - diferencia);

            resumen.append(j.getNombreUsuario())
                    .append(" ha ganado ")
                    .append(j.getBazasGanadas())
                    .append(" baza(s)\n")
                    .append("Predijo: ")
                    .append(j.getPrediccion())
                    .append(" → pierde ")
                    .append(diferencia)
                    .append(" vida(s)\n\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("Resultado final")
                .setMessage(resumen.toString())
                .setCancelable(false)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    tvTurno.setText("Ronda finalizada");
                })
                .show();
    }
}
