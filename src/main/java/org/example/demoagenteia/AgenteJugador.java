package org.example.demoagenteia;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.example.demoagenteia.Heuristicas.Euclidiana;
import org.example.demoagenteia.Heuristicas.FichasDesubicadas;
import org.example.demoagenteia.Heuristicas.Hamming;
import org.example.demoagenteia.Heuristicas.Heuristica;
import org.example.demoagenteia.Logica.Tablero;

import java.util.*;

/**
 * Jugador que resuelve el 8-puzzle con Greedy Best-First Search.
 * La heurística se elige con el argumento del agente: Hamming, Euclidiana o Desubicadas.
 */
public class AgenteJugador extends Agent {

    private Heuristica heuristica;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        String tipo = (args != null && args.length > 0) ? args[0].toString() : "Hamming";
        switch (tipo.toLowerCase()) {
            case "euclidiana": heuristica = new Euclidiana(); break;
            case "desubicadas": heuristica = new FichasDesubicadas(); break;
            default: heuristica = new Hamming();
        }
        System.out.println("Jugador " + getLocalName() + " listo con heurística " + heuristica.nombre());

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
                if (msg == null) {
                    block();
                    return;
                }
                Tablero inicial = Tablero.fromString(msg.getContent());

                long t0 = System.nanoTime();
                int[] expandidos = new int[1];
                List<Tablero> camino = greedyBFS(inicial, expandidos);
                double ms = (System.nanoTime() - t0) / 1_000_000.0;

// Formato: heuristica|resuelto|pasos|expandidos|ms|movimientos
                String contenido = heuristica.nombre() + "|" + !camino.isEmpty() + "|"
                        + Math.max(0, camino.size() - 1) + "|" + expandidos[0] + "|"
                        + String.format(Locale.US, "%.3f", ms) + "|"
                        + movimientos(camino);

                ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
                reply.addReceiver(new AID("gui", AID.ISLOCALNAME));
                reply.setContent(contenido);
                myAgent.send(reply);
            }
        });
    }

    private double h(Tablero t) {
        return heuristica.calcular(t.getEstado());
    }

    /** Greedy BFS: siempre expande el nodo con menor h. Devuelve el camino (vacío si no hay). */
    private List<Tablero> greedyBFS(Tablero inicio, int[] expandidos) {
        PriorityQueue<Nodo> frontera = new PriorityQueue<>();
        Set<Tablero> visitados = new HashSet<>();
        frontera.add(new Nodo(inicio, null, h(inicio)));

        while (!frontera.isEmpty()) {
            Nodo actual = frontera.poll();
            if (actual.estado.esMeta()) return reconstruirCamino(actual);

            if (!visitados.add(actual.estado)) continue; // ya expandido
            expandidos[0]++;

            for (Tablero vecino : actual.estado.getVecinos()) {
                if (!visitados.contains(vecino)) {
                    frontera.add(new Nodo(vecino, actual, h(vecino)));
                }
            }
        }
        return Collections.emptyList();
    }

    private List<Tablero> reconstruirCamino(Nodo meta) {
        LinkedList<Tablero> camino = new LinkedList<>();
        for (Nodo n = meta; n != null; n = n.padre) camino.addFirst(n.estado);
        return camino;
    }

    /** Movimientos del hueco: U, D, L, R. */
    private String movimientos(List<Tablero> camino) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < camino.size(); i++) {
            int d = camino.get(i).getPosVacio() - camino.get(i - 1).getPosVacio();
            sb.append(d == -3 ? 'U' : d == 3 ? 'D' : d == -1 ? 'L' : 'R');
        }
        return sb.toString();
    }
}