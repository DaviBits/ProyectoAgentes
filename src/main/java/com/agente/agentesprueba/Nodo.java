package com.agente.agentesprueba;

public class Nodo implements Comparable<Nodo> {
    public final int id;        // Nodo actual en la matriz
    public final Nodo padre;    // Nodo anterior para reconstruir la ruta
    public final double h;      // Heurística

    public Nodo(int id, Nodo padre, double h) {
        this.id = id;
        this.padre = padre;
        this.h = h;
    }

    @Override
    public int compareTo(Nodo otro) {
        return Double.compare(this.h, otro.h);
    }
}