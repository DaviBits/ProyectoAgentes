package org.example.demoagenteia.Logica;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class Tablero {

    public static final int TAMANO = 3;
    public static final int CELDAS = TAMANO * TAMANO;

    // Arriba, abajo, izquierda, derecha (movimientos del hueco)
    private static final int[][] MOVIMIENTOS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    private final int[] estado;
    private final int posVacio;
    private final int hash;

    /** Constructor público: valida y copia el arreglo recibido. */
    public Tablero(int[] estado) {
        validar(estado);
        this.estado = estado.clone();
        this.posVacio = buscarVacio(this.estado);
        this.hash = Arrays.hashCode(this.estado);
    }

    /** Constructor interno: el arreglo ya es válido y nadie más lo referencia. */
    private Tablero(int[] estadoPropio, int posVacio) {
        this.estado = estadoPropio;
        this.posVacio = posVacio;
        this.hash = Arrays.hashCode(estadoPropio);
    }

    private static void validar(int[] estado) {
        if (estado == null || estado.length != CELDAS) {
            throw new IllegalArgumentException("El estado debe tener " + CELDAS + " elementos.");
        }
        boolean[] presentes = new boolean[CELDAS];
        for (int valor : estado) {
            if (valor < 0 || valor >= CELDAS || presentes[valor]) {
                throw new IllegalArgumentException(
                        "El estado debe ser una permutación de 0 a " + (CELDAS - 1) + ".");
            }
            presentes[valor] = true;
        }
    }

    private static int buscarVacio(int[] estado) {
        for (int i = 0; i < estado.length; i++) {
            if (estado[i] == 0) return i;
        }
        throw new IllegalStateException("El tablero no tiene casilla vacía.");
    }


    /** Devuelve una copia, para que el tablero no pueda modificarse desde fuera. */
    public int[] getEstado() {
        return estado.clone();
    }

    public int getPosVacio() {
        return posVacio;
    }


    /** Formato: "1,2,3,4,5,6,7,8,0". */
    public String getEstadoString() {
        StringBuilder sb = new StringBuilder(CELDAS * 2);
        for (int i = 0; i < CELDAS; i++) {
            if (i > 0) sb.append(',');
            sb.append(estado[i]);
        }
        return sb.toString();
    }

    public static Tablero fromString(String s) {
        String[] partes = s.trim().split(",");
        int[] valores = new int[partes.length];
        for (int i = 0; i < partes.length; i++) {
            valores[i] = Integer.parseInt(partes[i].trim());
        }
        return new Tablero(valores); // valida longitud y permutación
    }

    /** Todos los tableros alcanzables moviendo el hueco una casilla. */
    public List<Tablero> getVecinos() {
        List<Tablero> vecinos = new ArrayList<>(4);
        int fila = posVacio / TAMANO;
        int col = posVacio % TAMANO;

        for (int[] mov : MOVIMIENTOS) {
            int nf = fila + mov[0];
            int nc = col + mov[1];
            if (nf < 0 || nf >= TAMANO || nc < 0 || nc >= TAMANO) continue;

            int nuevaPos = nf * TAMANO + nc;
            int[] siguiente = estado.clone();
            siguiente[posVacio] = siguiente[nuevaPos];
            siguiente[nuevaPos] = 0;
            vecinos.add(new Tablero(siguiente, nuevaPos));
        }
        return vecinos;
    }

    /** Meta: 1 2 3 / 4 5 6 / 7 8 0 */
    public static final int[] META = {1, 2, 3, 4, 5, 6, 7, 8, 0};

    public boolean esMeta() {
        return Arrays.equals(estado, META);
    }

    /** Para tamaño impar (3x3), es solucionable si las inversiones (sin el 0) son pares. */
    public boolean esSolucionable() {
        return esSolucionable(estado);
    }

    public static boolean esSolucionable(int[] e) {
        int inversiones = 0;
        for (int i = 0; i < e.length; i++) {
            if (e[i] == 0) continue;
            for (int j = i + 1; j < e.length; j++) {
                if (e[j] != 0 && e[j] < e[i]) inversiones++;
            }
        }
        return inversiones % 2 == 0;
    }

    /** Tablero aleatorio con solución, sin reintentos. */
    public static Tablero aleatorioSolucionable() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        int[] e = new int[CELDAS];
        for (int i = 0; i < CELDAS; i++) e[i] = i;
        for (int i = CELDAS - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int tmp = e[i]; e[i] = e[j]; e[j] = tmp;
        }

        if (!esSolucionable(e)) {
            int a = -1, b = -1;
            for (int i = 0; i < CELDAS && b < 0; i++) {
                if (e[i] == 0) continue;
                if (a < 0) a = i; else b = i;
            }
            int tmp = e[a]; e[a] = e[b]; e[b] = tmp;
        }
        return new Tablero(e);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tablero)) return false;
        Tablero otro = (Tablero) o;
        return hash == otro.hash && Arrays.equals(estado, otro.estado);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(CELDAS * 2 + TAMANO);
        for (int i = 0; i < CELDAS; i++) {
            sb.append(estado[i]).append(' ');
            if ((i + 1) % TAMANO == 0) sb.append('\n');
        }
        return sb.toString();
    }
}