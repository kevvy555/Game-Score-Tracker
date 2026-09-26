package com.kevvy555.gamescoretracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GameState {
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 6;
    public static final int MAX_TURN_SCORE = 999;

    public static final class Player {
        private final String name;
        private int score;

        Player(String name, int score) {
            this.name = name;
            this.score = score;
        }

        public String getName() { return name; }
        public int getScore() { return score; }
    }

    public static final class TurnRecord {
        private final int playerIndex;
        private final String playerName;
        private final int points;

        public TurnRecord(int playerIndex, String playerName, int points) {
            this.playerIndex = playerIndex;
            this.playerName = playerName;
            this.points = points;
        }

        public int getPlayerIndex() { return playerIndex; }
        public String getPlayerName() { return playerName; }
        public int getPoints() { return points; }
    }

    private final List<Player> players = new ArrayList<>();
    private final List<TurnRecord> history = new ArrayList<>();
    private int currentPlayerIndex;

    public void startGame(List<String> names) {
        if (names == null || names.size() < MIN_PLAYERS || names.size() > MAX_PLAYERS) {
            throw new IllegalArgumentException("A game needs between 2 and 6 players.");
        }
        players.clear();
        history.clear();
        currentPlayerIndex = 0;
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i) == null ? "" : names.get(i).trim();
            if (name.isEmpty()) name = "Player " + (i + 1);
            players.add(new Player(name, 0));
        }
    }

    public void restore(List<String> names, List<Integer> scores, int currentIndex, List<TurnRecord> records) {
        if (names == null || scores == null || names.size() != scores.size()
                || names.size() < MIN_PLAYERS || names.size() > MAX_PLAYERS) {
            throw new IllegalArgumentException("Invalid saved game.");
        }
        players.clear();
        history.clear();
        for (int i = 0; i < names.size(); i++) {
            players.add(new Player(names.get(i), Math.max(0, scores.get(i))));
        }
        currentPlayerIndex = Math.floorMod(currentIndex, players.size());
        if (records != null) history.addAll(records);
    }

    public TurnRecord addScore(int points) {
        ensureStarted();
        if (points < 0 || points > MAX_TURN_SCORE) {
            throw new IllegalArgumentException("Turn score must be between 0 and 999.");
        }
        Player player = players.get(currentPlayerIndex);
        player.score += points;
        TurnRecord record = new TurnRecord(currentPlayerIndex, player.name, points);
        history.add(record);
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        return record;
    }

    public TurnRecord undoLastTurn() {
        ensureStarted();
        if (history.isEmpty()) return null;
        TurnRecord record = history.remove(history.size() - 1);
        Player player = players.get(record.playerIndex);
        player.score = Math.max(0, player.score - record.points);
        currentPlayerIndex = record.playerIndex;
        return record;
    }

    public List<String> getWinnerNames() {
        ensureStarted();
        int best = Integer.MIN_VALUE;
        for (Player player : players) best = Math.max(best, player.score);
        List<String> winners = new ArrayList<>();
        for (Player player : players) if (player.score == best) winners.add(player.name);
        return winners;
    }

    public boolean isStarted() { return players.size() >= MIN_PLAYERS; }
    public int getCurrentPlayerIndex() { return currentPlayerIndex; }
    public Player getCurrentPlayer() { ensureStarted(); return players.get(currentPlayerIndex); }
    public List<Player> getPlayers() { return Collections.unmodifiableList(players); }
    public List<TurnRecord> getHistory() { return Collections.unmodifiableList(history); }

    private void ensureStarted() {
        if (!isStarted()) throw new IllegalStateException("No game is currently running.");
    }
}
