package sudokuEngine.ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import sudokuEngine.controller.SudokuController;
import sudokuEngine.model.Difficulty;
import sudokuEngine.model.GameSession;
import sudokuEngine.model.Grid;
import sudokuEngine.persistenceDB.DatabaseManager;
import sudokuEngine.persistenceDB.GameRepository;

import java.util.List;

/**
 * Interfaccia Grafica Completa in JavaFX per Sudoku Engine & Solver.
 * Include gestione eventi da tastiera/mouse, animazioni del timer,
 * evidenziazione interattiva delle sottomatrici 3x3 e integrazione diretta con il database SQL.
 */
// Layout responsive con ridimensionamento finestra
public class SudokuGUI extends Application {

    private SudokuController gameService;
    private GameSession currentSession;

    private TextField[][] cellFields;
    private Label timerLabel;
    private Label mistakesLabel;
    private Label statusLabel;
    private ComboBox<Difficulty> difficultyCombo;
    private String currentPlayerName = "Fabio";

    private Timeline timeline;
    private long elapsedSeconds = 0;
    private boolean isUpdatingUI = false;

    @Override
    public void start(Stage primaryStage) {
        DatabaseManager dbManager = new DatabaseManager("jdbc:sqlite:sudoku.db");
        this.gameService = new SudokuController(dbManager);
        this.cellFields = new TextField[9][9];

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0f172a;");

        root.setTop(buildTopBar(primaryStage));
        root.setCenter(buildGridPane());
        root.setBottom(buildBottomControls(primaryStage));

        setupTimer();
        startNewGame(Difficulty.MEDIUM);

        Scene scene = new Scene(root, 780, 840);
        primaryStage.setTitle("Sudoku Engine & Solver - JavaFX (Fabio Zanetti)");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(740);
        primaryStage.setMinHeight(800);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    private Node buildTopBar(Stage stage) {
        VBox topContainer = new VBox(10);
        topContainer.setPadding(new Insets(18, 20, 10, 20));
        topContainer.setStyle("-fx-background-color: #1e293b; -fx-border-color: #334155; -fx-border-width: 0 0 1 0;");

        // Titolo & Sottotitolo
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("🧩 SUDOKU ENGINE & SOLVER");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        Label badge = new Label("JavaFX 25 + SQLite");
        badge.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 6; -fx-font-size: 11px; -fx-font-weight: bold;");

        headerBox.getChildren().addAll(title, badge);

        // Barra con controlli stato e tempo
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER_LEFT);

        Label diffLabel = new Label("Difficoltà:");
        diffLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold;");

        difficultyCombo = new ComboBox<>();
        difficultyCombo.getItems().addAll(Difficulty.values());
        difficultyCombo.setValue(Difficulty.MEDIUM);
        difficultyCombo.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-font-weight: bold;");

        Button newGameBtn = new Button("Nuova Partita");
        newGameBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 14; -fx-background-radius: 6; -fx-cursor: hand;");
        newGameBtn.setOnAction(e -> startNewGame(difficultyCombo.getValue()));

        timerLabel = new Label("⏱ Tempo: 00:00");
        timerLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        mistakesLabel = new Label("Errori: 0/3");
        mistakesLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #10b981;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button leaderboardBtn = new Button("🏆 Classifica SQL");
        leaderboardBtn.setStyle("-fx-background-color: #475569; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 6; -fx-cursor: hand;");
        leaderboardBtn.setOnAction(e -> showLeaderboardDialog(stage));

        statsBox.getChildren().addAll(diffLabel, difficultyCombo, newGameBtn, timerLabel, mistakesLabel, spacer, leaderboardBtn);

        topContainer.getChildren().addAll(headerBox, statsBox);
        return topContainer;
    }

    private Node buildGridPane() {
        GridPane mainGrid = new GridPane();
        mainGrid.setAlignment(Pos.CENTER);
        mainGrid.setPadding(new Insets(20));

        // Costruisci 9 sottomatrici 3x3 per bordi ben definiti
        for (int blockRow = 0; blockRow < 3; blockRow++) {
            for (int blockCol = 0; blockCol < 3; blockCol++) {
                GridPane subGrid = new GridPane();
                subGrid.setStyle("-fx-border-color: #64748b; -fx-border-width: 2px; -fx-background-color: #1e293b;");

                for (int r = 0; r < 3; r++) {
                    for (int c = 0; c < 3; c++) {
                        int globalRow = blockRow * 3 + r;
                        int globalCol = blockCol * 3 + c;

                        TextField cell = createCellTextField(globalRow, globalCol);
                        cellFields[globalRow][globalCol] = cell;
                        subGrid.add(cell, c, r);
                    }
                }
                mainGrid.add(subGrid, blockCol, blockRow);
            }
        }

        StackPane centerBox = new StackPane(mainGrid);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(10));
        return centerBox;
    }

    private TextField createCellTextField(int row, int col) {
        TextField tf = new TextField();
        tf.setPrefSize(60, 60);
        tf.setMinSize(50, 50);
        tf.setAlignment(Pos.CENTER);
        tf.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-background-color: #1e293b; -fx-text-fill: #f8fafc; -fx-border-color: #334155; -fx-border-width: 0.5px;");

        // Gestione inserimento singolo tasto (1-9 o Backspace/Delete)
        tf.setOnKeyPressed(event -> {
            if (currentSession == null || currentSession.getInitialGrid().isInitialClue(row, col)) {
                return;
            }

            KeyCode code = event.getCode();
            if (code.isDigitKey()) {
                String digitStr = event.getText();
                int digit = Integer.parseInt(digitStr);
                if (digit >= 1 && digit <= 9) {
                    handleUserMove(row, col, digit);
                    event.consume();
                }
            } else if (code == KeyCode.BACK_SPACE || code == KeyCode.DELETE || code == KeyCode.NUMPAD0 || code == KeyCode.DIGIT0) {
                handleUserMove(row, col, 0);
                event.consume();
            }
        });

        tf.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                highlightRelatedCells(row, col);
            } else {
                clearHighlights();
            }
        });

        return tf;
    }

    private Node buildBottomControls(Stage stage) {
        VBox bottomContainer = new VBox(12);
        bottomContainer.setPadding(new Insets(10, 20, 20, 20));
        bottomContainer.setAlignment(Pos.CENTER);

        HBox actionsBox = new HBox(12);
        actionsBox.setAlignment(Pos.CENTER);

        Button undoBtn = new Button("↩ Annulla (Undo)");
        styleActionButton(undoBtn, "#475569");
        undoBtn.setOnAction(e -> {
            if (currentSession != null && gameService.undo(currentSession)) {
                refreshUIFromSession();
            }
        });

        Button redoBtn = new Button("↪ Ripristina (Redo)");
        styleActionButton(redoBtn, "#475569");
        redoBtn.setOnAction(e -> {
            if (currentSession != null && gameService.redo(currentSession)) {
                refreshUIFromSession();
            }
        });

        Button hintBtn = new Button("💡 Suggerimento (Hint)");
        styleActionButton(hintBtn, "#eab308");
        hintBtn.setOnAction(e -> handleHint());

        Button solveBtn = new Button("⚡ Risolvi Subito (AI)");
        styleActionButton(solveBtn, "#6366f1");
        solveBtn.setOnAction(e -> handleSolvePuzzle());

        Button saveBtn = new Button("💾 Salva");
        styleActionButton(saveBtn, "#0ea5e9");
        saveBtn.setOnAction(e -> {
            if (currentSession != null) {
                currentSession.addElapsedSeconds(elapsedSeconds);
                gameService.makeMove(currentSession, 0, 0, currentSession.getCurrentGrid().get(0, 0));
                showAlert("Partita Salvata", "Lo stato della partita corrente è stato registrato nel database SQLite!", Alert.AlertType.INFORMATION);
            }
        });

        Button loadBtn = new Button("📂 Riprendi Partita");
        styleActionButton(loadBtn, "#10b981");
        loadBtn.setOnAction(e -> showResumeDialog(stage));

        actionsBox.getChildren().addAll(undoBtn, redoBtn, hintBtn, solveBtn, saveBtn, loadBtn);

        statusLabel = new Label("Tasti da 1 a 9 per inserire un numero, Backspace per cancellare.");
        statusLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        bottomContainer.getChildren().addAll(actionsBox, statusLabel);
        return bottomContainer;
    }

    private void styleActionButton(Button btn, String color) {
        btn.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 14; -fx-background-radius: 6; -fx-cursor: hand;", color));
    }

    private void startNewGame(Difficulty difficulty) {
        statusLabel.setText("Generazione puzzle a soluzione unica garantita in corso...");
        currentSession = gameService.createGame(currentPlayerName, difficulty);
        elapsedSeconds = 0;
        timeline.playFromStart();
        refreshUIFromSession();
        statusLabel.setText("Partita avviata (" + difficulty.getDisplayName() + "). Inserisci i numeri!");
    }

    private void handleUserMove(int row, int col, int value) {
        if (currentSession == null || currentSession.getStatus() != GameSession.Status.IN_PROGRESS) {
            return;
        }

        boolean ok = gameService.makeMove(currentSession, row, col, value);
        if (ok) {
            refreshUIFromSession();

            if (currentSession.getStatus() == GameSession.Status.COMPLETED) {
                timeline.stop();
                showAlert("🎉 Vittoria!", "Congratulazioni " + currentPlayerName + "! Hai completato con successo il Sudoku in "
                        + formatTime(elapsedSeconds) + "!", Alert.AlertType.INFORMATION);
            } else if (currentSession.getStatus() == GameSession.Status.FAILED) {
                timeline.stop();
                showAlert("❌ Game Over", "Hai raggiunto il limite di 3 errori. Riprova con una nuova partita!", Alert.AlertType.ERROR);
            }
        }
    }

    private void handleHint() {
        if (currentSession == null || currentSession.getStatus() != GameSession.Status.IN_PROGRESS) return;
        var hint = gameService.getHint(currentSession);
        if (hint.value() != 0) {
            handleUserMove(hint.row(), hint.col(), hint.value());
            cellFields[hint.row()][hint.col()].setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-background-color: #14532d; -fx-text-fill: #4ade80; -fx-border-color: #22c55e; -fx-border-width: 1.5px;");
            statusLabel.setText("💡 " + hint.message());
        }
    }

    private void handleSolvePuzzle() {
        if (currentSession == null || currentSession.getStatus() != GameSession.Status.IN_PROGRESS) return;
        Grid sol = currentSession.getSolutionGrid();
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (!currentSession.getInitialGrid().isInitialClue(r, c)) {
                    currentSession.getCurrentGrid().set(r, c, sol.get(r, c));
                }
            }
        }
        currentSession.setStatus(GameSession.Status.COMPLETED);
        timeline.stop();
        refreshUIFromSession();
        statusLabel.setText("⚡ Puzzle completato istantaneamente con il solver Backtracking MRV!");
    }

    private void refreshUIFromSession() {
        if (currentSession == null) return;
        isUpdatingUI = true;

        Grid current = currentSession.getCurrentGrid();
        Grid initial = currentSession.getInitialGrid();

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                int val = current.get(r, c);
                TextField tf = cellFields[r][c];
                tf.setText(val == 0 ? "" : String.valueOf(val));

                if (initial.isInitialClue(r, c)) {
                    // Indizio fisso iniziale (colore ciano/blu, bloccato)
                    tf.setEditable(false);
                    tf.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-background-color: #0f172a; -fx-text-fill: #38bdf8; -fx-border-color: #334155; -fx-border-width: 0.5px;");
                } else {
                    // Cella utente modificabile
                    tf.setEditable(false); // input gestito via KeyPressed per robustezza
                    tf.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-background-color: #1e293b; -fx-text-fill: #4ade80; -fx-border-color: #334155; -fx-border-width: 0.5px;");
                }
            }
        }

        mistakesLabel.setText("Errori: " + currentSession.getMistakes() + "/3");
        if (currentSession.getMistakes() > 0) {
            mistakesLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
        } else {
            mistakesLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #10b981;");
        }

        isUpdatingUI = false;
    }

    private void highlightRelatedCells(int targetRow, int targetCol) {
        if (currentSession == null) return;
        int subRow = (targetRow / 3) * 3;
        int subCol = (targetCol / 3) * 3;

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                boolean inSub = (r >= subRow && r < subRow + 3 && c >= subCol && c < subCol + 3);
                if (r == targetRow && c == targetCol) {
                    cellFields[r][c].setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-background-color: #334155; -fx-text-fill: #f8fafc; -fx-border-color: #60a5fa; -fx-border-width: 2px;");
                } else if (r == targetRow || c == targetCol || inSub) {
                    // Evidenziazione leggera della riga/colonna/blocco
                    String textColor = currentSession.getInitialGrid().isInitialClue(r, c) ? "#38bdf8" : "#4ade80";
                    cellFields[r][c].setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-background-color: #273549; -fx-text-fill: " + textColor + "; -fx-border-color: #475569; -fx-border-width: 0.5px;");
                }
            }
        }
    }

    private void clearHighlights() {
        refreshUIFromSession();
    }

    private void setupTimer() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            elapsedSeconds++;
            timerLabel.setText("⏱ Tempo: " + formatTime(elapsedSeconds));
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    private String formatTime(long totalSecs) {
        long m = totalSecs / 60;
        long s = totalSecs % 60;
        return String.format("%02d:%02d", m, s);
    }

    private void showLeaderboardDialog(Stage owner) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Classifica Tempi (SQLite Database)");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #0f172a;");

        Label heading = new Label("🏆 TOP RECORD DA DATABASE SQLITE");
        heading.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        TabPane tabPane = new TabPane();
        for (Difficulty d : Difficulty.values()) {
            Tab tab = new Tab(d.getDisplayName());
            tab.setClosable(false);

            ListView<String> listView = new ListView<>();
            listView.setStyle("-fx-background-color: #1e293b; -fx-text-fill: white;");

            List<GameRepository.LeaderboardEntry> entries = gameService.getLeaderboard(d, 10);
            if (entries.isEmpty()) {
                listView.getItems().add("Nessun record registrato per " + d.getDisplayName());
            } else {
                for (int i = 0; i < entries.size(); i++) {
                    var e = entries.get(i);
                    listView.getItems().add(String.format("#%d %-15s | Tempo: %s | Errori: %d | Data: %s",
                            i + 1, e.playerName(), formatTime(e.timeSeconds()), e.mistakes(), e.completedAt()));
                }
            }
            tab.setContent(listView);
            tabPane.getTabs().add(tab);
        }

        Button closeBtn = new Button("Chiudi");
        closeBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 16;");
        closeBtn.setOnAction(e -> dialog.close());

        content.getChildren().addAll(heading, tabPane, closeBtn);
        dialog.setScene(new Scene(content, 560, 420));
        dialog.show();
    }

    private void showResumeDialog(Stage owner) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Riprendi Partita Salvata");

        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #0f172a;");

        Label lbl = new Label("Seleziona una sessione recente:");
        lbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

        ListView<String> listView = new ListView<>();
        List<GameRepository.SessionSummary> sessions = gameService.getRecentGames(10);
        for (var s : sessions) {
            listView.getItems().add(String.format("ID: %s | %s | %s | Errori: %d/3 | Data: %s",
                    s.sessionId(), s.playerName(), s.difficulty(), s.mistakes(), s.updatedAt()));
        }

        Button loadBtn = new Button("Carica Selezionata");
        loadBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold;");
        loadBtn.setOnAction(e -> {
            int idx = listView.getSelectionModel().getSelectedIndex();
            if (idx >= 0 && idx < sessions.size()) {
                currentSession = gameService.loadGame(sessions.get(idx).sessionId());
                if (currentSession != null) {
                    elapsedSeconds = currentSession.getElapsedSeconds();
                    refreshUIFromSession();
                    timeline.playFromStart();
                    dialog.close();
                    statusLabel.setText("Partita ripristinata dal database SQLite!");
                }
            }
        });

        box.getChildren().addAll(lbl, listView, loadBtn);
        dialog.setScene(new Scene(box, 520, 360));
        dialog.show();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
