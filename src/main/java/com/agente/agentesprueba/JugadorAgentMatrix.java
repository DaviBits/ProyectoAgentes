package com.agente.agentesprueba;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.*;

public class JugadorAgentMatrix extends Agent {

    // Matriz de adyacencia del grafo
    private final int[][] matrizAdyacencia = {
            { 0, 1, 1, 0, 0 },
            { 0, 0, 0, 1, 0 },
            { 0, 0, 0, 1, 1 },
            { 0, 0, 0, 0, 1 },
            { 0, 0, 0, 0, 0 }
    };

    /**
     *
     * Retorna 1.0 constante por ahora.
     */
    private double calcularHeuristica(int nodo, int meta) {
        return 1.0;
    }

    public List<Integer> ejecutarGreedyBFS(int inicio, int meta) {
        PriorityQueue<Nodo> frontera = new PriorityQueue<>();
        Set<Integer> visitados = new HashSet<>();

        frontera.add(new Nodo(inicio, null, calcularHeuristica(inicio, meta)));

        while (!frontera.isEmpty()) {
            Nodo actual = frontera.poll();

            if (actual.id == meta) {
                return reconstruirCamino(actual);
            }

            visitados.add(actual.id);

            // Recorrer los vecinos en la matriz de adyacencia
            for (int vecino = 0; vecino < matrizAdyacencia[actual.id].length; vecino++) {
                if (matrizAdyacencia[actual.id][vecino] != 0 && !visitados.contains(vecino)) {
                    double h = calcularHeuristica(vecino, meta);
                    frontera.add(new Nodo(vecino, actual, h));
                }
            }
        }

        return Collections.emptyList();
    }

    private List<Integer> reconstruirCamino(Nodo nodoMeta) {
        List<Integer> camino = new LinkedList<>();
        Nodo actual = nodoMeta;
        while (actual != null) {
            camino.add(0, actual.id);
            actual = actual.padre;
        }
        return camino;
    }

    @Override
    protected void setup() {
        System.out.println("Agente Jugador iniciado: " + getLocalName());

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                ACLMessage msg = myAgent.receive(mt);

                if (msg != null) {
                    if ("iniciar".equalsIgnoreCase(msg.getContent().trim())) {
                        int inicio = 0;
                        int meta = 4;

                        List<Integer> camino = ejecutarGreedyBFS(inicio, meta);

                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("Ruta encontrada: " + camino.toString());
                        myAgent.send(reply);
                    }
                } else {
                    block();
                }
            }
        });
    }
}