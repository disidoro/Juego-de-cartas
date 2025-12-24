package com.example.juegocartas;

import java.io.Serializable;

public class Carta implements Serializable {

    // 🔢 Valor de la carta (1–12)
    private int valor;

    // 🏷️ Nombre de la carta (Ej: "As de Bastos")
    private String nombre;

    // 🖼️ Imagen de la carta (drawable, para el futuro)
    private int imagenResId;

    // ---------------- CONSTRUCTORES ----------------

    // Constructor SIN imagen (fase actual)
    public Carta(int valor, String nombre) {
        this.valor = valor;
        this.nombre = nombre;
        this.imagenResId = 0; // sin imagen por ahora
    }

    // Constructor CON imagen (fase futura)
    public Carta(int valor, String nombre, int imagenResId) {
        this.valor = valor;
        this.nombre = nombre;
        this.imagenResId = imagenResId;
    }

    // ---------------- GETTERS ----------------

    public int getValor() {
        return valor;
    }

    public String getNombre() {
        return nombre;
    }

    public int getImagenResId() {
        return imagenResId;
    }

    // ---------------- UTILIDAD ----------------

    @Override
    public String toString() {
        return nombre + " (" + valor + ")";
    }
}
