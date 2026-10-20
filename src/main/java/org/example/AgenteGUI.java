package org.example;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import org.example.Logica.Tablero;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Font;

/**Clase de ejemplo*/

public class AgenteGUI extends Agent {

    private JFrame frame;
    private JTextArea textArea;
    private JButton btnIniciar;



    @Override
    protected void setup() {
        initGUI();

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.INFORM) {
                        mostrarResumen(msg.getContent());
                    }
                } else {
                    block();
                }
            }
        });
    }

    /** Contenido esperado: total|profundidadMax|estado;estado;... */
    private void mostrarResumen(String contenido) {
        try {
            String[] partes = contenido.split("\\|", 3);
            String total = partes[0];
            String profMax = partes.length > 1 ? partes[1] : "?";
            String[] estados = partes.length > 2 ? partes[2].split(";") : new String[0];

            StringBuilder sb = new StringBuilder();
            sb.append("\n--- Datos Recibidos del Espacio de Soluciones ---\n");
            sb.append("Total de estados en el espacio: ").append(total).append("\n");
            sb.append("Profundidad máxima: ").append(profMax).append("\n");
            sb.append("Muestra de estados:\n");

            int n = 0;
            for (String s : estados) {
                if (s.isEmpty()) continue;
                Tablero t = Tablero.fromString(s);
                sb.append("Estado ").append(n++).append(":\n").append(t).append("\n");
            }
            appendTextArea(sb.toString());

            SwingUtilities.invokeLater(() -> btnIniciar.setEnabled(true));
        } catch (Exception ex) {
            appendTextArea("Error al leer el mensaje recibido: " + ex.getMessage() + "\n");
        }
    }

    private void initGUI() {
        frame = new JFrame("GUI 8-Puzzle SMA");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 600);
        frame.setLayout(new BorderLayout());

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);

        btnIniciar = new JButton("Generar Estado y Espacio");
        btnIniciar.addActionListener(e -> {
            appendTextArea("Solicitando generación de estado inicial...\n");
            btnIniciar.setEnabled(false); // evita lanzar dos generaciones a la vez

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