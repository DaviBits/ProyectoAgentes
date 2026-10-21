package org.example.Logica;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Guarda el espacio de soluciones del 8-puzzle de forma compacta.
 * Cada estado se identifica por su ranking de permutación (0..362879).
 */
public class EspacioCompacto {

    private static final int N = 9;
    private static final int TOTAL = 362880; // 9!
    private static final int[] FACT = {1, 1, 2, 6, 24, 120, 720, 5040, 40320};
    private static final int[][] MOVS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    private final byte[] profundidad = new byte[TOTAL];
    private final int[] padre = new int[TOTAL];
    private int[] orden = new int[0];
    private int raiz;
    private int maxProfundidad;

    public static int rank(int[] e) {
        int r = 0;
        for (int i = 0; i < N; i++) {
            int menores = 0;
            for (int j = i + 1; j < N; j++) if (e[j] < e[i]) menores++;
            r += menores * FACT[N - 1 - i];
        }
        return r;
    }

    public static int[] unrank(int r) {
        int[] e = new int[N];
        boolean[] usado = new boolean[N];
        for (int i = 0; i < N; i++) {
            int f = FACT[N - 1 - i];
            int k = r / f;
            r %= f;
            for (int v = 0; v < N; v++) {
                if (usado[v]) continue;
                if (k == 0) { e[i] = v; usado[v] = true; break; }
                k--;
            }
        }
        return e;
    }

    public void generar(Tablero inicial) {
        Arrays.fill(profundidad, (byte) -1);
        int[] cola = new int[TOTAL];
        int head = 0, tail = 0;

        raiz = rank(inicial.getEstado());
        profundidad[raiz] = 0;
        padre[raiz] = -1;
        cola[tail++] = raiz;
        maxProfundidad = 0;

        while (head < tail) {
            int actual = cola[head++];
            int[] e = unrank(actual);
            int pos = 0;
            while (e[pos] != 0) pos++;
            int fila = pos / 3, col = pos % 3;

            for (int[] m : MOVS) {
                int nf = fila + m[0], nc = col + m[1];
                if (nf < 0 || nf > 2 || nc < 0 || nc > 2) continue;
                int np = nf * 3 + nc;

                e[pos] = e[np]; e[np] = 0;
                int r = rank(e);
                e[np] = e[pos]; e[pos] = 0;

                if (profundidad[r] == -1) {
                    profundidad[r] = (byte) (profundidad[actual] + 1);
                    padre[r] = actual;
                    maxProfundidad = Math.max(maxProfundidad, profundidad[r]);
                    cola[tail++] = r;
                }
            }
        }
        orden = Arrays.copyOf(cola, tail);
    }

    public int getTotal() { return orden.length; }
    public int getMaxProfundidad() { return maxProfundidad; }

    public Tablero estadoEnPosicion(int i) { return new Tablero(unrank(orden[i])); }

    public int getProfundidad(Tablero t) { return profundidad[rank(t.getEstado())]; }

    /** Camino desde la raíz hasta el tablero dado (vacío si no pertenece al espacio). */
    public List<Tablero> camino(Tablero destino) {
        int r = rank(destino.getEstado());
        List<Tablero> ruta = new ArrayList<>();
        if (profundidad[r] == -1) return ruta;
        while (r != -1) {
            ruta.add(0, new Tablero(unrank(r)));
            r = padre[r];
        }
        return ruta;
    }

    public void guardar(Path archivo) throws IOException {
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(archivo)))) {
            out.writeInt(orden.length);
            for (int r : orden) {
                out.writeInt(r);
                out.writeInt(padre[r]);
                out.writeByte(profundidad[r]);
            }
        }
    }
}