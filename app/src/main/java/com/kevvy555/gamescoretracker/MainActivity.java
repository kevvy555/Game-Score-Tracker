package com.kevvy555.gamescoretracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class MainActivity extends Activity {
    private static final String PREFS = "qwirkle_score_tracker";
    private static final String STATE_KEY = "active_game";
    private static final String HISTORY_KEY = "game_history_v1";

    private static final int BG = Color.rgb(17, 19, 24);
    private static final int PANEL = Color.rgb(29, 32, 39);
    private static final int PANEL_ACTIVE = Color.rgb(44, 48, 58);
    private static final int TEXT = Color.rgb(245, 247, 250);
    private static final int MUTED = Color.rgb(173, 181, 194);
    private static final int ACCENT = Color.rgb(86, 203, 139);

    private final GameState game = new GameState();
    private final GameHistory gameHistory = new GameHistory();
    private final List<EditText> nameInputs = new ArrayList<>();
    private int setupPlayerCount = 2;
    private EditText scoreInput;
    private boolean hasActiveGame;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        loadGameHistory();
        hasActiveGame = loadGame();
        if (hasActiveGame) showGame(); else showSetup();
    }

    private void showSetup() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);
        LinearLayout root = column();
        root.setPadding(dp(20), dp(24), dp(20), dp(28));
        scroll.addView(root);

        TextView title = text("QWIRKLE", 38, Typeface.BOLD, TEXT);
        title.setGravity(Gravity.CENTER);
        root.addView(title, wide());

        TextView sub = text("SCORE TRACKER", 14, Typeface.BOLD, MUTED);
        sub.setGravity(Gravity.CENTER);
        root.addView(sub, top(2));

        LinearLayout countCard = card();
        root.addView(countCard, top(24));
        countCard.addView(text("Players", 18, Typeface.BOLD, TEXT), wide());

        LinearLayout counter = row();
        counter.setGravity(Gravity.CENTER_VERTICAL);
        countCard.addView(counter, top(10));
        Button minus = button("−", PANEL_ACTIVE, TEXT);
        Button plus = button("+", PANEL_ACTIVE, TEXT);
        TextView count = text(String.valueOf(setupPlayerCount), 28, Typeface.BOLD, TEXT);
        count.setGravity(Gravity.CENTER);
        counter.addView(minus, new LinearLayout.LayoutParams(dp(56), dp(52)));
        counter.addView(count, new LinearLayout.LayoutParams(0, dp(52), 1));
        counter.addView(plus, new LinearLayout.LayoutParams(dp(56), dp(52)));

        LinearLayout namesCard = card();
        root.addView(namesCard, top(14));
        namesCard.addView(text("Player names", 18, Typeface.BOLD, TEXT), wide());
        LinearLayout names = column();
        namesCard.addView(names, top(10));
        rebuildNames(names);

        minus.setOnClickListener(v -> {
            if (setupPlayerCount > GameState.MIN_PLAYERS) {
                setupPlayerCount--;
                count.setText(String.valueOf(setupPlayerCount));
                rebuildNames(names);
            }
        });
        plus.setOnClickListener(v -> {
            if (setupPlayerCount < GameState.MAX_PLAYERS) {
                setupPlayerCount++;
                count.setText(String.valueOf(setupPlayerCount));
                rebuildNames(names);
            }
        });

        Button start = button("START GAME", ACCENT, Color.rgb(12, 30, 21));
        start.setTextSize(18);
        start.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        start.setOnClickListener(v -> startGame());
        LinearLayout.LayoutParams startParams = new LinearLayout.LayoutParams(-1, dp(58));
        startParams.topMargin = dp(18);
        root.addView(start, startParams);

        Button leaderboard = button("LEADERBOARD & HISTORY", PANEL_ACTIVE, TEXT);
        leaderboard.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        leaderboard.setOnClickListener(v -> showLeaderboard());
        LinearLayout.LayoutParams boardParams = new LinearLayout.LayoutParams(-1, dp(52));
        boardParams.topMargin = dp(10);
        root.addView(leaderboard, boardParams);

        TextView note = text("Completed games are stored on this device.", 12, Typeface.NORMAL, MUTED);
        note.setGravity(Gravity.CENTER);
        root.addView(note, top(16));

        TextView trademark = text("Unofficial companion score tracker for Qwirkle®.", 12, Typeface.NORMAL, MUTED);
        trademark.setGravity(Gravity.CENTER);
        root.addView(trademark, top(4));
        setContentView(scroll);
    }

    private void rebuildNames(LinearLayout holder) {
        List<String> existing = new ArrayList<>();
        for (EditText input : nameInputs) existing.add(input.getText().toString());
        nameInputs.clear();
        holder.removeAllViews();

        for (int i = 0; i < setupPlayerCount; i++) {
            EditText input = new EditText(this);
            input.setSingleLine(true);
            input.setTextColor(TEXT);
            input.setHintTextColor(MUTED);
            input.setTextSize(18);
            input.setHint("Player " + (i + 1));
            input.setPadding(dp(14), 0, dp(14), 0);
            input.setBackground(rounded(PANEL_ACTIVE, 12, 0, 0));
            if (i < existing.size()) input.setText(existing.get(i));
            nameInputs.add(input);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(54));
            if (i > 0) p.topMargin = dp(8);
            holder.addView(input, p);
        }
    }

    private void startGame() {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < nameInputs.size(); i++) {
            String name = nameInputs.get(i).getText().toString().trim();
            names.add(name.isEmpty() ? "Player " + (i + 1) : name);
        }
        game.startGame(names);
        hasActiveGame = true;
        saveGame();
        hideKeyboard();
        showGame();
    }

    private void showGame() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        LinearLayout root = column();
        root.setPadding(dp(16), dp(18), dp(16), dp(28));
        scroll.addView(root);

        TextView title = text("QWIRKLE", 26, Typeface.BOLD, TEXT);
        title.setGravity(Gravity.CENTER);
        root.addView(title, wide());

        Button leaderboard = button("LEADERBOARD", PANEL_ACTIVE, TEXT);
        leaderboard.setOnClickListener(v -> showLeaderboard());
        LinearLayout.LayoutParams leaderboardParams = new LinearLayout.LayoutParams(-1, dp(44));
        leaderboardParams.topMargin = dp(8);
        root.addView(leaderboard, leaderboardParams);

        TextView turn = text(game.getCurrentPlayer().getName() + "'s turn", 28, Typeface.BOLD, TEXT);
        turn.setGravity(Gravity.CENTER);
        turn.setPadding(dp(10), dp(14), dp(10), dp(14));
        turn.setBackground(rounded(Color.rgb(35, 70, 52), 14, 2, ACCENT));
        root.addView(turn, top(12));

        LinearLayout scores = column();
        root.addView(scores, top(8));
        for (int i = 0; i < game.getPlayers().size(); i++) {
            GameState.Player player = game.getPlayers().get(i);
            boolean current = i == game.getCurrentPlayerIndex();
            LinearLayout line = row();
            line.setGravity(Gravity.CENTER_VERTICAL);
            line.setPadding(dp(14), dp(8), dp(14), dp(8));
            line.setBackground(rounded(current ? PANEL_ACTIVE : PANEL, 12, current ? 2 : 0, current ? ACCENT : 0));

            TextView name = text((current ? "▶  " : "") + player.getName(), 18,
                    current ? Typeface.BOLD : Typeface.NORMAL, TEXT);
            name.setGravity(Gravity.CENTER_VERTICAL);
            line.addView(name, new LinearLayout.LayoutParams(0, dp(46), 1));

            TextView points = text(String.valueOf(player.getScore()), 26, Typeface.BOLD, current ? ACCENT : TEXT);
            points.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
            line.addView(points, new LinearLayout.LayoutParams(dp(100), dp(46)));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            if (i > 0) lp.topMargin = dp(6);
            scores.addView(line, lp);
        }

        LinearLayout scoring = card();
        root.addView(scoring, top(12));
        scoring.addView(text("Score this turn", 18, Typeface.BOLD, TEXT), wide());

        scoreInput = new EditText(this);
        scoreInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        scoreInput.setSingleLine(true);
        scoreInput.setGravity(Gravity.CENTER);
        scoreInput.setTextSize(34);
        scoreInput.setTextColor(TEXT);
        scoreInput.setHintTextColor(MUTED);
        scoreInput.setHint("0");
        scoreInput.setBackground(rounded(PANEL_ACTIVE, 12, 0, 0));
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(-1, dp(66));
        inputParams.topMargin = dp(10);
        scoring.addView(scoreInput, inputParams);

        LinearLayout quick = row();
        scoring.addView(quick, top(10));
        int[] values = {1, 2, 3, 4, 5, 6};
        for (int value : values) {
            Button b = button("+" + value, PANEL_ACTIVE, TEXT);
            b.setOnClickListener(v -> addToInput(value));
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, dp(48), 1);
            if (value > 1) bp.leftMargin = dp(4);
            quick.addView(b, bp);
        }

        Button qwirkle = button("QWIRKLE  +6", Color.rgb(70, 55, 112), TEXT);
        qwirkle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        qwirkle.setOnClickListener(v -> addToInput(6));
        LinearLayout.LayoutParams qParams = new LinearLayout.LayoutParams(-1, dp(52));
        qParams.topMargin = dp(10);
        scoring.addView(qwirkle, qParams);

        Button commit = button("SCORE & NEXT TURN", ACCENT, Color.rgb(12, 30, 21));
        commit.setTextSize(17);
        commit.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        commit.setOnClickListener(v -> submitTurn());
        LinearLayout.LayoutParams cParams = new LinearLayout.LayoutParams(-1, dp(58));
        cParams.topMargin = dp(10);
        scoring.addView(commit, cParams);

        Button pass = button("PASS / SCORE 0", PANEL_ACTIVE, TEXT);
        pass.setOnClickListener(v -> {
            scoreInput.setText("0");
            submitTurn();
        });
        LinearLayout.LayoutParams passParams = new LinearLayout.LayoutParams(-1, dp(50));
        passParams.topMargin = dp(8);
        scoring.addView(pass, passParams);

        LinearLayout controls = row();
        root.addView(controls, top(10));
        Button undo = button("UNDO", PANEL_ACTIVE, TEXT);
        undo.setEnabled(!game.getHistory().isEmpty());
        undo.setAlpha(undo.isEnabled() ? 1f : 0.4f);
        undo.setOnClickListener(v -> undoTurn());
        LinearLayout.LayoutParams undoParams = new LinearLayout.LayoutParams(0, dp(50), 1);
        undoParams.rightMargin = dp(5);
        controls.addView(undo, undoParams);

        Button end = button("END GAME", Color.rgb(72, 39, 43), Color.rgb(255, 207, 207));
        end.setOnClickListener(v -> endGame());
        LinearLayout.LayoutParams endParams = new LinearLayout.LayoutParams(0, dp(50), 1);
        endParams.leftMargin = dp(5);
        controls.addView(end, endParams);

        addTurnHistory(root);
        setContentView(scroll);
    }

    private void addTurnHistory(LinearLayout root) {
        if (game.getHistory().isEmpty()) return;
        root.addView(text("Recent turns", 17, Typeface.BOLD, TEXT), top(16));
        int first = Math.max(0, game.getHistory().size() - 8);
        for (int i = game.getHistory().size() - 1; i >= first; i--) {
            GameState.TurnRecord record = game.getHistory().get(i);
            TextView line = text(record.getPlayerName() + "  +" + record.getPoints(), 15, Typeface.NORMAL, MUTED);
            line.setPadding(dp(12), dp(9), dp(12), dp(9));
            line.setBackground(rounded(PANEL, 9, 0, 0));
            root.addView(line, top(5));
        }
    }

    private void showLeaderboard() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);
        LinearLayout root = column();
        root.setPadding(dp(16), dp(20), dp(16), dp(28));
        scroll.addView(root);

        TextView title = text("LEADERBOARD", 30, Typeface.BOLD, TEXT);
        title.setGravity(Gravity.CENTER);
        root.addView(title, wide());

        List<GameHistory.LeaderboardEntry> board = gameHistory.getLeaderboard();
        TextView subtitle = text(gameHistory.getRecords().size() + " completed game"
                + (gameHistory.getRecords().size() == 1 ? "" : "s"), 14, Typeface.NORMAL, MUTED);
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle, top(2));

        Button back = button(hasActiveGame ? "BACK TO GAME" : "NEW GAME", PANEL_ACTIVE, TEXT);
        back.setOnClickListener(v -> {
            if (hasActiveGame) showGame(); else showSetup();
        });
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(-1, dp(48));
        backParams.topMargin = dp(14);
        root.addView(back, backParams);

        if (board.isEmpty()) {
            LinearLayout empty = card();
            root.addView(empty, top(16));
            TextView noGames = text("No completed games yet.", 20, Typeface.BOLD, TEXT);
            noGames.setGravity(Gravity.CENTER);
            empty.addView(noGames, wide());
            TextView hint = text("Finish a game and its result will appear here.", 14, Typeface.NORMAL, MUTED);
            hint.setGravity(Gravity.CENTER);
            empty.addView(hint, top(8));
            setContentView(scroll);
            return;
        }

        GameHistory.LeaderboardEntry leader = board.get(0);
        LinearLayout leadCard = card();
        leadCard.setBackground(rounded(Color.rgb(35, 70, 52), 14, 2, ACCENT));
        root.addView(leadCard, top(16));
        TextView leadingLabel = text("CURRENT LEADER", 12, Typeface.BOLD, ACCENT);
        leadingLabel.setGravity(Gravity.CENTER);
        leadCard.addView(leadingLabel, wide());
        TextView leadingName = text(leader.getName(), 30, Typeface.BOLD, TEXT);
        leadingName.setGravity(Gravity.CENTER);
        leadCard.addView(leadingName, top(4));
        TextView leadingPoints = text(leader.getPoints() + " leaderboard point"
                + (leader.getPoints() == 1 ? "" : "s"), 17, Typeface.BOLD, TEXT);
        leadingPoints.setGravity(Gravity.CENTER);
        leadCard.addView(leadingPoints, top(4));

        root.addView(text("Standings", 19, Typeface.BOLD, TEXT), top(18));
        for (int i = 0; i < board.size(); i++) {
            GameHistory.LeaderboardEntry entry = board.get(i);
            LinearLayout line = card();
            line.setPadding(dp(14), dp(12), dp(14), dp(12));
            root.addView(line, top(6));

            LinearLayout heading = row();
            heading.setGravity(Gravity.CENTER_VERTICAL);
            line.addView(heading, wide());

            TextView name = text((i + 1) + ".  " + entry.getName(), 19, Typeface.BOLD, TEXT);
            heading.addView(name, new LinearLayout.LayoutParams(0, -2, 1));

            TextView points = text(entry.getPoints() + " pt" + (entry.getPoints() == 1 ? "" : "s"),
                    20, Typeface.BOLD, i == 0 ? ACCENT : TEXT);
            points.setGravity(Gravity.END);
            heading.addView(points, new LinearLayout.LayoutParams(dp(92), -2));

            String stats = entry.getGamesPlayed() + " games  •  "
                    + entry.getTotalScore() + " total score  •  "
                    + entry.getHighScore() + " best";
            line.addView(text(stats, 13, Typeface.NORMAL, MUTED), top(5));
        }

        root.addView(text("Game history", 19, Typeface.BOLD, TEXT), top(22));
        List<GameHistory.MatchRecord> records = gameHistory.getRecords();
        for (int i = records.size() - 1; i >= 0; i--) {
            GameHistory.MatchRecord record = records.get(i);
            LinearLayout match = card();
            match.setPadding(dp(14), dp(13), dp(14), dp(13));
            root.addView(match, top(7));

            match.addView(text(formatDateTime(record.getFinishedAtEpochMillis()),
                    14, Typeface.BOLD, MUTED), wide());

            String winnerLabel = record.getWinnerNames().size() == 1 ? "Winner: " : "Winners: ";
            match.addView(text(winnerLabel + String.join(" & ", record.getWinnerNames()),
                    17, Typeface.BOLD, ACCENT), top(5));

            for (GameHistory.PlayerResult player : record.getPlayers()) {
                LinearLayout scoreLine = row();
                scoreLine.setGravity(Gravity.CENTER_VERTICAL);
                match.addView(scoreLine, top(4));

                TextView playerName = text(player.getName(), 16, Typeface.NORMAL, TEXT);
                scoreLine.addView(playerName, new LinearLayout.LayoutParams(0, -2, 1));

                TextView score = text(String.valueOf(player.getFinalScore()), 17, Typeface.BOLD, TEXT);
                score.setGravity(Gravity.END);
                scoreLine.addView(score, new LinearLayout.LayoutParams(dp(80), -2));
            }
        }

        TextView rule = text("Each game winner earns 1 leaderboard point. Tied winners each earn 1 point.",
                12, Typeface.NORMAL, MUTED);
        rule.setGravity(Gravity.CENTER);
        root.addView(rule, top(18));
        setContentView(scroll);
    }

    private void addToInput(int amount) {
        int value = 0;
        try {
            String raw = scoreInput.getText().toString().trim();
            if (!raw.isEmpty()) value = Integer.parseInt(raw);
        } catch (NumberFormatException ignored) { }
        value = Math.min(GameState.MAX_TURN_SCORE, value + amount);
        scoreInput.setText(String.valueOf(value));
        scoreInput.setSelection(scoreInput.length());
    }

    private void submitTurn() {
        int points;
        try {
            String raw = scoreInput.getText().toString().trim();
            points = raw.isEmpty() ? 0 : Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            Toast.makeText(this, "Enter a valid score.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (points < 0 || points > GameState.MAX_TURN_SCORE) {
            Toast.makeText(this, "Score must be between 0 and 999.", Toast.LENGTH_SHORT).show();
            return;
        }
        GameState.TurnRecord record = game.addScore(points);
        saveGame();
        hideKeyboard();
        Toast.makeText(this, record.getPlayerName() + " scored " + points, Toast.LENGTH_SHORT).show();
        showGame();
    }

    private void undoTurn() {
        GameState.TurnRecord record = game.undoLastTurn();
        if (record == null) return;
        saveGame();
        Toast.makeText(this, "Undid " + record.getPlayerName() + "'s turn", Toast.LENGTH_SHORT).show();
        showGame();
    }

    private void endGame() {
        List<String> winners = game.getWinnerNames();
        StringBuilder message = new StringBuilder();
        if (winners.size() == 1) message.append(winners.get(0)).append(" wins!");
        else message.append("Tie: ").append(String.join(" & ", winners));

        message.append("\n\nFinal scores\n");
        for (GameState.Player player : game.getPlayers()) {
            message.append(player.getName()).append(": ").append(player.getScore()).append('\n');
        }
        message.append("\nSave this result to the leaderboard and end the game?");

        new AlertDialog.Builder(this)
                .setTitle("End game")
                .setMessage(message.toString().trim())
                .setNegativeButton("Cancel", null)
                .setPositiveButton("END & SAVE", (dialog, which) -> completeGame())
                .show();
    }

    private void completeGame() {
        gameHistory.recordGame(game, System.currentTimeMillis());
        saveGameHistory();
        setupPlayerCount = game.getPlayers().size();
        clearGame();
        Toast.makeText(this, "Game saved to leaderboard", Toast.LENGTH_SHORT).show();
        showLeaderboard();
    }

    private boolean loadGame() {
        String raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString(STATE_KEY, null);
        if (raw == null || raw.isEmpty()) return false;
        try {
            JSONObject root = new JSONObject(raw);
            JSONArray savedPlayers = root.getJSONArray("players");
            List<String> names = new ArrayList<>();
            List<Integer> scores = new ArrayList<>();
            for (int i = 0; i < savedPlayers.length(); i++) {
                JSONObject p = savedPlayers.getJSONObject(i);
                names.add(p.getString("name"));
                scores.add(p.getInt("score"));
            }

            List<GameState.TurnRecord> history = new ArrayList<>();
            JSONArray savedHistory = root.optJSONArray("history");
            if (savedHistory != null) {
                for (int i = 0; i < savedHistory.length(); i++) {
                    JSONObject h = savedHistory.getJSONObject(i);
                    history.add(new GameState.TurnRecord(
                            h.getInt("playerIndex"),
                            h.getString("playerName"),
                            h.getInt("points")));
                }
            }
            game.restore(names, scores, root.getInt("currentPlayerIndex"), history);
            return true;
        } catch (Exception ex) {
            clearGame();
            return false;
        }
    }

    private void saveGame() {
        try {
            JSONObject root = new JSONObject();
            root.put("currentPlayerIndex", game.getCurrentPlayerIndex());

            JSONArray players = new JSONArray();
            for (GameState.Player player : game.getPlayers()) {
                JSONObject p = new JSONObject();
                p.put("name", player.getName());
                p.put("score", player.getScore());
                players.put(p);
            }
            root.put("players", players);

            JSONArray history = new JSONArray();
            for (GameState.TurnRecord record : game.getHistory()) {
                JSONObject h = new JSONObject();
                h.put("playerIndex", record.getPlayerIndex());
                h.put("playerName", record.getPlayerName());
                h.put("points", record.getPoints());
                history.put(h);
            }
            root.put("history", history);

            getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                    .putString(STATE_KEY, root.toString())
                    .apply();
        } catch (Exception ex) {
            Toast.makeText(this, "Could not save the game.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadGameHistory() {
        String raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString(HISTORY_KEY, null);
        if (raw == null || raw.isEmpty()) return;

        try {
            JSONArray records = new JSONArray(raw);
            for (int i = 0; i < records.length(); i++) {
                try {
                    JSONObject saved = records.getJSONObject(i);
                    List<GameHistory.PlayerResult> players = new ArrayList<>();
                    JSONArray savedPlayers = saved.getJSONArray("players");
                    for (int p = 0; p < savedPlayers.length(); p++) {
                        JSONObject player = savedPlayers.getJSONObject(p);
                        players.add(new GameHistory.PlayerResult(
                                player.getString("name"),
                                player.getInt("score")));
                    }

                    List<String> winners = new ArrayList<>();
                    JSONArray savedWinners = saved.getJSONArray("winners");
                    for (int w = 0; w < savedWinners.length(); w++) {
                        winners.add(savedWinners.getString(w));
                    }

                    gameHistory.addRecord(new GameHistory.MatchRecord(
                            saved.getLong("finishedAt"),
                            players,
                            winners));
                } catch (Exception ignored) {
                    // Keep valid older records if one saved match is malformed.
                }
            }
        } catch (Exception ignored) {
            // A corrupt history must never prevent the scorer from opening.
        }
    }

    private void saveGameHistory() {
        try {
            JSONArray records = new JSONArray();
            for (GameHistory.MatchRecord record : gameHistory.getRecords()) {
                JSONObject saved = new JSONObject();
                saved.put("finishedAt", record.getFinishedAtEpochMillis());

                JSONArray players = new JSONArray();
                for (GameHistory.PlayerResult player : record.getPlayers()) {
                    JSONObject p = new JSONObject();
                    p.put("name", player.getName());
                    p.put("score", player.getFinalScore());
                    players.put(p);
                }
                saved.put("players", players);

                JSONArray winners = new JSONArray();
                for (String winner : record.getWinnerNames()) winners.put(winner);
                saved.put("winners", winners);

                records.put(saved);
            }

            getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                    .putString(HISTORY_KEY, records.toString())
                    .apply();
        } catch (Exception ex) {
            Toast.makeText(this, "Could not save game history.", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearGame() {
        hasActiveGame = false;
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().remove(STATE_KEY).apply();
    }

    private String formatDateTime(long epochMillis) {
        return new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.UK).format(new Date(epochMillis));
    }

    private void hideKeyboard() {
        InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (keyboard != null && getCurrentFocus() != null) {
            keyboard.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private LinearLayout card() {
        LinearLayout layout = column();
        layout.setPadding(dp(16), dp(16), dp(16), dp(16));
        layout.setBackground(rounded(PANEL, 14, 0, 0));
        return layout;
    }

    private LinearLayout column() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private LinearLayout row() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        return layout;
    }

    private TextView text(String value, int size, int style, int colour) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(colour);
        view.setTypeface(Typeface.DEFAULT, style);
        return view;
    }

    private Button button(String value, int background, int foreground) {
        Button button = new Button(this);
        button.setText(value);
        button.setTextColor(foreground);
        button.setTextSize(15);
        button.setAllCaps(false);
        button.setPadding(dp(8), 0, dp(8), 0);
        button.setBackground(rounded(background, 11, 0, 0));
        return button;
    }

    private GradientDrawable rounded(int fill, int radius, int stroke, int strokeColour) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(radius));
        if (stroke > 0) drawable.setStroke(dp(stroke), strokeColour);
        return drawable;
    }

    private LinearLayout.LayoutParams wide() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams top(int margin) {
        LinearLayout.LayoutParams params = wide();
        params.topMargin = dp(margin);
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
