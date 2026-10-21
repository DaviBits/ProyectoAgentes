package org.example;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.ContainerController;

/** Levanta JADE y crea los agentes. Basta con ejecutar este main. */
public class Main {
    public static void main(String[] args) throws Exception {
        Runtime rt = Runtime.instance();
        ContainerController cc = rt.createMainContainer(new ProfileImpl());

        cc.createNewAgent("estado", AgenteEstadoInicial.class.getName(), null).start();
        cc.createNewAgent("espacio", AgenteEspacioSoluciones.class.getName(), null).start();

        // Tres jugadores, misma búsqueda, distinta heurística
        cc.createNewAgent("hamming", AgenteJugador.class.getName(), new Object[]{"Hamming"}).start();
        cc.createNewAgent("euclidiana", AgenteJugador.class.getName(), new Object[]{"Euclidiana"}).start();
        cc.createNewAgent("desubicadas", AgenteJugador.class.getName(), new Object[]{"Desubicadas"}).start();

        cc.createNewAgent("gui", AgenteGUI.class.getName(), null).start();
    }
}
