package com.kevvy555.gamescoretracker;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GameHistoryTest {
    @Test
    public void completedGamesAwardOnePointToWinnerAndTrackScores() {
        GameHistory history = new GameHistory();

        GameState first = gameWithScores("Kev", 20, "Luke", 12);
        history.recordGame(first, 1000L);

        GameState second = gameWithScores("Kev", 8, "Luke", 25);
        history.recordGame(second, 2000L);

        List<GameHistory.LeaderboardEntry> board = history.getLeaderboard();

        assertEquals("Luke", board.get(0).getName());
        assertEquals(1, board.get(0).getPoints());
        assertEquals(2, board.get(0).getGamesPlayed());
        assertEquals(37, board.get(0).getTotalScore());
        assertEquals(25, board.get(0).getHighScore());

        assertEquals("Kev", board.get(1).getName());
        assertEquals(1, board.get(1).getPoints());
        assertEquals(28, board.get(1).getTotalScore());
    }

    @Test
    public void tieAwardsOnePointToEachWinner() {
        GameHistory history = new GameHistory();
        GameState game = gameWithScores("Kev", 18, "Luke", 18);

        history.recordGame(game, 123456L);
        List<GameHistory.LeaderboardEntry> board = history.getLeaderboard();

        assertEquals(1, board.get(0).getPoints());
        assertEquals(1, board.get(1).getPoints());
        assertEquals(123456L, history.getRecords().get(0).getFinishedAtEpochMillis());
    }

    @Test
    public void samePlayerNameIsAggregatedIgnoringCase() {
        GameHistory history = new GameHistory();

        history.recordGame(gameWithScores("Luke", 30, "Kev", 20), 1L);
        history.recordGame(gameWithScores("luke", 22, "Kev", 25), 2L);

        List<GameHistory.LeaderboardEntry> board = history.getLeaderboard();
        GameHistory.LeaderboardEntry luke = find(board, "Luke");

        assertEquals(2, luke.getGamesPlayed());
        assertEquals(1, luke.getPoints());
        assertEquals(52, luke.getTotalScore());
    }

    private static GameState gameWithScores(String firstName, int firstScore, String secondName, int secondScore) {
        GameState state = new GameState();
        state.startGame(Arrays.asList(firstName, secondName));
        state.addScore(firstScore);
        state.addScore(secondScore);
        return state;
    }

    private static GameHistory.LeaderboardEntry find(List<GameHistory.LeaderboardEntry> board, String name) {
        for (GameHistory.LeaderboardEntry entry : board) {
            if (entry.getName().equalsIgnoreCase(name)) return entry;
        }
        throw new AssertionError("Player not found: " + name);
    }
}
