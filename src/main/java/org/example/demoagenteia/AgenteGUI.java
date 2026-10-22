package org.example.demoagenteia;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import javafx.application.Application;
import javafx.application.Platform;
import org.example.demoagenteia.Logica.Tablero;

import java.util.ArrayList;
import java.util.List;

public class AgenteGUI extends Agent {

    private final List<String[]> resultados = new ArrayList<>();

    @Override
    protected void setup() {
        // Lanzar la aplicación JavaFX en un hilo independiente
        new Thread(() -> {
            try {
                Application.launch(Interfaz8PuzzleFX.class);
            } catch (Exception ignored) {}
        }).start();

        // Intentar conectar las acciones hasta que la instancia de JavaFX esté lista
        new Thread(() -> {
            while (Interfaz8PuzzleFX.getInstancia() == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
            }
            Platform.runLater(() -> {
                Interfaz8PuzzleFX.getInstancia().setAcciones(
                        this::solicitarEstadoInicial,
                        this::enviarOrdenResolver
                );
                System.out.println("GUI: Conexión con Interfaz8PuzzleFX establecida correctamente.");
            });
        }).start();

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.INFORM) {
                        if ("espacio".equals(msg.getSender().getLocalName())) {
                            procesarEspacio(msg.getContent());
                        } else {
                            procesarResultado(msg.getContent());
                        }
                    }
                } else {
                    block();
                }
            }
        });
    }

    private void solicitarEstadoInicial() {
        resultados.clear();
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(new AID("estado", AID.ISLOCALNAME));
        msg.setContent("generar");
        send(msg);
        System.out.println("GUI: Solicitando estado inicial al AgenteEstadoInicial...");
    }

    private void enviarOrdenResolver() {
        resultados.clear();
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(new AID("espacio", AID.ISLOCALNAME));
        msg.setContent("resolver");
        send(msg);
        System.out.println("GUI: Enviando orden de resolver al AgenteEspacioSoluciones...");
    }

    private void procesarEspacio(String contenido) {
        try {
            String[] partes = contenido.split("\\|", 3);
            Tablero inicial = Tablero.fromString(partes[2].replace(";", ""));

            Platform.runLater(() -> {
                Interfaz8PuzzleFX gui = Interfaz8PuzzleFX.getInstancia();
                if (gui != null) {
                    gui.actualizarEstadoInicial(inicial, partes[0], partes[1]);
                }
            });
        } catch (Exception ex) {
            System.err.println("Error procesando espacio en GUI: " + ex.getMessage());
        }
    }

    private void procesarResultado(String contenido) {
        try {
            String[] p = contenido.split("\\|", 6);
            String heuristica = p[0];
            boolean resuelto = Boolean.parseBoolean(p[1]);
            int pasos = Integer.parseInt(p[2]);
            int expandidos = Integer.parseInt(p[3]);
            double ms = Double.parseDouble(p[4]);   // antes: long ms = Long.parseLong(p[4]);

            String movs = p.length > 5 ? p[5] : "";

            resultados.add(p);

            Platform.runLater(() -> {
                Interfaz8PuzzleFX gui = Interfaz8PuzzleFX.getInstancia();
                if (gui != null) {
                    gui.agregarResultadoAgente(heuristica, resuelto, pasos, expandidos, ms, movs);

                    if (resultados.size() == 3) {
                        gui.competenciaFinalizada();
                    }
                }
            });
        } catch (Exception ex) {
            System.err.println("Error procesando resultado en GUI: " + ex.getMessage());
        }
    }
}