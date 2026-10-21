package org.example;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import org.example.Logica.Tablero;
import java.util.*;

public class AgenteEstadoInicial extends Agent {

    @Override
    protected void setup() {
        System.out.println("Agente Estado Inicial iniciado.");

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    Tablero tableroValido = Tablero.aleatorioSolucionable();

                    System.out.println("ESTADO INICIAL GENERADO:\n" + tableroValido.toString());

                    ACLMessage reply = new ACLMessage();
                    reply.addReceiver(new jade.core.AID("espacio", jade.core.AID.ISLOCALNAME));
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent(tableroValido.getEstadoString());
                    send(reply);

                    System.out.println("Estado enviado al AgenteEspacioSoluciones...");
                } else {
                    block();
                }
            }
        });
    }

    private Tablero generarTableroSolucionable() {
        Random random = new Random();
        int[] estado = new int[9];
        boolean solucionable = false;
        while (!solucionable) {
            List<Integer> numeros = new ArrayList<>();
            for (int i = 0; i < 9; i++) numeros.add(i);
            Collections.shuffle(numeros);
            for (int i = 0; i < 9; i++) estado[i] = numeros.get(i);
            if (verificarSolucionabilidad(estado)) solucionable = true;
        }
        return new Tablero(estado);
    }

    private boolean verificarSolucionabilidad(int[] estado) {
        int inversiones = 0;
        for (int i = 0; i < estado.length; i++) {
            if (estado[i] == 0) continue;
            for (int j = i + 1; j < estado.length; j++) {
                if (estado[j] != 0 && estado[j] < estado[i]) inversiones++;
            }
        }
        return inversiones % 2 == 0;
    }
}
