# 🧩 Sudoku Engine & Game Backend (Java + SQLite)

> Engine computazionale in Java per la generazione e risoluzione deterministica di puzzle Sudoku con garanzia di soluzione unica, architettura a oggetti (OOP) con Design Pattern e persistenza su database relazionale SQL.

---

## 🎯 Panoramica e Obiettivi di Progetto

Il progetto nasce per dimostrare la padronanza di:
1. **Algoritmi & Constraint Satisfaction**: Risoluzione rapida tramite **Backtracking con euristica MRV (Minimum Remaining Values)** e verifica formale dell'**unicità della soluzione** durante la fase di scavo degli indizi.
2. **Architettura Orientata agli Oggetti (OOP)**: Struttura a livelli disaccoppiati (Model, Solver, Generator, Repository, Service, CLI) con applicazione di design pattern notevoli.
3. **Persistenza su Database SQL**: Layer relazionale basato su **SQLite via JDBC** per salvare sessioni di gioco attive, cronologia delle mosse e record nella leaderboard.

---

## 🏗️ Architettura e Design Pattern Utilizzati

- **Strategy Pattern (`SudokuSolver`)**: Permette di disaccoppiare la logica della griglia dall'algoritmo di risoluzione (Brute-force, MRV Backtracking, o futuri algoritmi DLX / Exact Cover).
- **Command Pattern (`Move` / Undo-Redo Stacks)**: Ogni azione dell'utente è incapsulata in un oggetto che memorizza coordinate, vecchio e nuovo valore, consentendo annullamento e ripristino illimitato delle mosse a runtime.
- **DAO & Repository Pattern (`GameRepository`, `DatabaseManager`)**: Separazione totale tra entità di business e query SQL con Prepared Statements e prevenzione di SQL Injection.
- **Layered Architecture**: `CLI / Controller` ➔ `SudokuGameService` ➔ `Model / Repository`.

---

## 🗄️ Modello Dati Relazionale (SQL)

Il database SQLite integrato (`sudoku.db`) viene creato e migrato automaticamente all'avvio con il seguente schema:

```sql
-- Schema e Soluzione dei Puzzle generati
CREATE TABLE puzzles (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    puzzle_data TEXT NOT NULL,
    solution_data TEXT NOT NULL,
    difficulty TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sessioni di gioco salvate (supporta ripresa partita)
CREATE TABLE game_sessions (
    session_id TEXT PRIMARY KEY,
    player_name TEXT NOT NULL,
    difficulty TEXT NOT NULL,
    initial_grid TEXT NOT NULL,
    current_grid TEXT NOT NULL,
    solution_grid TEXT NOT NULL,
    elapsed_seconds INTEGER NOT NULL,
    mistakes INTEGER NOT NULL,
    status TEXT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Storico analitico delle mosse
CREATE TABLE moves (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id TEXT NOT NULL,
    move_order INTEGER NOT NULL,
    cell_row INTEGER NOT NULL,
    cell_col INTEGER NOT NULL,
    old_value INTEGER NOT NULL,
    new_value INTEGER NOT NULL,
    move_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES game_sessions(session_id)
);

-- Classifica tempi e record
CREATE TABLE leaderboard (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    player_name TEXT NOT NULL,
    difficulty TEXT NOT NULL,
    time_seconds INTEGER NOT NULL,
    mistakes INTEGER NOT NULL,
    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## ⚙️ Algoritmo di Generazione a Difficoltà Graduata

1. Generazione di una griglia completa valida partendo dal riempimento casuale delle tre sottomatrici diagonali indipendenti (che non interferiscono tra loro per costruzione).
2. Risoluzione della griglia restante tramite backtracking casuale per ottenere la griglia completa (soluzione ground-truth).
3. Selezione del target di indizi da lasciare in base alla difficoltà:
   - **Facile**: ~40 indizi (38-45)
   - **Medio**: ~33 indizi (30-36)
   - **Difficile**: ~27 indizi (25-29)
   - **Esperto**: ~22 indizi (21-24)
4. Scavo delle celle in ordine casuale: per ogni cella rimossa, l'algoritmo esegue `countSolutions(grid, limit=2)`. Se la rimozione porta a due o più soluzioni possibili, il valore viene immediatamente ripristinato, garantendo che ogni schema generato abbia **una e una sola soluzione valida**.

---

## 🚀 Compilazione ed Esecuzione

### Prerequisiti
- **JDK 17+** (testato e compatibile con OpenJDK 21 e 25 LTS).
- Il driver `sqlite-jdbc.jar` è incluso nella cartella `lib/`.

### Avvio rapido su Windows (Batch script)
```bash
build_and_run.bat
```

### Esecuzione della Suite di Test Algoritmici Integrati
```bash
build_and_run.bat --test
```

### Compilazione manuale con `javac`
```bash
# Compilazione
javac -encoding UTF-8 -cp "lib/sqlite-jdbc.jar" -d bin src/it/fabiozanetti/sudoku/**/*.java src/it/fabiozanetti/sudoku/*.java

# Esecuzione
java -cp "bin;lib/sqlite-jdbc.jar" it.fabiozanetti.sudoku.SudokuApp
```

---

## 🎮 Comandi durante la Partita
- `m <riga> <colonna> <valore>`: Inserisce un numero (es. `m 2 4 7` inserisce 7 alla riga 2, colonna 4; valore 0 cancella).
- `u`: Annulla l'ultima mossa (Undo).
- `r`: Ripristina l'ultima mossa annullata (Redo).
- `h`: Richiede un suggerimento intelligente (Hint calcolato sulla cella più vincolata).
- `s`: Salva lo stato su SQLite ed esce al menu principale.

---

## 👤 Autore
**Fabio Zanetti**  
Studente di Ingegneria Informatica @ Università di Bologna  
- Portfolio: [fabiozanetti.it](https://fabiozanetti-it.vercel.app)  
- GitHub: [@zannaStack](https://github.com/zannaStack)
