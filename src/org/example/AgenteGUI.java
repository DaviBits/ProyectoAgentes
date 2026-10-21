package org.example;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import org.example.Logica.Tablero;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

public class AgenteGUI extends Agent {

    private JFrame frame;
    private JTextArea textArea;
    private JButton btnIniciar;

    // Resultados de los jugadores de la ronda actual
    private final List<String[]> resultados = new ArrayList<>();

    @Override
    protected void setup() {
        initGUI();

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.INFORM) {
                        if ("espacio".equals(msg.getSender().getLocalName())) {
                            mostrarResumen(msg.getContent());
                        } else {
                            mostrarResultado(msg.getContent());
                        }
                    }
                } else {
                    block();
                }
            }
        });
    }

    /** Contenido esperado: total|profundidadMax|estadoInicial; */
    private void mostrarResumen(String contenido) {
        try {
            String[] partes = contenido.split("\\|", 3);
            Tablero inicial = Tablero.fromString(partes[2].replace(";", ""));

            resultados.clear();
            appendTextArea("\n--- Espacio de soluciones ---\n"
                    + "Total de estados: " + partes[0] + "\n"
                    + "Profundidad máxima: " + partes[1] + "\n"
                    + "Estado inicial (el mismo para los 3 agentes):\n" + inicial + "\n"
                    + "Esperando a los jugadores...\n");
        } catch (Exception ex) {
            appendTextArea("Error al leer el resumen: " + ex.getMessage() + "\n");
        }
    }

    /** Contenido esperado: heuristica|resuelto|pasos|expandidos|ms|movimientos */
    private void mostrarResultado(String contenido) {
        try {
            String[] p = contenido.split("\\|", 6);
            String mov = p.length > 5 ? p[5] : "";
            if (mov.length() > 60) mov = mov.substring(0, 60) + "...";

            appendTextArea("[" + p[0] + "] resuelto=" + p[1] + ", pasos=" + p[2]
                    + ", nodos expandidos=" + p[3] + ", tiempo=" + p[4] + " ms\n"
                    + "   movimientos: " + mov + "\n");

            resultados.add(p);
            if (resultados.size() == 3) {
                String[] mejor = resultados.get(0);
                for (String[] r : resultados) {
                    if (Integer.parseInt(r[3]) < Integer.parseInt(mejor[3])) mejor = r;
                }
                appendTextArea("\nMenos nodos expandidos: " + mejor[0] + " (" + mejor[3] + ")\n");
                SwingUtilities.invokeLater(() -> btnIniciar.setEnabled(true));
            }
        } catch (Exception ex) {
            appendTextArea("Error al leer el resultado: " + ex.getMessage() + "\n");
        }
    }

    private void initGUI() {
        frame = new JFrame("8-Puzzle: 3 agentes, 3 heurísticas");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 600);
        frame.setLayout(new BorderLayout());

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);

        btnIniciar = new JButton("Generar estado y resolver");
        btnIniciar.addActionListener(e -> {
            appendTextArea("Solicitando estado inicial...\n");
            btnIniciar.setEnabled(false);

            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(new AID("estado", AID.ISLOCALNAME));
            msg.setContent("generar");
            send(msg);
        });

        frame.add(btnIniciar, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private void appendTextArea(String text) {
        SwingUtilities.invokeLater(() -> {
            textArea.append(text);
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }

    @Override
    protected void takeDown() {
        if (frame != null) {
            SwingUtilities.invokeLater(() -> frame.dispose());
        }
    }
}
