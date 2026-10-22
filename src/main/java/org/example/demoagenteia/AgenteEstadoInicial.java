package org.example.demoagenteia;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import org.example.demoagenteia.Logica.Tablero;

public class AgenteEstadoInicial extends Agent {

    @Override
    protected void setup() {
        System.out.println("Agente Estado Inicial iniciado y listo.");

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    System.out.println("AgenteEstadoInicial: Solicitud recibida. Generando tablero...");
                    Tablero tableroValido = Tablero.aleatorioSolucionable();

                    System.out.println("ESTADO INICIAL GENERADO:\n" + tableroValido.toString());

                    ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
                    reply.addReceiver(new jade.core.AID("espacio", jade.core.AID.ISLOCALNAME));
                    reply.setContent(tableroValido.getEstadoString());
                    send(reply);

                    System.out.println("Estado enviado al AgenteEspacioSoluciones...");
                } else {
                    block();
                }
            }
        });
    }
}