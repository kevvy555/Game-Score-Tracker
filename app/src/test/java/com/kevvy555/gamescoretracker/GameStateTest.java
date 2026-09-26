package com.kevvy555.gamescoretracker;

import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class GameStateTest {
    @Test
    public void scoringAdvancesTurnAndAccumulatesScores() {
        GameState state = new GameState();
        state.startGame(Arrays.asList("Kev", "Luke", "Tam"));
        state.addScore(7);
        state.addScore(12);
        state.addScore(3);
        state.addScore(8);

        assertEquals(15, state.getPlayers().get(0).getScore());
        assertEquals(12, state.getPlayers().get(1).getScore());
        assertEquals(3, state.getPlayers().get(2).getScore());
        assertEquals("Luke", state.getCurrentPlayer().getName());
    }

    @Test
    public void undoRestoresScoreAndTurn() {
        GameState state = new GameState();
        state.startGame(Arrays.asList("A", "B"));
        state.addScore(6);
        state.addScore(10);
        GameState.TurnRecord undone = state.undoLastTurn();

        assertEquals("B", undone.getPlayerName());
        assertEquals(0, state.getPlayers().get(1).getScore());
        assertEquals("B", state.getCurrentPlayer().getName());
    }

    @Test
    public void undoWithNoHistoryDoesNothing() {
        GameState state = new GameState();
        state.startGame(Arrays.asList("A", "B"));
        assertNull(state.undoLastTurn());
    }

    @Test
    public void winnerSupportsTies() {
        GameState state = new GameState();
        state.startGame(Arrays.asList("A", "B"));
        state.addScore(12);
        state.addScore(12);
        assertEquals(Arrays.asList("A", "B"), state.getWinnerNames());
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooFewPlayersAreRejected() {
        new GameState().startGame(Collections.singletonList("Solo"));
    }
}
