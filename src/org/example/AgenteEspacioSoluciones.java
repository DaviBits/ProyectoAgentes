package org.example;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import org.example.Logica.EspacioCompacto;
import org.example.Logica.Tablero;

import java.io.IOException;
import java.nio.file.Path;

public class AgenteEspacioSoluciones extends Agent {

    private static final int MUESTRA = 1;
    private static final String ARCHIVO = "espacio_soluciones.bin";

    // Nombres locales de los tres agentes jugadores (uno por heurística)
    private static final String[] JUGADORES = {"hamming", "euclidiana", "desubicadas"};

    private EspacioCompacto espacio;

    @Override
    protected void setup() {
        System.out.println("Agente Espacio de Soluciones listo y esperando estado inicial...");

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.INFORM) {
                        try {
                            Tablero inicial = Tablero.fromString(msg.getContent());
                            System.out.println("Estado recibido. Generando espacio de soluciones...");

                            procesarEspacio(inicial);
                            enviarAlGUI();
                            enviarAJugadores(inicial);
                        } catch (Exception ex) {
                            System.err.println("Error al procesar el estado recibido: " + ex.getMessage());
                        }
                    }
                } else {
                    block();
                }
            }
        });
    }

    private void procesarEspacio(Tablero inicial) {
        long t0 = System.currentTimeMillis();

        espacio = new EspacioCompacto();
        espacio.generar(inicial);

        System.out.println("Espacio generado. Total estados: " + espacio.getTotal()
                + ", profundidad máxima: " + espacio.getMaxProfundidad()
                + " (" + (System.currentTimeMillis() - t0) + " ms)");

        try {
            espacio.guardar(Path.of(ARCHIVO));
            System.out.println("Espacio guardado en " + ARCHIVO);
        } catch (IOException ex) {
            System.err.println("No se pudo guardar el espacio: " + ex.getMessage());
        }
    }

    /** Manda el mismo estado inicial a los tres jugadores para que resuelvan el mismo problema. */
    private void enviarAJugadores(Tablero inicial) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        for (String j : JUGADORES) msg.addReceiver(new AID(j, AID.ISLOCALNAME));
        msg.setContent(inicial.getEstadoString());
        send(msg);
    }

    /** Envía al GUI el total, la profundidad máxima y una muestra de estados. */
    private void enviarAlGUI() {
        StringBuilder sb = new StringBuilder();
        sb.append(espacio.getTotal()).append("|")
                .append(espacio.getMaxProfundidad()).append("|");

        int muestra = Math.min(MUESTRA, espacio.getTotal());
        for (int i = 0; i < muestra; i++) {
            sb.append(espacio.estadoEnPosicion(i).getEstadoString()).append(";");
        }

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID("gui", AID.ISLOCALNAME));
        msg.setContent(sb.toString());
        send(msg);
    }
}