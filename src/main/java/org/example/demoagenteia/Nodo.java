package org.example.demoagenteia;

import org.example.demoagenteia.Logica.Tablero;

public class Nodo implements Comparable<Nodo> {
    public final Tablero estado;
    public final Nodo padre;   // para reconstruir el camino
    public final double h;     // valor de la heurística

    public Nodo(Tablero estado, Nodo padre, double h) {
        this.estado = estado;
        this.padre = padre;
        this.h = h;
    }

    @Override
    public int compareTo(Nodo otro) {
        return Double.compare(this.h, otro.h);
    }
}