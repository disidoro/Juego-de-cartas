package com.example.juegocartas;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Jugador implements Serializable {

    // 🔑 Datos de usuario (login futuro)
    private String nombreUsuario;
    private String contraseña;

    // 🎯 Predicción de bazas (0–5)
    private int prediccion;

    // ❤️ Vidas del jugador
    private int vidas;

    // 🏆 Bazas ganadas en la partida actual
    private int bazasGanadas;

    // 🃏 Cartas en la mano
    private List<Carta> cartas;

    // ---------------- CONSTRUCTOR ----------------
    public Jugador(String nombreUsuario, String contraseña) {
        this.nombreUsuario = nombreUsuario;
        this.contraseña = contraseña;
        this.vidas = 3;
        this.prediccion = 0;
        this.bazasGanadas = 0;
        this.cartas = new ArrayList<>();
    }

    // ---------------- MÉTODOS ----------------

    public void añadirCarta(Carta carta) {
        cartas.add(carta);
    }

    public boolean jugarCarta(Carta carta) {
        return cartas.remove(carta);
    }

    public void reiniciarParaNuevaPartida() {
        prediccion = 0;
        bazasGanadas = 0;
        cartas.clear();
    }

    // ---------------- GETTERS Y SETTERS ----------------

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getContraseña() {
        return contraseña;
    }

    public int getPrediccion() {
        return prediccion;
    }

    public void setPrediccion(int prediccion) {
        this.prediccion = prediccion;
    }

    public int getVidas() {
        return vidas;
    }

    public void setVidas(int vidas) {
        this.vidas = vidas;
    }

    public int getBazasGanadas() {
        return bazasGanadas;
    }

    public void setBazasGanadas(int bazasGanadas) {
        this.bazasGanadas = bazasGanadas;
    }

    public List<Carta> getCartas() {
        return cartas;
    }
}
