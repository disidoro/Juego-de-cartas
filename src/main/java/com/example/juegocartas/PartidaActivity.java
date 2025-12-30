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
    private TextView tvTurno, tvObjetivo, tvGanadas, tvRestantes, tvVidas;
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

    // Rondas: 5 → 4 → 3 → 2
    private int bazasRondaActual = TOTAL_BAZAS;

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
        tvVidas = findViewById(R.id.tvVidas);

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

    // ----------- REPARTIR CARTAS -----------
    private void repartirCartas() {
        baraja = crearBaraja();
        bazasJugadas = 0;
        sumaPredicciones = 0;

        cartasEnMesa.clear();
        jugadoresEnMesa.clear();

        for (Jugador j : jugadores) {
            j.getCartas().clear();
            j.setBazasGanadas(0);
            j.setPrediccion(0);

            for (int i = 0; i < bazasRondaActual; i++) {
                j.getCartas().add(baraja.remove(0));
            }
        }
    }

    // ----------- MOSTRAR JUGADOR -----------
    private void mostrarJugadorActual() {
        if (faseActual == Fase.FIN_RONDA) return;

        Jugador j = jugadores.get(indiceJugadorActual);

        tvTurno.setText("Turno de: " + j.getNombreUsuario()
                + " | Ronda de " + bazasRondaActual + " bazas");

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
        tvVidas.setText("Vidas: " + j.getVidas());
    }

    // ----------- PREDICCIÓN -----------
    private void confirmarPrediccion() {
        String txt = edtPrediccion.getText().toString().trim();
        if (txt.isEmpty()) {
            edtPrediccion.setError("Introduce un número");
            return;
        }

        int pred = Integer.parseInt(txt);

        if (pred < 0 || pred > bazasRondaActual) {
            edtPrediccion.setError("Número no válido");
            return;
        }

        boolean ultimo = indiceJugadorActual == jugadores.size() - 1;
        if (ultimo && sumaPredicciones + pred == bazasRondaActual) {
            edtPrediccion.setError("La suma no puede ser " + bazasRondaActual);
            return;
        }

        Jugador jugador = jugadores.get(indiceJugadorActual);
        jugador.setPrediccion(pred);
        sumaPredicciones += pred;

        TextView tvPred = new TextView(this);
        tvPred.setText(jugador.getNombreUsuario() + " predice " + pred + " baza(s)");
        tvPred.setPadding(16, 16, 16, 16);
        layoutMesa.addView(tvPred);

        indiceJugadorActual++;

        if (indiceJugadorActual >= jugadores.size()) {
            mostrarDialogoResumenPredicciones();
        } else {
            mostrarJugadorActual();
        }
    }

    // ----------- RESUMEN PREDICCIONES -----------
    private void mostrarDialogoResumenPredicciones() {

        StringBuilder resumen = new StringBuilder("Resumen de predicciones:\n\n");

        for (Jugador j : jugadores) {
            resumen.append(j.getNombreUsuario())
                    .append(" predijo ")
                    .append(j.getPrediccion())
                    .append(" baza(s)\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("Predicciones")
                .setMessage(resumen.toString())
                .setCancelable(false)
                .setPositiveButton("Empezar ronda", (dialog, which) -> {
                    faseActual = Fase.JUGAR_BAZA;
                    indiceJugadorActual = 0;
                    btnConfirmar.setEnabled(false);
                    edtPrediccion.setEnabled(false);
                    layoutMesa.removeAllViews();
                    mostrarJugadorActual();
                })
                .show();
    }

    // ----------- JUGAR CARTA -----------
    private void jugarCarta(Carta carta) {
        if (faseActual != Fase.JUGAR_BAZA) return;

        Jugador jugador = jugadores.get(indiceJugadorActual);
        jugador.jugarCarta(carta);

        cartasEnMesa.add(carta);
        jugadoresEnMesa.add(jugador);

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

        cartasEnMesa.clear();
        jugadoresEnMesa.clear();

        if (bazasJugadas >= bazasRondaActual) {
            faseActual = Fase.FIN_RONDA;
            mostrarDialogoFinRonda();
        } else {
            indiceJugadorActual = jugadores.indexOf(ganador);
            mostrarJugadorActual();
        }
    }

    private int fuerzaCarta(Carta c) {
        return c.getValor() == 1 ? 100 : c.getValor();
    }

    // ----------- FIN DE RONDA -----------
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
                    .append(" vida(s)\n")
                    .append("Vidas restantes: ")
                    .append(j.getVidas())
                    .append("\n\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("Resultado final")
                .setMessage(resumen.toString())
                .setCancelable(false)
                .setPositiveButton("Aceptar", (dialog, which) -> {

                    bazasRondaActual--;

                    if (bazasRondaActual < 2) {
                        tvTurno.setText("Fin de la partida");
                        Toast.makeText(this, "La partida ha terminado", Toast.LENGTH_LONG).show();
                        return;
                    }

                    faseActual = Fase.PREDICCION;
                    indiceJugadorActual = 0;
                    edtPrediccion.setEnabled(true);
                    btnConfirmar.setEnabled(true);
                    edtPrediccion.setText("");

                    repartirCartas();
                    mostrarJugadorActual();
                })
                .show();
    }
}
