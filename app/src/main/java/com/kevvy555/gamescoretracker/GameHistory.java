package com.kevvy555.gamescoretracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class GameHistory {
    public static final class PlayerResult {
        private final String name;
        private final int finalScore;

        public PlayerResult(String name, int finalScore) {
            this.name = name == null ? "" : name.trim();
            this.finalScore = Math.max(0, finalScore);
        }

        public String getName() { return name; }
        public int getFinalScore() { return finalScore; }
    }

    public static final class MatchRecord {
        private final long finishedAtEpochMillis;
        private final List<PlayerResult> players;
        private final List<String> winnerNames;

        public MatchRecord(long finishedAtEpochMillis, List<PlayerResult> players, List<String> winnerNames) {
            this.finishedAtEpochMillis = finishedAtEpochMillis;
            this.players = Collections.unmodifiableList(new ArrayList<>(players));
            this.winnerNames = Collections.unmodifiableList(new ArrayList<>(winnerNames));
        }

        public long getFinishedAtEpochMillis() { return finishedAtEpochMillis; }
        public List<PlayerResult> getPlayers() { return players; }
        public List<String> getWinnerNames() { return winnerNames; }
    }

    public static final class LeaderboardEntry {
        private final String name;
        private final int points;
        private final int gamesPlayed;
        private final int totalScore;
        private final int highScore;

        LeaderboardEntry(String name, int points, int gamesPlayed, int totalScore, int highScore) {
            this.name = name;
            this.points = points;
            this.gamesPlayed = gamesPlayed;
            this.totalScore = totalScore;
            this.highScore = highScore;
        }

        public String getName() { return name; }
        public int getPoints() { return points; }
        public int getGamesPlayed() { return gamesPlayed; }
        public int getTotalScore() { return totalScore; }
        public int getHighScore() { return highScore; }
    }

    private static final class MutableEntry {
        String displayName;
        int points;
        int gamesPlayed;
        int totalScore;
        int highScore;

        MutableEntry(String displayName) {
            this.displayName = displayName;
        }
    }

    private final List<MatchRecord> records = new ArrayList<>();

    public MatchRecord recordGame(GameState game, long finishedAtEpochMillis) {
        if (game == null || !game.isStarted()) {
            throw new IllegalArgumentException("A running game is required.");
        }

        List<PlayerResult> results = new ArrayList<>();
        for (GameState.Player player : game.getPlayers()) {
            results.add(new PlayerResult(player.getName(), player.getScore()));
        }

        MatchRecord record = new MatchRecord(
                finishedAtEpochMillis,
                results,
                game.getWinnerNames());
        records.add(record);
        return record;
    }

    public void addRecord(MatchRecord record) {
        if (record != null) records.add(record);
    }

    public List<MatchRecord> getRecords() {
        return Collections.unmodifiableList(records);
    }

    public List<LeaderboardEntry> getLeaderboard() {
        Map<String, MutableEntry> aggregate = new LinkedHashMap<>();

        for (MatchRecord record : records) {
            for (PlayerResult player : record.players) {
                String key = key(player.name);
                MutableEntry entry = aggregate.get(key);
                if (entry == null) {
                    entry = new MutableEntry(player.name);
                    aggregate.put(key, entry);
                }

                entry.gamesPlayed++;
                entry.totalScore += player.finalScore;
                entry.highScore = Math.max(entry.highScore, player.finalScore);
                if (containsName(record.winnerNames, player.name)) {
                    entry.points++;
                }
            }
        }

        List<LeaderboardEntry> result = new ArrayList<>();
        for (MutableEntry entry : aggregate.values()) {
            result.add(new LeaderboardEntry(
                    entry.displayName,
                    entry.points,
                    entry.gamesPlayed,
                    entry.totalScore,
                    entry.highScore));
        }

        result.sort(Comparator
                .comparingInt(LeaderboardEntry::getPoints).reversed()
                .thenComparing(Comparator.comparingInt(LeaderboardEntry::getTotalScore).reversed())
                .thenComparing(LeaderboardEntry::getName, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    private static boolean containsName(List<String> names, String target) {
        for (String name : names) {
            if (name != null && name.equalsIgnoreCase(target)) return true;
        }
        return false;
    }

    private static String key(String name) {
        return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
    }
}
