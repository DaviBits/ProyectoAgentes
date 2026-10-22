package org.example.demoagenteia;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.example.demoagenteia.Logica.Tablero;

import java.util.*;

public class Interfaz8PuzzleFX extends Application {

    private static Interfaz8PuzzleFX instancia;

    private TabPane tabPane;
    private Button[] botonesCasillas = new Button[9];
    private Button[] botonesPasoAPaso = new Button[9];
    private Button btnOrdenar;

    // Pestaña 3: Métricas
    private BarChart<String, Number> chartTiempos, chartNodos;
    private XYChart.Series<String, Number> seriesTiempos;
    private XYChart.Series<String, Number> seriesNodos;
    private Label lblResumenEspacio;
    private TableView<MetricaAgente> tablaMetricas;
    private ObservableList<MetricaAgente> listaMetricas = FXCollections.observableArrayList();

    // Pestaña 4: Animaciones
    private ComboBox<String> comboAgentesAnimacion;
    private Label lblPasoInfo;
    private Button btnAnterior, btnSiguiente, btnPlayPause;
    private Timeline timelineAnimacion;

    private static class SolucionAgente {
        String heuristica;
        String movimientos;
        List<Tablero> camino = new ArrayList<>();
    }

    private final Map<String, SolucionAgente> solucionesPorAgente = new HashMap<>();
    private SolucionAgente solucionSeleccionada;
    private int pasoActual = 0;

    private Runnable accionSolicitarEstado;
    private Runnable accionResolver;
    private Tablero estadoInicialActual;

    public Interfaz8PuzzleFX() {
        instancia = this;
    }

    public static Interfaz8PuzzleFX getInstancia() { return instancia; }

    public void setAcciones(Runnable solicitarEstado, Runnable resolver) {
        this.accionSolicitarEstado = solicitarEstado;
        this.accionResolver = resolver;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("8-Puzzle Multiagente - JADE + JavaFX");

        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab tab1 = new Tab("1. Inicio", crearPestañaInicio());
        Tab tab2 = new Tab("2. Puzzle (Tablero)", crearPestañaPuzzle());
        Tab tab3 = new Tab("3. Resultados (Métricas)", crearPestañaResultados());
        Tab tab4 = new Tab("4. Animación por Agente", crearPestañaCamino());

        tabPane.getTabs().addAll(tab1, tab2, tab3, tab4);

        Scene scene = new Scene(tabPane, 720, 620);

        try {
            var resource = getClass().getResource("/estilo.css");
            if (resource != null) {
                scene.getStylesheets().add(resource.toExternalForm());
                System.out.println("CSS cargado correctamente desde resources/estilo.css");
            }
        } catch (Exception ex) {
            System.err.println("Error cargando CSS: " + ex.getMessage());
        }

        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> Platform.exit());
    }

    // =========================================================================
    // PESTAÑA 1: INICIO (Paso 1: Generar Tablero únicamente)
    // =========================================================================
    private StackPane crearPestañaInicio() {
        StackPane panel = new StackPane();

        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);

        Label lblTitulo = new Label("Sistema Multiagente 8-Puzzle");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        Button btnGenerar = new Button("Generar Estado Inicial");
        btnGenerar.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        btnGenerar.setPrefSize(280, 55);
        btnGenerar.getStyleClass().add("button-success");

        lblTitulo.getStyleClass().add("titulo");
        btnGenerar.setOnAction(e -> {
            if (accionSolicitarEstado != null) {
                accionSolicitarEstado.run();
            } else {
                mostrarAlerta("Atención", "El Agente GUI aún no se ha conectado.");
            }
        });

        box.getChildren().addAll(lblTitulo, btnGenerar);
        panel.getChildren().add(box);
        return panel;
    }

    // =========================================================================
    // PESTAÑA 2: PUZZLE (Paso 2: Mostrar Tablero y Activar Búsqueda)
    // =========================================================================
    private VBox crearPestañaPuzzle() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(20));
        panel.setAlignment(Pos.CENTER);

        GridPane gridTablero = new GridPane();
        gridTablero.setHgap(8); gridTablero.setVgap(8);
        gridTablero.setAlignment(Pos.CENTER);

        for (int i = 0; i < 9; i++) {
            Button btn = new Button();
            btn.setPrefSize(95, 95);
            botonesCasillas[i] = btn;
            gridTablero.add(btn, i % 3, i / 3);
        }

        btnOrdenar = new Button("Ordenar / Resolver con Agentes");
        btnOrdenar.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        btnOrdenar.setPrefSize(260, 45);
        btnOrdenar.setDisable(true); // Deshabilitado hasta que haya un tablero
        btnOrdenar.getStyleClass().add("button-success");

        btnOrdenar.setOnAction(e -> {
            btnOrdenar.setDisable(true);
            if (accionResolver != null) {
                accionResolver.run(); // Dispara la resolución en JADE
            }
        });

        panel.getChildren().addAll(gridTablero, btnOrdenar);
        return panel;
    }

    // =========================================================================
    // PESTAÑA 3: DASHBOARD
    // =========================================================================
    private VBox crearPestañaResultados() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setAlignment(Pos.CENTER);

        lblResumenEspacio = new Label("Esperando a que los agentes resuelvan el puzzle...");
        lblResumenEspacio.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        seriesTiempos = new XYChart.Series<>(); seriesTiempos.setName("Tiempo (ms)");
        seriesNodos = new XYChart.Series<>();   seriesNodos.setName("Nodos Expandidos");

        chartTiempos = crearGrafica("Tiempo de ejecución", "ms", seriesTiempos);
        chartNodos = crearGrafica("Nodos expandidos", "nodos", seriesNodos);
        chartNodos.getStyleClass().add("grafica-nodos");   // para darle otro color en el CSS

        HBox graficas = new HBox(10, chartTiempos, chartNodos);
        graficas.setPrefHeight(240);
        HBox.setHgrow(chartTiempos, Priority.ALWAYS);
        HBox.setHgrow(chartNodos, Priority.ALWAYS);


        tablaMetricas = new TableView<>();
        tablaMetricas.setPrefHeight(170);

        TableColumn<MetricaAgente, String> colH = new TableColumn<>("Heurística");
        colH.setCellValueFactory(d -> d.getValue().heuristicaProperty());

        TableColumn<MetricaAgente, String> colT = new TableColumn<>("Tiempo (ms)");
        colT.setCellValueFactory(d -> d.getValue().tiempoProperty());

        TableColumn<MetricaAgente, String> colN = new TableColumn<>("Nodos Expandidos");
        colN.setCellValueFactory(d -> d.getValue().nodosProperty());

        TableColumn<MetricaAgente, String> colP = new TableColumn<>("Pasos (Largo)");
        colP.setCellValueFactory(d -> d.getValue().pasosProperty());

        tablaMetricas.getColumns().addAll(colH, colT, colN, colP);
        tablaMetricas.setItems(listaMetricas);

        panel.getChildren().addAll(lblResumenEspacio, graficas, tablaMetricas);
        return panel;
    }

    // =========================================================================
    // PESTAÑA 4: ANIMACIÓN POR AGENTE
    // =========================================================================
    private VBox crearPestañaCamino() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(15));
        panel.setAlignment(Pos.CENTER);

        HBox selectorBox = new HBox(10);
        selectorBox.setAlignment(Pos.CENTER);
        Label lblSelect = new Label("Ver animación del Agente:");

        comboAgentesAnimacion = new ComboBox<>();
        comboAgentesAnimacion.setOnAction(e -> cambiarAgenteAnimacion());
        selectorBox.getChildren().addAll(lblSelect, comboAgentesAnimacion);

        lblPasoInfo = new Label("Seleccione un agente para reproducir.");
        lblPasoInfo.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lblPasoInfo.getStyleClass().add("subtitulo");

        GridPane gridPaso = new GridPane();
        gridPaso.setHgap(6); gridPaso.setVgap(6);
        gridPaso.setAlignment(Pos.CENTER);

        for (int i = 0; i < 9; i++) {
            Button btn = new Button();
            btn.setPrefSize(75, 75);
            botonesPasoAPaso[i] = btn;
            gridPaso.add(btn, i % 3, i / 3);
        }

        HBox controles = new HBox(12);
        controles.setAlignment(Pos.CENTER);
        btnAnterior = new Button("◄ Anterior");
        btnPlayPause = new Button("▶ Reproducir");
        btnSiguiente = new Button("Siguiente ►");

        btnAnterior.setOnAction(e -> mostrarPaso(pasoActual - 1));
        btnSiguiente.setOnAction(e -> mostrarPaso(pasoActual + 1));
        btnPlayPause.setOnAction(e -> toggleAnimacion());

        controles.getChildren().addAll(btnAnterior, btnPlayPause, btnSiguiente);
        panel.getChildren().addAll(selectorBox, lblPasoInfo, gridPaso, controles);
        return panel;
    }

    public void actualizarEstadoInicial(Tablero inicial, String totalEstados, String profMax) {
        Platform.runLater(() -> {
            this.estadoInicialActual = inicial;
            actualizarTableroVisual(inicial.getEstado(), botonesCasillas);

            seriesTiempos.getData().clear();
            seriesNodos.getData().clear();
            listaMetricas.clear();
            solucionesPorAgente.clear();
            comboAgentesAnimacion.getItems().clear();

            lblResumenEspacio.setText("Total espacio alcanzable: " + totalEstados + " | Profundidad Máxima: " + profMax);
            btnOrdenar.setDisable(false); // Se habilita el botón en pestaña 2
            tabPane.getSelectionModel().select(1); // Cambia a Pestaña 2 y se queda esperando al usuario
        });
    }

    public void agregarResultadoAgente(String heuristica, boolean resuelto, int pasos,
                                       int expandidos, double ms, String movimientos) {
        Platform.runLater(() -> {
            seriesTiempos.getData().add(new XYChart.Data<>(heuristica, ms));
            seriesNodos.getData().add(new XYChart.Data<>(heuristica, expandidos));
            listaMetricas.add(new MetricaAgente(heuristica,
                    String.format(Locale.US, "%.3f", ms),
                    String.valueOf(expandidos), String.valueOf(pasos), movimientos));
            if (resuelto) {
                SolucionAgente sol = new SolucionAgente();
                sol.heuristica = heuristica;
                sol.movimientos = movimientos;
                sol.camino = reconstruirCaminoTableros(movimientos);
                solucionesPorAgente.put(heuristica, sol);

                comboAgentesAnimacion.getItems().add(heuristica);
                if (comboAgentesAnimacion.getValue() == null) {
                    comboAgentesAnimacion.setValue(heuristica);
                }
            }
        });
    }

    public void competenciaFinalizada() {
        Platform.runLater(() -> tabPane.getSelectionModel().select(2)); // Al recibir las 3 respuestas, salta a la pestaña 3
    }

    private void cambiarAgenteAnimacion() {
        String sel = comboAgentesAnimacion.getValue();
        if (sel != null && solucionesPorAgente.containsKey(sel)) {
            solucionSeleccionada = solucionesPorAgente.get(sel);
            pasoActual = 0;
            mostrarPaso(0);
        }
    }

    private List<Tablero> reconstruirCaminoTableros(String movs) {
        List<Tablero> camino = new ArrayList<>();
        if (estadoInicialActual == null) return camino;

        camino.add(estadoInicialActual);
        Tablero actual = estadoInicialActual;

        for (char m : movs.toCharArray()) {
            int posVacio = actual.getPosVacio();
            int r = posVacio / 3, c = posVacio % 3;
            if (m == 'U') r--; else if (m == 'D') r++; else if (m == 'L') c--; else if (m == 'R') c++;
            int nPos = r * 3 + c;

            int[] e = actual.getEstado();
            e[posVacio] = e[nPos]; e[nPos] = 0;
            actual = new Tablero(e);
            camino.add(actual);
        }
        return camino;
    }

    private void mostrarPaso(int paso) {
        if (solucionSeleccionada == null || paso < 0 || paso >= solucionSeleccionada.camino.size()) return;

        pasoActual = paso;
        Tablero t = solucionSeleccionada.camino.get(paso);
        actualizarTableroVisual(t.getEstado(), botonesPasoAPaso);

        char mov = (paso > 0) ? solucionSeleccionada.movimientos.charAt(paso - 1) : '-';
        lblPasoInfo.setText(String.format("Paso %d / %d (Movimiento: %s)", paso, solucionSeleccionada.camino.size() - 1, mov));

        btnAnterior.setDisable(pasoActual == 0);
        btnSiguiente.setDisable(pasoActual == solucionSeleccionada.camino.size() - 1);
    }

    private void toggleAnimacion() {
        if (solucionSeleccionada == null) return;

        if (timelineAnimacion != null && timelineAnimacion.getStatus() == Timeline.Status.RUNNING) {
            timelineAnimacion.pause();
            btnPlayPause.setText("▶ Reproducir");
            return;
        }

        if (pasoActual >= solucionSeleccionada.camino.size() - 1) pasoActual = 0;
        btnPlayPause.setText("⏸ Pausar");

        timelineAnimacion = new Timeline(new KeyFrame(Duration.millis(500), e -> {
            pasoActual++;
            mostrarPaso(pasoActual);
            if (pasoActual >= solucionSeleccionada.camino.size() - 1) {
                timelineAnimacion.stop();
                btnPlayPause.setText("▶ Reproducir");
            }
        }));
        timelineAnimacion.setCycleCount(Timeline.INDEFINITE);
        timelineAnimacion.play();
    }

    private void actualizarTableroVisual(int[] estado, Button[] arregloBotones) {
        for (int i = 0; i < 9; i++) {
            int val = estado[i];
            Button btn = arregloBotones[i];
            if (val == 0) {
                btn.setText("");
                btn.getStyleClass().setAll("button", "ficha-vacia");
            } else {
                btn.setText(String.valueOf(val));
                btn.getStyleClass().setAll("button", "ficha-puzzle");
                btn.getStyleClass().setAll("button", "ficha-puzzle");
                if (val == Tablero.META[i]) btn.getStyleClass().add("ficha-correcta");
            }
        }
    }

    private void mostrarAlerta(String t, String m) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(m);
        a.getDialogPane().getStylesheets().add(getClass().getResource("/estilo.css").toExternalForm());
        a.showAndWait();
    }
    private BarChart<String, Number> crearGrafica(String titulo, String ejeY,
                                                  XYChart.Series<String, Number> serie) {
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        y.setLabel(ejeY);

        BarChart<String, Number> chart = new BarChart<>(x, y);
        chart.setTitle(titulo);
        chart.setLegendVisible(false);   // el título ya dice qué es
        chart.setAnimated(false);        // evita glitches al agregar datos dinámicamente
        chart.setCategoryGap(25);
        chart.getData().add(serie);
        return chart;
    }

    public static class MetricaAgente {
        private final SimpleStringProperty heuristica, tiempo, nodos, pasos, movs;
        public MetricaAgente(String h, String t, String n, String p, String m) {
            this.heuristica = new SimpleStringProperty(h);
            this.tiempo = new SimpleStringProperty(t);
            this.nodos = new SimpleStringProperty(n);
            this.pasos = new SimpleStringProperty(p);
            this.movs = new SimpleStringProperty(m);
        }
        public SimpleStringProperty heuristicaProperty() { return heuristica; }
        public SimpleStringProperty tiempoProperty() { return tiempo; }
        public SimpleStringProperty nodosProperty() { return nodos; }
        public SimpleStringProperty pasosProperty() { return pasos; }
        public SimpleStringProperty movsProperty() { return movs; }
    }
}