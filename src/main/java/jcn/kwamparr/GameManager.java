package jcn.kwamparr;

public class GameManager {

    private final KwampaRR plugin;
    private GameState gameState = GameState.Waiting;

    public GameManager(KwampaRR plugin) {
        this.plugin = plugin;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
}
